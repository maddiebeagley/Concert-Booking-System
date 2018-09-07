package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.service.domain.jpa.Reservation;
import nz.ac.auckland.concert.service.mappers.*;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Path("/reservations")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class ReservationResource {

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response makeReservation(Reservation reservation){
        EntityManager em = PersistenceManager.instance().createEntityManager();

        em.getTransaction().begin();
        em.persist(reservation);
        em.getTransaction().commit();

        try {
            URI uri = new URI("/reservations/" + reservation.getReservationId());
            return Response.ok().location(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("{id}")
    public Response getReservation(@PathParam("id") long id){

        EntityManager em = PersistenceManager.instance().createEntityManager();

        em.getTransaction().begin();
        ReservationDTO reservationDTO = ReservationMapper.toDTO(em.find(Reservation.class, id));
        em.getTransaction().commit();
        em.close();

        if (reservationDTO == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(reservationDTO).build();
        }
    }

    //TODO get a reservation(s) by user?

}
