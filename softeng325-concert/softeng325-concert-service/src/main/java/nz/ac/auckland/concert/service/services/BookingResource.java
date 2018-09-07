package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.*;
import nz.ac.auckland.concert.service.mappers.ReservationMapper;
import nz.ac.auckland.concert.service.mappers.SeatMapper;
import nz.ac.auckland.concert.service.util.TheatreUtility;
import nz.ac.auckland.concert.utility.TheatreLayout;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.awt.*;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class BookingResource {

    //creates a timeout period of 10 seconds after which a reservation is not valid
    private final LocalTime _timeout = LocalTime.of(0,0,10);

    private final TemporalAmount _time = Duration.ofSeconds(5);

    @POST
    @Path("/reserve")
    public Response makeReservation(@CookieParam("token") Cookie token, ReservationRequestDTO reservationRequestDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            User user = query.getSingleResult();

            if (user == null) { //no user in the DB maps to the provided token
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            em.getTransaction().commit();

            em.getTransaction().begin();
            Concert concert = em.find(Concert.class, reservationRequestDTO.getConcertId());

            // check that the supplied concert id maps to a valid concert
            if (concert == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            //checks the date requested in the reservation corresponds to a date of the concert
            if (!concert.getDates().contains(reservationRequestDTO.getDate())) {
                return Response.status(Response.Status.EXPECTATION_FAILED).build();
            }

            em.getTransaction().commit();
            em.getTransaction().begin();

            // initialise seats if they are not yet in DB
            initialiseSeats(reservationRequestDTO.getConcertId(), reservationRequestDTO.getDate());

            Set<SeatDTO> bookedSeats = new HashSet<>();

            // find all the confirmed bookings for the required concert instance and price band
            TypedQuery<Reservation> reservationQuery = em.createQuery("SELECT r FROM Reservation r WHERE "
                    + "r._request._concertId = :concertId AND r._request._seatType = :seatType AND "
                    + "r._request._date = :dateTime AND r._confirmed = true", Reservation.class)
                    .setParameter("concertId", reservationRequestDTO.getConcertId())
                    .setParameter("seatType", reservationRequestDTO.getSeatType())
                    .setParameter("dateTime", reservationRequestDTO.getDate());

            for (Reservation reservation : reservationQuery.getResultList()) {
                for (Seat seat : reservation.getSeats()) {
                    bookedSeats.add(SeatMapper.toDTO(seat));
                }
            }

            em.getTransaction().commit();

            Set<SeatDTO> availableSeatDTOs = TheatreUtility.findAvailableSeats(reservationRequestDTO.getNumberOfSeats(),
                    reservationRequestDTO.getSeatType(),
                    bookedSeats);

            if (availableSeatDTOs.isEmpty()) {
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }

            Set<Seat> availableSeats = new HashSet<>();

            em.getTransaction().begin();

            for (SeatDTO seatDTO : availableSeatDTOs) {
                Seat seat = em.createQuery("SELECT s FROM Seat s WHERE " +
                        "s._row = :row AND s._number =:number AND " +
                        "s._concertDateTime = :dateTime AND s._concertId = :concertId", Seat.class)
                        .setParameter("row", seatDTO.getRow())
                        .setParameter("number", seatDTO.getNumber())
                        .setParameter("dateTime", reservationRequestDTO.getDate())
                        .setParameter("concertId", reservationRequestDTO.getConcertId())
                        .getSingleResult();
                if (seat.getSeatStatus().equals(Seat.SeatStatus.RESERVED)){
                    return Response.status(Response.Status.NOT_ACCEPTABLE).build();
                }
                availableSeats.add(seat);
            }
            em.getTransaction().commit();
            em.getTransaction().begin();

            Reservation reservation = new Reservation(ReservationMapper.toRequestDomain(reservationRequestDTO),
                    availableSeats, user.getUserName());

            em.persist(reservation);
            em.getTransaction().commit();

            return Response.ok(ReservationMapper.toReservationDTO(reservation))
                    .cookie(new NewCookie("token", token.getValue()))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }

        return null;
    }


    @POST
    @Path("/confirm")
    public Response confirmReservation(@CookieParam("token") Cookie token, ReservationDTO reservationDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            User user = query.getSingleResult();

            if (user == null) { //no user in the DB maps to the provided token
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            if (user.getCreditCard() == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            em.getTransaction().commit();

            em.getTransaction().begin();

            Reservation reservation = em.find(Reservation.class, reservationDTO.getId());

            if (reservation.getReservationTime().plus(_time).isBefore(LocalDateTime.now())) {
                return Response.status(Response.Status.GATEWAY_TIMEOUT).build();
            }

            reservation.setConfirmed(true);

            em.merge(reservation);
            em.getTransaction().commit();

            return Response.status(Response.Status.CREATED).build();

        } finally {
            em.close();
        }

    }

    @GET
    public Response getBookings(@CookieParam("token") Cookie token){
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            User user = query.getSingleResult();

            if (user == null) { //no user in the DB maps to the provided token
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            em.getTransaction().commit();

            String userName = user.getUserName();

            em.getTransaction().begin();

            //return all the confirmed reservations (bookings) from the given user
            List<Reservation> reservations = em.createQuery( "SELECT r FROM Reservation r WHERE " +
                    "r._userName = :userName AND r._confirmed = true", Reservation.class)
                    .setParameter("userName", userName)
                    .getResultList();

            List<BookingDTO> bookingDTOs = new ArrayList<>();

            em.getTransaction().commit();

            em.getTransaction().begin();

            for (Reservation reservation : reservations) {
                Concert concert = em.find(Concert.class, reservation.getReservationRequest().getConcertId());
                bookingDTOs.add(ReservationMapper.toBookingDTO(reservation, concert.getTitle()));
            }
            em.getTransaction().commit();

            GenericEntity<List<BookingDTO>> ge = new GenericEntity<List<BookingDTO>>(bookingDTOs) {
            };

            return Response.ok(ge).build();

        } finally {
            em.close();
        }

    }

    private void initialiseSeats(Long concertId, LocalDateTime dateTime) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            // find all the seats current associated to the concert on the given date
            List<Seat> seats = em.createQuery("SELECT s FROM Seat s WHERE s._concertId = :concertId" +
                    " AND s._concertDateTime = :concertDateTime", Seat.class)
                    .setParameter("concertId", concertId)
                    .setParameter("concertDateTime", dateTime)
                    .getResultList();

            if (seats.isEmpty()) {

                for (PriceBand priceBand : PriceBand.values()) {
                    for (SeatRow seatRow : TheatreLayout.getRowsForPriceBand(priceBand)) {
                        int numSeats = TheatreLayout.getNumberOfSeatsForRow(seatRow);
                        for (int num = 1; num <= numSeats; num++) {
                            Seat seat = new Seat(seatRow,
                                    new SeatNumber(num),
                                    concertId,
                                    dateTime);
                            em.persist(seat);
                        }
                    }
                }
            }
            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }
}

