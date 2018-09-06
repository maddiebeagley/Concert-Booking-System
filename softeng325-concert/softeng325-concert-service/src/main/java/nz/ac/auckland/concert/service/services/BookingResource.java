package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.jpa.Booking;
import nz.ac.auckland.concert.service.domain.jpa.User;
import nz.ac.auckland.concert.service.mappers.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class BookingResource {

    @GET
    public Response getBookingFromPriceBand(@CookieParam("token") Cookie token){
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            // Start a new transaction.
            em.getTransaction().begin();

            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            User user  = query.getSingleResult();

            System.out.println("user has been found with name: " + user.getUserName());

            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            TypedQuery<Booking> bookingQuery = em.createQuery(
                    "SELECT b FROM Booking b WHERE b._user = :user", Booking.class)
                    .setParameter("user", user);

            List<BookingDTO> bookingDTOs = BookingMapper.toDTOList(bookingQuery.getResultList());
            GenericEntity<List<BookingDTO>> ge = new GenericEntity<List<BookingDTO>>(bookingDTOs) {};

            em.getTransaction().commit();

            return Response.ok(ge).build();

        } finally {
            em.close();
        }
    }
}
