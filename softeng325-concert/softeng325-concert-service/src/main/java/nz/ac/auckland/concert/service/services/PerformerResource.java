package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.service.domain.jpa.Performer;
import nz.ac.auckland.concert.service.mappers.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/performers")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class PerformerResource {

    //caches should only be stored for 10 second intervals
    private int cacheTime = 10;

    @GET
    @Path("{id}")
    public Response getPerformer(@PathParam("id") long id) {
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            //find the performer with the supplied ID
            PerformerDTO performerDTO = PerformerMapper.toDTO(em.find(Performer.class, id));

            em.getTransaction().commit();

            if (performerDTO == null) { //there is no performer in the DB with the supplied ID
                return Response.status(Response.Status.NOT_FOUND).build();
            } else { // return the transferrable performer object to the client
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

            //find all performers in the DB and convert them to their transferrable DTO form
            TypedQuery<Performer> query = em.createQuery("SELECT p FROM Performer p", Performer.class);
            List<PerformerDTO> performerDTOS = PerformerMapper.toDTOList(query.getResultList());
            GenericEntity<List<PerformerDTO>> ge = new GenericEntity<List<PerformerDTO>>(performerDTOS) {
            };

            em.getTransaction().commit();

            CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(cacheTime);

            //return the DTO performer objects to the client
            return Response.ok(ge).cacheControl(cacheControl).build();

        } finally {
            em.close();
        }
    }

}
