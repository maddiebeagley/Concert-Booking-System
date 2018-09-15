package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.service.domain.jpa.NewsItem;
import nz.ac.auckland.concert.service.domain.jpa.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/newsItems")
@Produces(MediaType.WILDCARD)
@Consumes(MediaType.WILDCARD)
public class NewsItemResource {

    protected List<AsyncResponse> listeners = new ArrayList<>();

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

        } finally {
            em.close();
        }

        listeners.add(response);
        return Response.ok().build();
    }

    /**
     * Sends out the most recent news item to the subscribed listeners.
     * @param newsItem: news item to circulate to listeners
     */
    @POST
    public Response send(NewsItem newsItem, @CookieParam("token") Cookie token) {
        for (AsyncResponse asyncResponse : listeners) {
            asyncResponse.resume(newsItem);
        }
        return Response.ok().build();
    }
}
