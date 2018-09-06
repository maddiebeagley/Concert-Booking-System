package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.jpa.*;
import nz.ac.auckland.concert.service.mappers.*;
import nz.ac.auckland.concert.service.util.TheatreUtility;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class BookingResource {

    @GET
    public Response getBookingFromPriceBand(@CookieParam("token") Cookie token) {
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            // Start a new transaction.
            em.getTransaction().begin();

            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            User user = query.getSingleResult();

            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            List<Booking> bookings = em.createQuery(
                    "SELECT b FROM Booking b WHERE b._user = :user", Booking.class)
                    .setParameter("user", user).getResultList();

            List<BookingDTO> bookingDTOs = BookingMapper.toDTOList(bookings);
            GenericEntity<List<BookingDTO>> ge = new GenericEntity<List<BookingDTO>>(bookingDTOs) {
            };

            em.getTransaction().commit();

            return Response.ok(ge).build();

        } finally {
            em.close();
        }
    }


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

            Concert concert = em.find(Concert.class, reservationRequestDTO.getConcertId());

            //checks the date requested in the reservation corresponds to a date of the concert
            if (!concert.getDates().contains(reservationRequestDTO.getDate())) {
                return Response.status(Response.Status.EXPECTATION_FAILED).build();
            }

            Set<SeatDTO> bookedSeats = new HashSet<>();


            TypedQuery<Booking> bookingQuery = em.createQuery("SELECT b FROM Booking b WHERE "
                    + "b._concertId = :concertId AND b._priceBand = :priceBandValue", Booking.class)
                    .setParameter("concertId", reservationRequestDTO.getConcertId())
                    .setParameter("priceBandValue", reservationRequestDTO.getSeatType());

            List<Booking> bookings = bookingQuery.getResultList();

            for (Booking booking : bookings) {
                for (Seat seat : booking.getSeats()) {
                    bookedSeats.add(SeatMapper.toDTO(seat));
                }
            }

            Set<SeatDTO> availableSeatDTOs = TheatreUtility.findAvailableSeats(reservationRequestDTO.getNumberOfSeats(),
                    reservationRequestDTO.getSeatType(),
                    bookedSeats);

            if (availableSeatDTOs.isEmpty()){
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }

            Reservation reservation = new Reservation(
                    ReservationMapper.toRequestDomain(reservationRequestDTO),
                    SeatMapper.toDomainSet(availableSeatDTOs));

            return Response.ok(ReservationMapper.toDTO(reservation))
                    .cookie(new NewCookie("token", token.getValue()))
                    .build();

        } catch (Exception e) {
            System.out.println("i didn't make it to the return statement without throwing");
        } finally {
            em.close();
        }

        return null;
    }
}

