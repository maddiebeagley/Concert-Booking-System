package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
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
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class BookingResource {

    @POST
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

            TypedQuery<Booking> bookingQuery = em.createQuery("SELECT b FROM Booking b WHERE "
                    + "b._concertId = :concertId AND b._priceBand = :priceBandValue AND "
                    + "b._dateTime = :dateTime", Booking.class)
                    .setParameter("concertId", reservationRequestDTO.getConcertId())
                    .setParameter("priceBandValue", reservationRequestDTO.getSeatType())
                    .setParameter("dateTime", reservationRequestDTO.getDate());

            List<Booking> bookings = bookingQuery.getResultList();

            for (Booking booking : bookings) {
                for (Seat seat : booking.getSeats()) {
                    bookedSeats.add(SeatMapper.toDTO(seat));
                    System.out.println("seat: " + seat.toString());
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
                if (seat.getSeatStatus().equals(SeatStatus.RESERVED)){
                    return Response.status(Response.Status.NOT_ACCEPTABLE).build();
                }
                availableSeats.add(seat);
            }
            em.getTransaction().commit();
            em.getTransaction().begin();

            Reservation reservation = new Reservation(
                    ReservationMapper.toRequestDomain(reservationRequestDTO),
                    availableSeats);

            em.persist(reservation);
            em.getTransaction().commit();

            return Response.ok(ReservationMapper.toDTO(reservation))
                    .cookie(new NewCookie("token", token.getValue()))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }

        return null;
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

