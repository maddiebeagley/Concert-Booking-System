package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.mappers.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/concerts")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class ConcertResource {

    @GET
    @Path("{id}")
    public Response getConcert(@PathParam("id") long id) {
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            // Start a new transaction.
            em.getTransaction().begin();
            ConcertDTO concertDTO = ConcertMapper.toDTO(em.find(Concert.class, id));
            em.getTransaction().commit();

            if (concertDTO == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                return Response.ok(concertDTO).build();
            }
        } finally {
            em.close();
        }
    }

    @GET
    public Response getAllConcerts() {
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            // Start a new transaction.
            em.getTransaction().begin();

            TypedQuery<Concert> query = em.createQuery("SELECT c FROM Concert c", Concert.class);
            List<ConcertDTO> concertDTOs = ConcertMapper.toDTOList(query.getResultList());
            GenericEntity<List<ConcertDTO>> ge = new GenericEntity<List<ConcertDTO>>(concertDTOs) {};

            return Response.ok(ge).build();

        } finally {
            em.close();
        }
    }

    //TODO consider adding a get method for concerts on a certain date or in a date range
    //TODO consider adding a get method for concerts with a certain performer
}
