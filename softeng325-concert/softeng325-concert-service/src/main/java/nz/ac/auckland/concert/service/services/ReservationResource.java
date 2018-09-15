package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.domain.jpa.Reservation;
import nz.ac.auckland.concert.service.domain.jpa.Seat;
import nz.ac.auckland.concert.service.domain.jpa.User;
import nz.ac.auckland.concert.service.mappers.ReservationMapper;
import nz.ac.auckland.concert.service.util.TheatreUtility;
import nz.ac.auckland.concert.utility.TheatreLayout;
import org.hibernate.dialect.lock.OptimisticEntityLockException;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/reservations")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class ReservationResource {

    //creates a timeout period of 5 seconds after which a reservation is not valid
    private final TemporalAmount _time = Duration.ofSeconds(5);

    @POST
    @Path("/reserve")
    public Response makeReservation(@CookieParam("token") Cookie token, ReservationRequestDTO reservationRequestDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            Long concertId = reservationRequestDTO.getConcertId();
            LocalDateTime concertDateTime = reservationRequestDTO.getDate();

            //at the start of generating each new reservation, free the seats associated to any expired reservations
            //for the given concert on the given date
            checkForExpiredReservations(concertId, concertDateTime);

            //check that the current user is authenticated
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            //user associated to the supplied token
            User user = query.getSingleResult();

            if (user == null) { //no user in the DB maps to the provided token
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            em.getTransaction().commit();

            em.getTransaction().begin();
            Concert concert = em.find(Concert.class, concertId);

            if (concert == null) { // no concert in the DB matches the supplied concert ID
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            //checks the date requested in the reservation corresponds to a date of the concert
            if (!concert.getDates().contains(concertDateTime)) {
                return Response.status(Response.Status.EXPECTATION_FAILED).build();
            }

            em.getTransaction().commit();

            // initialise seats if they are not yet in DB
            initialiseSeats(concertId, reservationRequestDTO.getDate());

            boolean seatsReserved = false;
            Set<Seat> seatsToReserve = new HashSet<>();

            //try to book seats until there are no concurrency issues
            while (!seatsReserved) {
                try {
                    seatsToReserve = reserveSeats(reservationRequestDTO);
                    seatsReserved = true;
                } catch (OptimisticEntityLockException e) {
                    System.out.println("Concurrency issue whilst reserving seats");
                }
            }

            if (seatsToReserve.isEmpty()) { // there aren't enough seats left for this reservation
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }

            em.getTransaction().begin();

            //generates a new reservation for the user
            Reservation reservation = new Reservation(ReservationMapper.toRequestDomain(reservationRequestDTO),
                    seatsToReserve, user.getUserName());

            em.persist(reservation);
            em.getTransaction().commit();

            //returns the new reservation and the authentication token back to the client
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

            //use the supplied token to check if the user is authenticated
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            //user that maps to the supplied token
            User user = query.getSingleResult();

            if (user == null) { //no user in the DB maps to the provided token
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            if (user.getCreditCard() == null) { //the user does not have a credit card registered
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            em.getTransaction().commit();

            em.getTransaction().begin();

            //find the reservation to be confirmed
            Reservation reservation = em.find(Reservation.class, reservationDTO.getId());

            //if the current reservation has expired, set it to expired
            if (reservation.getReservationTime().plus(_time).isBefore(LocalDateTime.now())) {
                if (!reservation.getReservationStatus().equals(Reservation.ReservationStatus.EXPIRED)) {
                    reservation.setReservationStatus(Reservation.ReservationStatus.EXPIRED);
                    em.merge(reservation);
                    em.getTransaction().commit();
                }
                return Response.status(Response.Status.GATEWAY_TIMEOUT).build();
            }

            reservation.setReservationStatus(Reservation.ReservationStatus.CONFIRMED);

            em.merge(reservation);
            em.getTransaction().commit();

            return Response.status(Response.Status.CREATED).build();

        } finally {
            em.close();
        }

    }

    @GET
    public Response getBookings(@CookieParam("token") Cookie token) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            //use supplied token to authenticate user
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            User user = query.getSingleResult();

            if (user == null) { //no user in the DB maps to the provided token
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            em.getTransaction().commit();

            em.getTransaction().begin();
            String userName = user.getUserName();

            //return all the confirmed reservations (bookings) from the given user
            List<Reservation> reservations = em.createQuery("SELECT r FROM Reservation r WHERE " +
                    "r._userName = :userName AND r._reservationStatus = :reservationStatus", Reservation.class)
                    .setParameter("userName", userName)
                    .setParameter("reservationStatus", Reservation.ReservationStatus.CONFIRMED)
                    .getResultList();

            em.getTransaction().commit();

            em.getTransaction().begin();

            List<BookingDTO> bookingDTOs = new ArrayList<>();

            //convert the given reservations to a transferrable DTO form
            for (Reservation reservation : reservations) {
                Concert concert = em.find(Concert.class, reservation.getReservationRequest().getConcertId());
                bookingDTOs.add(ReservationMapper.toBookingDTO(reservation, concert.getTitle()));
            }
            em.getTransaction().commit();

            GenericEntity<List<BookingDTO>> ge = new GenericEntity<List<BookingDTO>>(bookingDTOs) {
            };

            //return the bookings from the DB to the client
            return Response.ok(ge).build();

        } finally {
            em.close();
        }

    }

    /**
     * Helper method to initialise all the seats for a concert on a given date. All seats are initially set to
     * available so they are free to be reserved by any user. No action is taken if the seats are already in the DB.
     */
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

                //initialise all the seats for the given concert and persist them to the DB
                for (PriceBand priceBand : PriceBand.values()) {
                    for (SeatRow seatRow : TheatreLayout.getRowsForPriceBand(priceBand)) {
                        int numSeats = TheatreLayout.getNumberOfSeatsForRow(seatRow);
                        for (int num = 1; num <= numSeats; num++) {
                            Seat seat = new Seat(seatRow,
                                    new SeatNumber(num),
                                    concertId,
                                    dateTime);
                            seat.setSeatType(priceBand);
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

    /**
     * Helper method that goes through all the reservations associated to a particular concert and marks any
     * reservation that is expired. This will free up the seats in the reservation so that other users are able
     * to book them.
     */
    private void checkForExpiredReservations(Long concertId, LocalDateTime dateTime) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            //find all the reservations associated to the supplied concert on supplied date
            List<Reservation> reservations = em.createQuery("SELECT r FROM Reservation r WHERE " +
                    "r._request._concertId = :concertId AND r._request._date = :dateTime", Reservation.class)
                    .setParameter("concertId", concertId)
                    .setParameter("dateTime", dateTime)
                    .getResultList();

            //mark any reservation that are expired if it has passed its expiration time.
            for (Reservation reservation : reservations) {
                if (reservation.getReservationTime().plus(_time).isBefore(LocalDateTime.now())) {
                    reservation.setReservationStatus(Reservation.ReservationStatus.EXPIRED);
                    em.merge(reservation);
                }
            }

            em.getTransaction().commit();

        } finally {
            em.close();
        }

    }

    /**
     * Will optimistically lock all seats currently available to be booked until they have been set to reserved and
     * persisted to the DB. If concurrent access is detected, an exception will be thrown which will be detected by
     * the calling method. Method can be re-run until an exception is not thrown.
     */
    private Set<Seat> reserveSeats(ReservationRequestDTO reservationRequestDTO) throws OptimisticEntityLockException {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            // find all the available seats for the required concert instance and price band
            List<Seat> allAvailableSeats = em.createQuery("SELECT s FROM Seat s WHERE "
                    + "s._concertId = :concertId AND s._seatType = :seatType AND "
                    + "s._concertDateTime = :dateTime AND s._seatStatus = :seatStatus", Seat.class)
                    .setParameter("concertId", reservationRequestDTO.getConcertId())
                    .setParameter("seatStatus", Seat.SeatStatus.AVAILABLE)
                    .setParameter("seatType", reservationRequestDTO.getSeatType())
                    .setParameter("dateTime", reservationRequestDTO.getDate())
                    .setLockMode(LockModeType.OPTIMISTIC)
                    .getResultList();

            //find a set of seats that the user is able to reserve
            Set<Seat> seatsToReserve = TheatreUtility.findAvailableSeats(reservationRequestDTO.getNumberOfSeats(),
                    new HashSet<>(allAvailableSeats));

            if (seatsToReserve.isEmpty()) { //there are no seats available to book, reservation cannot go through
                return seatsToReserve;
            }

            //ensure all the seats in the reservation are marked as reserved
            for (Seat seat : seatsToReserve) {
                seat.setSeatStatus(Seat.SeatStatus.RESERVED);
                em.persist(seat);
            }

            em.getTransaction().commit();

            return seatsToReserve;

        } finally {
            em.close();
        }

    }
}

