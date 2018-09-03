package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.jpa.Booking;
import nz.ac.auckland.concert.service.domain.jpa.User;
import nz.ac.auckland.concert.service.mappers.BookingMapper;
import nz.ac.auckland.concert.service.mappers.CreditCardMapper;
import nz.ac.auckland.concert.service.mappers.UserMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Path("/users")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class UserResource {

    @POST
    public Response createUser(UserDTO newUser) {
        // check that all required fields have been set
        if (newUser.getUsername() == null ||
                newUser.getPassword() == null ||
                newUser.getFirstname() == null ||
                newUser.getLastname() == null) {
            return Response.status(Response.Status.PARTIAL_CONTENT).build();
        }

        EntityManager em = PersistenceManager.instance().createEntityManager();

        //find all users to check that given username is unique
        TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
        List<User> users = query.getResultList();

        for (User user : users) {
            if (user.getUsername().equals(newUser.getUsername())){
                return Response.status(Response.Status.EXPECTATION_FAILED).build();
            }
        }

        // code will only reach here and commit the desired new user if all criteria are met
        em.getTransaction().begin();
        em.persist(UserMapper.toDomain(newUser));
        em.getTransaction().commit();

        try {
            URI uri = new URI("/users/" + newUser.getUsername());
            return Response.ok().location(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @GET
    public Response getAllUsers() {
        // Acquire an EntityManager (creating a new persistence context).
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            // Start a new transaction.
            em.getTransaction().begin();

            TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
            List<UserDTO> userDTOS = UserMapper.toDTOList(query.getResultList());
            GenericEntity<List<UserDTO>> ge = new GenericEntity<List<UserDTO>>(userDTOS) {};

            em.getTransaction().commit();

            return Response.ok(ge).build();

        } finally {
            em.close();
        }
    }


    @PUT
    @Path("{username}")
    public Response addCreditCard(@PathParam("username") String userName, CreditCardDTO creditCardDTO){
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            User toUpdate = em.find(User.class, userName);
            if (toUpdate == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            //update the credit card field of the supplied user
            toUpdate.setCreditCard(CreditCardMapper.toDomain(creditCardDTO));

            //merge and commit changes to the DB
            em.merge(toUpdate);
            em.getTransaction().commit();

            return Response.ok().build();
        } finally {
            em.close();
        }

    }
}