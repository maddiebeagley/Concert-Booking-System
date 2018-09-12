package nz.ac.auckland.concert.service.services;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.jpa.User;
import nz.ac.auckland.concert.service.mappers.CreditCardMapper;
import nz.ac.auckland.concert.service.mappers.UserMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Path("/users")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class UserResource {

    @POST
    public Response createUser(UserDTO newUserDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        //checks that there are no other users with this username
        em.getTransaction().begin();
        User user = em.find(User.class, newUserDTO.getUserName());

        if (user != null) { //a user has been found with the input username, ie username is not unique
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }

        // code will only reach here and commit the desired new user if all criteria are met
        //a cookie is generated with a unique ID and assigned to the user for authentication purposes
        NewCookie newCookie = new NewCookie("token", UUID.randomUUID().toString());

        //generates a new user from the given values and sets its authentication token
        User newUser = UserMapper.toDomain(newUserDTO);
        newUser.setToken(newCookie.getValue());

        //new user is written to the DB
        em.persist(newUser);
        em.getTransaction().commit();

        try {
            //stores the URI location of the new user in the response
            URI uri = new URI("/users/" + newUserDTO.getUserName());
            return Response.created(uri).cookie(newCookie).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return Response.serverError().build();
        } finally {
            em.close();
        }
    }

    @POST
    @Path("/authenticate")
    public Response authenticateUser(UserDTO userDTO) {

        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {

            em.getTransaction().begin();
            User user = em.find(User.class, userDTO.getUserName());

            //there is no user with the specified user name
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            //check that supplied password matches password in DB
            if (!user.getPassword().equals(userDTO.getPassword())){
                return Response.status(Response.Status.EXPECTATION_FAILED).build();
            }

            em.getTransaction().commit();

            //store the user's current token in the response for authentication purposes
            NewCookie token = new NewCookie("token", user.getToken());
            //return the userDTO corresponding to DB user with all fields populated
            return Response.ok(UserMapper.toDTO(user)).cookie(token).build();

        } finally {
            em.close();
        }
    }

    @PUT
    @Path("/registerCard")
    public Response addCreditCard(CreditCardDTO creditCardDTO, @CookieParam("token") Cookie token) {

        if (token == null) { // user has not supplied a token so they are not authenticated.
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            //check that the supplied token maps to a user in the DB
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u._token = :tokenValue", User.class)
                    .setParameter("tokenValue", token.getValue());

            User user  = query.getSingleResult();

            if (user == null) { //no user in the DB has the supplied token
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            //update the credit card field of the supplied user
            user.setCreditCard(CreditCardMapper.toDomain(creditCardDTO));

            //merge and commit changes to the DB
            em.merge(user);
            em.getTransaction().commit();

            return Response.ok().build();
        } finally {
            em.close();
        }

    }

}