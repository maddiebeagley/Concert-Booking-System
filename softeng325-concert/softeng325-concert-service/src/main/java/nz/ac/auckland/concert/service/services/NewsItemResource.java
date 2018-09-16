package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.jpa.NewsItem;
import nz.ac.auckland.concert.service.domain.jpa.User;
import nz.ac.auckland.concert.service.mappers.NewsItemMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;

@Path("/newsItems")
@Produces(MediaType.WILDCARD)
@Consumes(MediaType.WILDCARD)
public class NewsItemResource {

    protected HashMap<String, AsyncResponse> listeners = new HashMap<>();

    /**
     * Registers a listener to the subscription service. They will be notified of any news items.
     */
    @GET
    public Response subscribe(@Suspended AsyncResponse response, @CookieParam("token") Cookie token) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            //check that the supplied token maps to a user in the DB
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            User user = query.getSingleResult();

            if (user == null) { //no user in the DB has the supplied token
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            em.getTransaction().commit();
            listeners.put(user.getUserName(), response);
            return Response.ok().build();

        } finally {
            em.close();
        }
    }

    @DELETE
    public Response unsubscribe(@CookieParam("token") Cookie token) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            //check that the supplied token maps to a user in the DB
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            User user = query.getSingleResult();

            if (user == null) { //no user in the DB has the supplied token
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //remove the authenticated user from the list of registered listeners
            if (listeners.containsKey(user.getUserName())) {
                listeners.remove(user.getUserName());
            }
            return Response.status(Response.Status.NO_CONTENT).build();
        } finally {
            em.close();
        }

    }


    /**
     * Sends out the most recent news item to the subscribed listeners.
     *
     * @param newsItemDTO: news item to circulate to listeners
     */
    @POST
    public Response send(NewsItemDTO newsItemDTO, @CookieParam("token") Cookie token) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();
            //store the new news item in the DB
            em.persist(NewsItemMapper.toDomain(newsItemDTO));
            em.getTransaction().commit();

            //send the news item to the registered listeners
            for (AsyncResponse asyncResponse : listeners.values()) {
                asyncResponse.resume(newsItemDTO);
            }
            return Response.ok().build();
        } finally {
            em.close();
        }
    }
}
