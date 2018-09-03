package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.jpa.Booking;
import nz.ac.auckland.concert.service.mappers.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class BookingResource {

    @GET
    @Path("{priceBand}")
    public Response getBookingFromPriceBand(@PathParam("priceBand") String priceBandValue){
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            // Start a new transaction.
            em.getTransaction().begin();

            TypedQuery<Booking> query = em.createQuery(
                    "SELECT b FROM Booking b WHERE b.PRICE_BAND = :priceBand", Booking.class)
                    .setParameter("priceBand", priceBandValue);

            List<BookingDTO> bookingDTOs = BookingMapper.toDTOList(query.getResultList());
            GenericEntity<List<BookingDTO>> ge = new GenericEntity<List<BookingDTO>>(bookingDTOs) {};

            em.getTransaction().commit();

            return Response.ok(ge).build();

        } finally {
            em.close();
        }
    }
}
