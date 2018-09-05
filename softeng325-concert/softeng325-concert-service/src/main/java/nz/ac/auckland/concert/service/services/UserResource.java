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
import java.util.List;
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

        //a user has been found with the input username, ie username is not unique
        if (user != null) {
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }

        // code will only reach here and commit the desired new user if all criteria are met
        //a cookie is generated with a unique ID and assigned to the user
        NewCookie newCookie = new NewCookie("token", UUID.randomUUID().toString());
        User newUser = UserMapper.toDomain(newUserDTO);
        newUser.setToken(newCookie.getValue());

        //new user is written to the DB
        em.persist(newUser);
        em.getTransaction().commit();

        try {
            //stores the URI location of the new user in the response
            URI uri = new URI("/users/" + newUserDTO.getUserName());
            return Response.created(uri).build();
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
            GenericEntity<List<UserDTO>> ge = new GenericEntity<List<UserDTO>>(userDTOS) {
            };

            em.getTransaction().commit();

            return Response.ok(ge).build();

        } finally {
            em.close();
        }
    }

    @POST
    public Response authenticateUser(UserDTO userDTO) {

        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {

            em.getTransaction().begin();
            User user = em.find(User.class, userDTO.getUserName());

            //there is no user with the specified user name
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String dbPassword = user.getPassword();
            String inputPassWord = userDTO.getPassword();

            //check that supplied password matches password in DB
            if (!dbPassword.equals(inputPassWord)){
                return Response.status(Response.Status.EXPECTATION_FAILED).build();
            }

            em.getTransaction().commit();

            //return the userDTO corresponding to DB user with all fields populated
            return Response.ok(UserMapper.toDTO(user)).build();

        } finally {
            em.close();
        }
    }


    @GET
    @Path("{userName}")
    public Response authenticateUser(@PathParam("userName") String userName) {

        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {

            em.getTransaction().begin();
            User user = em.find(User.class, userName);

            //there is no user with the specified user name
            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }


            em.getTransaction().commit();

            UserDTO userDTO = UserMapper.toDTO(user);
            return Response.ok(userDTO).build();

        } finally {
            em.close();
        }
    }


    @PUT
    @Path("{userName}")
    public Response addCreditCard(@PathParam("userName") String userName, CreditCardDTO creditCardDTO) {
        EntityManager em = PersistenceManager.instance().createEntityManager();

        try {
            em.getTransaction().begin();

            User user = em.find(User.class, userName);

            if (user == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
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