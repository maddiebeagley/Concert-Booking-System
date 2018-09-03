package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.service.domain.jpa.Performer;
import nz.ac.auckland.concert.service.mappers.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/performers")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class PerformerResource {


    @GET
    @Path("{id}")
    public Response getPerformer(@PathParam("id") long id) {
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            // Start a new transaction.
            em.getTransaction().begin();

            // Use the EntityManager to retrieve, persist or delete object(s).
            PerformerDTO performerDTO = PerformerMapper.toDTO(em.find(Performer.class, id));

            // Commit the transaction.
            em.getTransaction().commit();

            if (performerDTO == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                return Response.ok(performerDTO).build();
            }
        } finally {
            em.close();
        }
    }

    @GET
    public Response getAllPerformers() {
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            // Start a new transaction.
            em.getTransaction().begin();

            TypedQuery<Performer> query = em.createQuery("SELECT p FROM Performer p", Performer.class);

            List<PerformerDTO> performerDTOS = PerformerMapper.toDTOList(query.getResultList());

            GenericEntity<List<PerformerDTO>> ge = new GenericEntity<List<PerformerDTO>>(performerDTOS) {
            };

            return Response.ok(ge).build();

        } finally {
            em.close();
        }
    }

    //TODO consider getting performers by name (first or last?)

}
