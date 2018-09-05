package nz.ac.auckland.concert.client.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
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
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class DefaultService implements ConcertService {

    // AWS information to access performer images.
    private static final String AWS_ACCESS_KEY_ID = "AKIAJOG7SJ36SFVZNJMQ";
    private static final String AWS_SECRET_ACCESS_KEY = "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";
    private static final String AWS_BUCKET = "concert2.aucklanduni.ac.nz";

    // URLS to access resources
    private static String CONCERT_WEB_SERVICE_URI = "http://localhost:10000/services/concerts";
    private static String RESERVATION_WEB_SERVICE_URI = "http://localhost:10000/services/reservations";
    private static String PERFORMER_WEB_SERVICE_URI = "http://localhost:10000/services/performers";
    private static String USER_WEB_SERVICE_URI = "http://localhost:10000/services/users";
    private static String BOOKING_WEB_SERVICE_URI = "http://localhost:10000/services/bookings";

    private UserDTO _currentAuthUser;
    private Cookie _curAuthUserCookie;

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

        Response response = builder.post(Entity.entity(newUser,
                MediaType.APPLICATION_XML));

        if (response.getStatus() == Response.Status.OK.getStatusCode()){
            _currentAuthUser = newUser;
            return newUser;
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
     * @param inputUserDTO stores the user's authentication credentials. Properties
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
    public UserDTO authenticateUser(UserDTO inputUserDTO) throws ServiceException {
        Client client = ClientBuilder.newClient();

        //ensures both username and password have been set
        if (inputUserDTO.getUserName() == null || inputUserDTO.getPassword() == null) {
            throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS);
        }

        String userURI = USER_WEB_SERVICE_URI + "/" + inputUserDTO.getUserName();

        // retrieve all users from DB to check their username fields
        Invocation.Builder builder = client.target(userURI).request().accept(MediaType.APPLICATION_XML);
        Response response = builder.get();

        //there is no user with the given username
        if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()){
            throw new ServiceException(Messages.AUTHENTICATE_NON_EXISTENT_USER);
        }

        //ensures there has been no error when communicating with the server
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }


        UserDTO DBUserDTO = response.readEntity(UserDTO.class);

        //check if supplied password matches the password stored in the DB
        if (!DBUserDTO.getPassword().equals(inputUserDTO.getPassword())) {
            throw new ServiceException(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD);
        }

        //set the remaining fields of the DTO object
        if (inputUserDTO.getFirstName() == null) {
            inputUserDTO.setFirstName(DBUserDTO.getFirstName());
        }
        if (inputUserDTO.getLastName() == null) {
            inputUserDTO.setLastName(DBUserDTO.getLastName());
        }

        System.out.println("output DTO User from authenticateUser method: " + inputUserDTO.toString());

        _currentAuthUser = inputUserDTO;
        return inputUserDTO;
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

        //verifies that server communication was correctly executed.
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            //retrieves image from performer
            Performer performer = response.readEntity(Performer.class);
            String imageName = performer.getImageName();

            //checks that performer image is not null
            if (imageName == null) {
                throw new ServiceException(Messages.NO_IMAGE_FOR_PERFORMER);
            } else {
                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                        AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
                AmazonS3 s3 = AmazonS3ClientBuilder
                        .standard()
                        .withRegion(Regions.AP_SOUTHEAST_2)
                        .withCredentials(
                                new AWSStaticCredentialsProvider(awsCredentials))
                        .build();

                // Download the images.
//                return download(s3, imageName);
                //TODO configure download method

                return null;
            }

        } else {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }
    }


    /**
     * Downloads images in the bucket named AWS_BUCKET.
     *
     * @param s3         the AmazonS3 connection.
     * @param imageName the named image to download.
     */
    private static Image download(AmazonS3 s3, String imageName) {
//
//        File downloadDirectory = new File(DOWNLOAD_DIRECTORY);
//        System.out.println("Will download " + imageName.size() + " files to: " + downloadDirectory.getPath());
//
//        TransferManager mgr = TransferManagerBuilder
//                .standard()
//                .withS3Client(s3)
//                .build();
//
//        File imageFile = new File(downloadDirectory, imageName);
//
//        if (imageFile.exists()) {
//            imageFile.delete();
//        }
//
//            try {
//                S3Object o = s3.getObject(AWS_BUCKET, AWS_SECRET_ACCESS_KEY);
//                S3ObjectInputStream s3is = o.getObjectContent();
//                FileOutputStream fos = new FileOutputStream(new File(AWS_SECRET_ACCESS_KEY));
//                byte[] read_buf = new byte[1024];
//                int read_len = 0;
//                while ((read_len = s3is.read(read_buf)) > 0) {
//                    fos.write(read_buf, 0, read_len);
//                }
//                s3is.close();
//                fos.close();
//            } catch (AmazonServiceException e) {
//                System.err.println(e.getErrorMessage());
//                System.exit(1);
//            } catch (FileNotFoundException e) {
//                System.err.println(e.getMessage());
//                System.exit(1);
//            } catch (IOException e) {
//                System.err.println(e.getMessage());
//                System.exit(1);
//            }

//
//            System.out.print("Downloading " + imageName + "... ");
//            GetObjectRequest req = new GetObjectRequest(AWS_BUCKET, imageName);
//            Image image = s3.getObject(req, imageFile);
//            s3.getObject();
//            System.out.println("Complete!");

            //TODO remove placeholder
            return null;
        }


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

        if (_currentAuthUser == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }

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
        String concertURI = CONCERT_WEB_SERVICE_URI + "/" + concertId;

        Invocation.Builder builder = client.target(concertURI).request()
                .accept(MediaType.APPLICATION_XML);

        Response response = builder.get();

        //Checks communication with server was as expected
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }

        ConcertDTO concertDTO = response.readEntity(ConcertDTO.class);

        //check that the reservation date matches a date of the concert
        boolean validDate = false;

        for (LocalDateTime date : concertDTO.getDates()) {
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

        Response bookingResponse = bookingBuilder.get();

        if (bookingResponse.getStatus() != Response.Status.OK.getStatusCode()){
            throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
        }

        Set<BookingDTO> bookingDTOs = bookingResponse.readEntity(new GenericType<Set<BookingDTO>>() {});

//        Set<SeatDTO> bookedSeats = bookingDTO.getSeats();
//
        //TODO use methods properly! need to book a subset of available seats!
//        Set<SeatDTO> availableSeatDTOs = TheatreUtility.findAvailableSeats(
//                reservationRequestDTO.getNumberOfSeats(),
//                reservationRequestDTO.getSeatType(),
//                bookedSeats
//        );
//
//        Set<Seat> availableSeats = SeatMapper.toDomainSet(availableSeatDTOs);
//
//        if (availableSeats.isEmpty()){
//            throw new ServiceException(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION);
//        } else{
//
//            ReservationRequest reservationRequest = ReservationMapper.toRequestDomain(reservationRequestDTO);
//
//            Reservation reservation = new Reservation(reservationRequest, availableSeats);
//            Invocation.Builder reservationBuilder = client.target(RESERVATION_WEB_SERVICE_URI).request()
//                    .accept(MediaType.APPLICATION_XML);
//
//            Response reservationResponse = reservationBuilder.post(Entity.entity(reservation,
//                    MediaType.APPLICATION_XML));
//
//            if (reservationResponse.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()){
//                throw new ServiceException(Messages.SERVICE_COMMUNICATION_ERROR);
//            }
//
//            return ReservationMapper.toDTO(reservation);
//        }

        return null;
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
        if (_currentAuthUser == null) {
            throw new ServiceException(Messages.UNAUTHENTICATED_REQUEST);
        }
        Client client = ClientBuilder.newClient();

        authenticateUser();

        //TODO make this user name of current user
        String userURI = USER_WEB_SERVICE_URI + "/" + _currentAuthUser.getUserName();

        Invocation.Builder builder = client.target(userURI).request()
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
