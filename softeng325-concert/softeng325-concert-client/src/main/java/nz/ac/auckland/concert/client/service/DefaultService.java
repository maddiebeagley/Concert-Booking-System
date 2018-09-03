package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.jpa.*;
import nz.ac.auckland.concert.service.mappers.ReservationMapper;
import nz.ac.auckland.concert.service.mappers.SeatMapper;
import nz.ac.auckland.concert.service.util.TheatreUtility;
import org.hibernate.boot.jaxb.SourceType;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.Set;

public class DefaultService implements ConcertService {

    private static String CONCERT_WEB_SERVICE_URI = "http://localhost:10000/services/concerts";
    private static String RESERVATION_WEB_SERVICE_URI = "http://localhost:10000/services/reservations";
    private static String PERFORMER_WEB_SERVICE_URI = "http://localhost:10000/services/performers";
    private static String USER_WEB_SERVICE_URI = "http://localhost:10000/services/users";
    private static String BOOKING_WEB_SERVICE_URI = "http://localhost:10000/services/bookings";

    /**
     * Returns a Set of ConcertDTO objects, where each ConcertDTO instance
     * describes a concert.
     *
     * @throws ServiceException if there's an error communicating with the
     * service. The exception's message is Messages.SERVICE_COMMUNICATION_ERROR.
     *
     */
    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {

        Client client = ClientBuilder.newClient();

        // Make an invocation on a Concert URI and specify XML as the data return type
        Invocation.Builder builder = client.target(CONCERT_WEB_SERVICE_URI).request()
               .accept(MediaType.APPLICATION_XML);

        Response response = builder.get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            Set<ConcertDTO> concertDTOS = response.readEntity(new GenericType<Set<ConcertDTO>>() {});
            return concertDTOS;
        } else {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }

    /**
     * Returns a Set of PerformerDTO objects. Each member of the Set describes
     * a Performer.
     *
     * @throws ServiceException if there's an error communicating with the
     * service. The exception's message is Messages.SERVICE_COMMUNICATION_ERROR.
     *
     */
    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Client client = ClientBuilder.newClient();
        // Make an invocation on a Concert URI and specify XML as the data return type
        Invocation.Builder builder = client.target(PERFORMER_WEB_SERVICE_URI).request()
                .accept(MediaType.APPLICATION_XML);

        Response response = builder.get();

