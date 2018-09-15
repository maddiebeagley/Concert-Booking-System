package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.client.util.AWSUtil;
import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.service.domain.jpa.NewsItem;

import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class DefaultService implements ConcertService {

    private Set<PerformerDTO> _performerCache = new HashSet<>();
    private LocalDateTime _performerCacheExpiry = null;

    private Set<ConcertDTO> _concertCache = new HashSet<>();
    private LocalDateTime _concertCacheExpiry = null;

    private final static Logger LOGGER = Logger.getLogger(DefaultService.class.getName());

    // URLS to access resources
    private static String CONCERT_WEB_SERVICE_URI = "http://localhost:10000/services/concerts";
    private static String PERFORMER_WEB_SERVICE_URI = "http://localhost:10000/services/performers";
    private static String USER_WEB_SERVICE_URI = "http://localhost:10000/services/users";
    private static String RESERVATION_WEB_SERVICE_URI = "http://localhost:10000/services/reservations";
    private static String NEWS_ITEM_WEB_SERVICE_URI = "http://localhost:10000/services/newsItems";

    private Cookie _token;

    /**
     * Returns a Set of ConcertDTO objects, where each ConcertDTO instance
     * describes a concert.
     *
     * @throws ServiceException if there's an error communicating with the
     *                          service. The exception's message is Messages.SERVICE_COMMUNICATION_ERROR.
     */
    @Override
    public Set<ConcertDTO> getConcerts() throws ServiceException {

        Client client = ClientBuilder.newClient();

        try {
            if (_concertCacheExpiry == null || _concertCacheExpiry.isBefore(LocalDateTime.now())) {

                // Make an invocation on a Concert URI and specify XML as the data return type
                Invocation.Builder builder = client.target(CONCERT_WEB_SERVICE_URI).request()
                        .accept(MediaType.APPLICATION_XML);

                Response response = builder.get();

                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
                }

                Set<ConcertDTO> concertDTOS = response.readEntity(new GenericType<Set<ConcertDTO>>() {
                });

                CacheControl cacheControl = CacheControl.valueOf(response.getHeaderString(HttpHeaders.CACHE_CONTROL));

                int cacheTime = cacheControl.getMaxAge();
                LocalDateTime now = LocalDateTime.now();

                _concertCache = concertDTOS;
                _concertCacheExpiry = now.plus(Duration.ofSeconds(cacheTime));

                return concertDTOS;
            } else {
                return _concertCache;
            }
        } finally {
            client.close();
        }
    }

    /**
     * Returns a Set of PerformerDTO objects. Each member of the Set describes
     * a Performer.
     *
     * @throws ServiceException if there's an error communicating with the
     *                          service. The exception's message is Messages.SERVICE_COMMUNICATION_ERROR.
     */
    @Override
    public Set<PerformerDTO> getPerformers() throws ServiceException {
        Client client = ClientBuilder.newClient();

        try {
            if (_performerCacheExpiry == null || _performerCacheExpiry.isBefore(LocalDateTime.now())) {
                // Make an invocation on a Concert URI and specify XML as the data return type
                Invocation.Builder builder = client.target(PERFORMER_WEB_SERVICE_URI).request()
                        .accept(MediaType.APPLICATION_XML);

                Response response = builder.get();

                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
                }

                Set<PerformerDTO> performerDTOS = response.readEntity(new GenericType<Set<PerformerDTO>>() {
                });

                CacheControl cacheControl = CacheControl.valueOf(response.getHeaderString(HttpHeaders.CACHE_CONTROL));

                int cacheTime = cacheControl.getMaxAge();
                LocalDateTime now = LocalDateTime.now();

                _performerCache.addAll(performerDTOS);
                _performerCacheExpiry = now.plus(Duration.ofSeconds(cacheTime));

                return performerDTOS;
            } else {
                return _performerCache;
            }
        } finally {
            client.close();
        }

    }

    /**
     * Attempts to create a new user. When successful, the new user is
     * automatically authenticated and logged into the remote service.
     *
     * @param newUser a description of the new user. The following
     *                properties are expected to be set: username, password, firstname
     *                and lastname.
     * @return a new UserDTO object, whose identity property is also set.
     * @throws ServiceException in response to any of the following conditions.
     *                          The exception's message is defined in
     *                          class nz.ac.auckland.concert.common.Messages.
     *                          <p>
     *                          Condition: the expected UserDTO attributes are not set.
     *                          Messages.CREATE_USER_WITH_MISSING_FIELD
     *                          <p>
     *                          Condition: the supplied username is already taken.
     *                          Messages.CREATE_USER_WITH_NON_UNIQUE_NAME
     *                          <p>
     *                          Condition: there is a communication error.
     *                          Messages.SERVICE_COMMUNICATION_ERROR
     */
    @Override
    public UserDTO createUser(UserDTO newUser) throws ServiceException {
        Client client = ClientBuilder.newClient();

        try {
            // a request is only made if all required fields are set
            if (newUser.getUserName() == null || newUser.getPassword() == null ||
                    newUser.getFirstName() == null || newUser.getLastName() == null) {
                throw new ServiceException(Messages.CREATE_USER_WITH_MISSING_FIELDS);
            }

            //send HTTP request to create new user
            Invocation.Builder builder = client.target(USER_WEB_SERVICE_URI).request().accept(MediaType.APPLICATION_XML);
            Response response = builder.post(Entity.entity(newUser, MediaType.APPLICATION_XML));

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) { //user successfully created
                //current token is that of the newly added user
                _token = response.getCookies().get("token");
                return newUser;
            } else if (response.getStatus() == Response.Status.EXPECTATION_FAILED.getStatusCode()) {
                throw new ServiceException(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME);
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } finally {
            client.close();
        }
    }


    /**
     * Attempts to authenticate an existing user and log them into the remote
     * service.
     *
     * @param userDTO stores the user's authentication credentials. Properties
     *                username and password must be set.
     * @return a UserDTO whose properties are all set.
     * @throws ServiceException in response to any of the following conditions.
     *                          The exception's message is defined in
     *                          class nz.ac.auckland.concert.common.Messages.
     *                          <p>
     *                          Condition: the UserDTO parameter doesn't have values for username and/or
     *                          password.
     *                          Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS
     *                          <p>
     *                          Condition: the remote service doesn't have a record of a user with the
     *                          specified username.
     *                          Messages.AUTHENTICATE_NON_EXISTENT_USER
     *                          <p>
     *                          Condition: the given user can't be authenticated because their password
     *                          doesn't match what's stored in the remote service.
     *                          Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD
     *                          <p>
     *                          Condition: there is a communication error.
     *                          Messages.SERVICE_COMMUNICATION_ERROR
     */
    @Override
    public UserDTO authenticateUser(UserDTO userDTO) throws ServiceException {
        Client client = ClientBuilder.newClient();
        try {

            //request will only be generated if all required fields are set
            if (userDTO.getUserName() == null || userDTO.getPassword() == null) {
                throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS);
            }

            //authenticate input user by invoking a request
            String uri = USER_WEB_SERVICE_URI + "/authenticate";

            Invocation.Builder builder = client.target(uri).request();
            Response response = builder.post(Entity.entity(userDTO, MediaType.APPLICATION_XML));

            //there is no user with the given username
            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new ServiceException(Messages.AUTHENTICATE_NON_EXISTENT_USER);
            } else if (response.getStatus() == Response.Status.EXPECTATION_FAILED.getStatusCode()) {
                throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD);
            } else if (response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }

            userDTO = response.readEntity(UserDTO.class);
            _token = response.getCookies().get("token");
            return userDTO;
        } finally {
            client.close();
        }
    }

    /**
     * Returns an Image for a given performer.
     *
     * @param performerDTO the performer for whom an Image is required.
     * @return an Image instance.
     * @throws ServiceException in response to any of the following conditions.
     *                          The exception's message is defined in
     *                          class nz.ac.auckland.concert.common.Messages.
     *                          <p>
     *                          Condition: there is no image for the specified performer.
     *                          Messages.NO_IMAGE_FOR_PERFORMER
     *                          <p>
     *                          Condition: there is a communication error.
     *                          Messages.SERVICE_COMMUNICATION_ERROR
     */
    @Override
    public Image getImageForPerformer(PerformerDTO performerDTO) throws ServiceException {
        Client client = ClientBuilder.newClient();

        try {
            String performerURI = PERFORMER_WEB_SERVICE_URI + "/" + performerDTO.getId();

            //retrieve the performer from the DB with the given ID
            Invocation.Builder builder = client.target(performerURI).request()
                    .accept(MediaType.APPLICATION_XML);

            Response response = builder.get();

            //no performer has been found in the DB with the given details
            if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new ServiceException(Messages.NO_IMAGE_FOR_PERFORMER);
            } else if (response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }

            String imageName = performerDTO.getImageName();

            //checks that performer image is not null
            if (imageName == null) {
                throw new ServiceException(Messages.NO_IMAGE_FOR_PERFORMER);
            }

            return AWSUtil.downloadImageFromAWS(imageName);
        } finally {
            client.close();
        }
    }


    /**
     * Attempts to reserve seats for a concert. The reservation is valid for a
     * short period that is determine by the remote service.
     *
     * @param reservationRequestDTO a description of the reservation, including
     *                              number of seats, price band, concert identifier, and concert date. All
     *                              fields are expected to be filled.
     * @return a ReservationDTO object that describes the reservation. This
     * includes the original ReservationDTO parameter plus the seats (a Set of
     * SeatDTO objects) that have been reserved.
     * @throws ServiceException in response to any of the following conditions.
     *                          The exception's message is defined in
     *                          class nz.ac.auckland.concert.common.Messages.
     *                          <p>
     *                          Condition: the request is made by an unauthenticated user.
     *                          Messages.UNAUTHENTICATED_REQUEST
     *                          <p>
     *                          Condition: the request includes an authentication token but it's not
     *                          recognised by the remote service.
     *                          Messages.BAD_AUTHENTICATON_TOKEN
     *                          <p>
     *                          Condition: the ReservationRequestDTO parameter is incomplete.
     *                          Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS
     *                          <p>
     *                          Condition: the ReservationRequestDTO parameter specifies a reservation
     *                          date/time for when the concert is not scheduled.
     *                          Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE
     *                          <p>
     *                          Condition: the reservation request is unsuccessful because the number of
     *                          seats within the required price band are unavailable.
     *                          Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION
     *                          <p>
     *                          Condition: there is a communication error.
     *                          Messages.SERVICE_COMMUNICATION_ERROR
     */
    @Override
    public ReservationDTO reserveSeats(ReservationRequestDTO reservationRequestDTO) throws ServiceException {
        //if token not set, current user is not authenticated
        if (_token == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        //a request will not be generated unless all required fields are populated.
        if (reservationRequestDTO.getConcertId() == null || reservationRequestDTO.getDate() == null ||
                reservationRequestDTO.getSeatType() == null || reservationRequestDTO.getNumberOfSeats() == 0) {
            throw new ServiceException(Messages.RESERVATION_REQUEST_WITH_MISSING_FIELDS);
        }

        Client client = ClientBuilder.newClient();

        try {
            String uri = RESERVATION_WEB_SERVICE_URI + "/reserve";

            Invocation.Builder builder = client.target(uri).request()
                    .accept(MediaType.APPLICATION_XML).cookie(_token);

            Response response = builder.post(Entity.entity(reservationRequestDTO, MediaType.APPLICATION_XML));

            if (response.getStatus() == Response.Status.OK.getStatusCode()) { //reservation has successfully been created
                ReservationDTO reservationDTO = response.readEntity(ReservationDTO.class);
                return reservationDTO;
            } else if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
            } else if (response.getStatus() == Response.Status.EXPECTATION_FAILED.getStatusCode()) {
                throw new ServiceException(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE);
            } else if (response.getStatus() == Response.Status.NOT_ACCEPTABLE.getStatusCode()) {
                throw new ServiceException(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION);
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } finally {
            client.close();
        }
    }

    /**
     * Confirms a reservation. Prior to calling this method, a successful
     * reservation request should have been made via a call to reserveSeats(),
     * returning a ReservationDTO.
     *
     * @param reservationDTO a description of the reservation to confirm.
     * @throws ServiceException in response to any of the following conditions.
     *                          The exception's message is defined in
     *                          class nz.ac.auckland.concert.common.Messages.
     *                          <p>
     *                          Condition: the request is made by an unauthenticated user.
     *                          Messages.UNAUTHENTICATED_REQUEST
     *                          <p>
     *                          Condition: the request includes an authentication token but it's not
     *                          recognised by the remote service.
     *                          Messages.BAD_AUTHENTICATON_TOKEN
     *                          <p>
     *                          Condition: the reservation has expired.
     *                          Messages.EXPIRED_RESERVATION
     *                          <p>
     *                          Condition: the user associated with the request doesn't have a credit
     *                          card registered with the remote service.
     *                          Messages.CREDIT_CARD_NOT_REGISTERED
     *                          <p>
     *                          Condition: there is a communication error.
     *                          Messages.SERVICE_COMMUNICATION_ERROR
     */
    @Override
    public void confirmReservation(ReservationDTO reservationDTO) throws ServiceException {
        //if token not set, current user is not authenticated
        if (_token == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        Client client = ClientBuilder.newClient();
        try {

            String uri = RESERVATION_WEB_SERVICE_URI + "/confirm";

            Invocation.Builder builder = client.target(uri).request()
                    .accept(MediaType.APPLICATION_XML).cookie(_token);

            Response response = builder.post(Entity.entity(reservationDTO, MediaType.APPLICATION_XML));

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            } else if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
            } else if (response.getStatus() == Response.Status.GATEWAY_TIMEOUT.getStatusCode()) {
                throw new ServiceException(Messages.EXPIRED_RESERVATION);
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new ServiceException(Messages.CREDIT_CARD_NOT_REGISTERED);
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } finally {
            client.close();
        }
    }

    /**
     * Registers a credit card for the currently logged in user.
     *
     * @param creditCard a description of the credit card.
     * @throws ServiceException in response to any of the following conditions.
     *                          The exception's message is defined in
     *                          class nz.ac.auckland.concert.common.Messages.
     *                          <p>
     *                          Condition: the request is made by an unauthenticated user.
     *                          Messages.UNAUTHENTICATED_REQUEST
     *                          <p>
     *                          Condition: the request includes an authentication token but it's not
     *                          recognised by the remote service.
     *                          Messages.BAD_AUTHENTICATON_TOKEN
     *                          <p>
     *                          Condition: there is a communication error.
     *                          Messages.SERVICE_COMMUNICATION_ERROR
     */
    @Override
    public void registerCreditCard(CreditCardDTO creditCard) throws ServiceException {
        //if token not set, current user is not authenticated
        if (_token == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        Client client = ClientBuilder.newClient();

        try {

            String userURI = USER_WEB_SERVICE_URI + "/registerCard";

            Invocation.Builder builder = client.target(userURI).request(MediaType.APPLICATION_XML)
                    .accept(MediaType.APPLICATION_XML).cookie(_token);

            Response response = builder.put(Entity.entity(creditCard,
                    MediaType.APPLICATION_XML));

            if (response.getStatus() == Response.Status.OK.getStatusCode()){
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
            } else if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
                throw new ServiceException(Messages.BAD_AUTHENTICATON_TOKEN);
            } else {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } finally {
            client.close();
        }
    }

    /**
     * Retrieves the bookings (confirmed reservations) for the currently
     * authenticated (logged in) user.
     *
     * @return a Set of BookingDTOs describing the bookings. Each BookingDTO
     * includes concert-identifying information, booking date, seats booked and
     * their price band.
     * @throws ServiceException in response to any of the following conditions.
     *                          The exception's message is defined in
     *                          class nz.ac.auckland.concert.common.Messages.
     *                          <p>
     *                          Condition: the request is made by an unauthenticated user.
     *                          Messages.UNAUTHENTICATED_REQUEST
     *                          <p>
     *                          Condition: the request includes an authentication token but it's not
     *                          recognised by the remote service.
     *                          Messages.BAD_AUTHENTICATON_TOKEN
     *                          <p>
     *                          Condition: there is a communication error.
     *                          Messages.SERVICE_COMMUNICATION_ERROR
     */
    @Override
    public Set<BookingDTO> getBookings() throws ServiceException {
        //if token not set, current user is not authenticated
        if (_token == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        Client client = ClientBuilder.newClient();
        try {
            // Make an invocation on a Concert URI and specify XML as the data return type
            Invocation.Builder builder = client.target(RESERVATION_WEB_SERVICE_URI).request()
                    .accept(MediaType.APPLICATION_XML).cookie(_token);

            Response response = builder.get();

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }

            Set<BookingDTO> bookingDTOS = response.readEntity(new GenericType<Set<BookingDTO>>() {
            });
            return bookingDTOS;

        } finally {
            client.close();
        }

    }

    /**
     * subscribes an authenticated user to receive notifications of published news items.
     */
    public void subscribe() {
        //if token not set, current user is not authenticated
        if (_token == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        Client client = ClientBuilder.newClient();

        final WebTarget target = client.target(NEWS_ITEM_WEB_SERVICE_URI);

        target.request().cookie(_token).async().get(new InvocationCallback<NewsItem>() {
            @Override
            public void completed(NewsItem response) {
                target.request().async().get(this);
            }

            @Override
            public void failed(Throwable throwable) {

            }
        });

    }

    /**
     * Publishes news item, server side will send out the news item to all subscribed users.
     *
     * @param newsItem: to be sent to listeners
     */
    public void publishNewsItem(NewsItem newsItem) {
        //if token not set, current user is not authenticated
        if (_token == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

        Client client = ClientBuilder.newClient();

        try {
            Invocation.Builder builder = client.target(NEWS_ITEM_WEB_SERVICE_URI).request()
                    .accept(MediaType.APPLICATION_XML).cookie(_token);

            Response response = builder.post(Entity.entity(newsItem, MediaType.APPLICATION_XML));

            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
            }
        } finally {
            client.close();
        }
    }

}