        if (response.getStatus() == 500) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } else {
            Set<PerformerDTO> performerDTOS = response.readEntity(new GenericType<Set<PerformerDTO>>() {});
            return performerDTOS;
        }
    }

    /**
     * Attempts to create a new user. When successful, the new user is
     * automatically authenticated and logged into the remote service.
     *
     * @param newUser a description of the new user. The following
     * properties are expected to be set: username, password, firstname
     * and lastname.
     *
     * @return a new UserDTO object, whose identity property is also set.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the expected UserDTO attributes are not set.
     * Messages.CREATE_USER_WITH_MISSING_FIELD
     *
     * Condition: the supplied username is already taken.
     * Messages.CREATE_USER_WITH_NON_UNIQUE_NAME
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        Client client = ClientBuilder.newClient();

        Invocation.Builder builder = client.target(USER_WEB_SERVICE_URI).request();

        //generate a new UserDTO with required information
        UserDTO userDTO = new UserDTO(newUser.getUsername(),
                newUser.getPassword(),
                newUser.getLastname(),
                newUser.getFirstname());

        Response response = builder.post(Entity.entity(userDTO,
                MediaType.APPLICATION_XML));

        if (response.getStatus() == Response.Status.OK.getStatusCode()){
            return userDTO;
            //TODO authorise new user
        } else if (response.getStatus() == Response.Status.PARTIAL_CONTENT.getStatusCode()) {
            throw new ServiceException(Messages.CREATE_USER_WITH_MISSING_FIELDS);
        } else if (response.getStatus() == Response.Status.EXPECTATION_FAILED.getStatusCode()) {
            throw new ServiceException(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME);
        } else {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }


    /**
     * Attempts to authenticate an existing user and log them into the remote
     * service.
     *
     * @param user stores the user's authentication credentials. Properties
     * username and password must be set.
     *
     * @return a UserDTO whose properties are all set.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the UserDTO parameter doesn't have values for username and/or
     * password.
     * Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS
     *
     * Condition: the remote service doesn't have a record of a user with the
     * specified username.
     * Messages.AUTHENTICATE_NON_EXISTENT_USER
     *
     * Condition: the given user can't be authenticated because their password
     * doesn't match what's stored in the remote service.
     * Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public UserDTO authenticateUser(UserDTO user) throws ServiceException {
        Client client = ClientBuilder.newClient();

        //ensures both username and password have been set
        if (user.getUsername() == null || user.getPassword() == null) {
            throw new ServiceException(Messages.CREATE_USER_WITH_MISSING_FIELDS);
        }

        // retrieve all users from DB to check their username fields
        Invocation.Builder builder = client.target(USER_WEB_SERVICE_URI).request().accept(MediaType.APPLICATION_XML);
        Response response = builder.get();
        Set<UserDTO> userDTOS = response.readEntity(new GenericType<Set<UserDTO>>() {});

        //ensures there has been no error when communicating with the server
        if (response.getStatus() == 500) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }

        boolean userNameSet = false;
        String dbPassword = null;

        //verifies that username of given user is in the DB
        for (UserDTO userDTO : userDTOS) {
            if (userDTO.getUsername().equals(user.getUsername())){
                userNameSet = true;
                dbPassword = userDTO.getPassword();
                break;
            }
        }

        //throws exeception if the user is not stored in the DB
        if (!userNameSet){
            throw new ServiceException(Messages.AUTHENTICATE_NON_EXISTENT_USER);
        }

        //checks that given password matches that of the DB
        if (!user.getPassword().equals(dbPassword)){
            throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD);
        }

        return user;
    }

    /**
     * Returns an Image for a given performer.
     *
     * @param performerDTO the performer for whom an Image is required.
     *
     * @return an Image instance.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: there is no image for the specified performer.
     * Messages.NO_IMAGE_FOR_PERFORMER
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public Image getImageForPerformer(PerformerDTO performerDTO) throws ServiceException {
        Client client = ClientBuilder.newClient();

        String performerURI = PERFORMER_WEB_SERVICE_URI + "/" + performerDTO.getId();

        //retrieve the performer from the DB with the given ID
        Invocation.Builder builder = client.target(performerURI).request()
                .accept(MediaType.APPLICATION_XML);

        Response response = builder.get();

        //verfies that server communication was correctly executed.
        if (response.getStatus() == 500) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }

        //retrieves image from performer
        Performer performer = response.readEntity(Performer.class);
        String image = performer.getImage();

        //checks that performer image is not null
        if (image == null) {
            throw new ServiceException(Messages.NO_IMAGE_FOR_PERFORMER);
        }

        //TODO retrieve Image
        return null;
    }

    //TODO THIS LOL.
    /**
     * Attempts to reserve seats for a concert. The reservation is valid for a
     * short period that is determine by the remote service.
     *
     * @param reservationRequestDTO a description of the reservation, including
     * number of seats, price band, concert identifier, and concert date. All
     * fields are expected to be filled.
     *
     * @return a ReservationDTO object that describes the reservation. This
     * includes the original ReservationDTO parameter plus the seats (a Set of
     * SeatDTO objects) that have been reserved.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * Condition: the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * Condition: the ReservationRequestDTO parameter is incomplete.
     * Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS
     *
     * Condition: the ReservationRequestDTO parameter specifies a reservation
     * date/time for when the concert is not scheduled.
     * Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE
     *
     * Condition: the reservation request is unsuccessful because the number of
     * seats within the required price band are unavailable.
     * Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequestDTO) throws ServiceException {

        Client client = ClientBuilder.newClient();

        //verifies that all required fields are not null
        if (reservationRequestDTO.getConcertId() == null ||
                reservationRequestDTO.getDate() == null ||
                reservationRequestDTO.getSeatType() == null ||
                reservationRequestDTO.getNumberOfSeats() == 0 ){
            throw new ServiceException(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS);
        }

        //TODO bad authentication token thing?

        //retrieve the concert to reserve tickets for from the DB
        Long concertId = reservationRequestDTO.getConcertId();
        String concertURL = CONCERT_WEB_SERVICE_URI + "/" + concertId;

        Invocation.Builder builder = client.target(concertURL).request()
                .accept(MediaType.APPLICATION_XML);

        Response response = builder.get();

        //Checks communication with server was as expected
        if (response.getStatus() == 500) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }

        Concert concert = response.readEntity(Concert.class);

        //check that the reservation date matches a date of the concert
        boolean validDate = false;

        for (LocalDateTime date : concert.getDates()) {
            if (date.equals(reservationRequestDTO.getDate())){
                validDate = true;
                break;
            }
        }

        if (!validDate) {
            throw new ServiceException(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE);
        }

        String bookingURL = BOOKING_WEB_SERVICE_URI + "/" + reservationRequestDTO.getSeatType();

        Invocation.Builder bookingBuilder = client.target(bookingURL).request()
                .accept(MediaType.APPLICATION_XML);

        Response bookingResponse = builder.get();

        Set<Seat> bookedSeats = bookingResponse.readEntity(new GenericType<Set<Seat>>() {});

        Set<SeatDTO> availableSeatDTOs = TheatreUtility.findAvailableSeats(
                reservationRequestDTO.getNumberOfSeats(),
                reservationRequestDTO.getSeatType(),
                SeatMapper.toDTOSet(bookedSeats)
        );

        Set<Seat> availableSeats = SeatMapper.toDomainSet(availableSeatDTOs);

        if (availableSeats.isEmpty()){
            throw new ServiceException(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION);
        } else{

            ReservationRequest reservationRequest = ReservationMapper.toRequestDomain(reservationRequestDTO);

            Reservation reservation = new Reservation(reservationRequest, availableSeats);
            Invocation.Builder reservationBuilder = client.target(RESERVATION_WEB_SERVICE_URI).request()
                    .accept(MediaType.APPLICATION_XML);

            Response reservationResponse = reservationBuilder.post(Entity.entity(reservation,
                    MediaType.APPLICATION_XML));

            if (reservationResponse.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()){
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }

            return ReservationMapper.toDTO(reservation);
        }
    }

    /**
     * Confirms a reservation. Prior to calling this method, a successful
     * reservation request should have been made via a call to reserveSeats(),
     * returning a ReservationDTO.
     *
     * @param reservation a description of the reservation to confirm.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * Condition: the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * Condition: the reservation has expired.
     * Messages.EXPIRED_RESERVATION
     *
     * Condition: the user associated with the request doesn't have a credit
     * card registered with the remote service.
     * Messages.CREDIT_CARD_NOT_REGISTERED
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public void confirmReservation(ReservationDTO reservation) throws ServiceException {

        Client client = ClientBuilder.newClient();
    }

    /**
     * Registers a credit card for the currently logged in user.
     *
     * @param creditCard a description of the credit card.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * Condition: the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        Client client = ClientBuilder.newClient();
        authenticateUser();

        Invocation.Builder builder = client.target(USER_WEB_SERVICE_URI).request()
                .accept(MediaType.APPLICATION_XML);

        Response response = builder.put(Entity.entity(creditCard,
                MediaType.APPLICATION_XML));


        if (response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }

    /**
     * Retrieves the bookings (confirmed reservations) for the currently
     * authenticated (logged in) user.
     *
     * @return a Set of BookingDTOs describing the bookings. Each BookingDTO
     * includes concert-identifying information, booking date, seats booked and
     * their price band.
     *
     * @throws ServiceException in response to any of the following conditions.
     * The exception's message is defined in
     * class nz.ac.auckland.concert.common.Messages.
     *
     * Condition: the request is made by an unauthenticated user.
     * Messages.UNAUTHENTICATED_REQUEST
     *
     * Condition: the request includes an authentication token but it's not
     * recognised by the remote service.
     * Messages.BAD_AUTHENTICATON_TOKEN
     *
     * Condition: there is a communication error.
     * Messages.SERVICE_COMMUNICATION_ERROR
     *
     */
    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        Client client = ClientBuilder.newClient();

        // Make an invocation on a Concert URI and specify XML as the data return type
        Invocation.Builder builder = client.target(BOOKING_WEB_SERVICE_URI).request()
                .accept(MediaType.APPLICATION_XML);

        Response response = builder.get();

        if (response.getStatus() == 500) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        } else {
            Set<BookingDTO> bookingDTOS = response.readEntity(new GenericType<Set<BookingDTO>>() {});
            return bookingDTOS;
        }
    }

    private void authenticateUser(){
        //TODO make sure current user is authenticated
    }

    //TODO make sure all methods start with the setup method.

}
