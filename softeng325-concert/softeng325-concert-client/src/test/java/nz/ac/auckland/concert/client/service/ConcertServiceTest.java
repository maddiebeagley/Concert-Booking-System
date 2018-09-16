package nz.ac.auckland.concert.client.service;

import nz.ac.auckland.concert.common.dto.*;
import nz.ac.auckland.concert.common.message.Messages;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.NewsItem;
import nz.ac.auckland.concert.service.services.ConcertApplication;
import nz.ac.auckland.concert.utility.TheatreLayout;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests for a ConcertService implementation.
 * 
 * Prior to running each test, an embedded servlet container is started up that
 * hosts a named Web service. Immediately after each test, the servlet 
 * container is stopped. Testing is handled in this way so that the Web service
 * can reinitialise its database. Since an embedded H2 database can only have 
 * one connection at a time, it's not possible for the @After method in this 
 * Test class to connect to the H2 database to delete data - hence restarting
 * the Web service before each test allows the Web service to clear from the
 * database the effects of the tests. 
 * 
 * This Test class has 3 configurable properties:
 * 
 * - SERVER_PORT                        the port number on which the servlet 
 *                                      container listens for incoming HTTP 
 *                                      requests.
 * - WEB_SERVICE_CLASS_NAME             the name of the javax.ws.rs.core.Application
 *                                      subclass for the Web service 
 *                                      application.
 * - RESERVATION_EXPIRY_TIME_IN_SECONDS the expiry time for reservations.
 *
 */
public class ConcertServiceTest {

	private static final int SERVER_PORT = 10000;
	private static final String WEB_SERVICE_CLASS_NAME = ConcertApplication.class.getName();
	private static final int RESERVATION_EXPIRY_TIME_IN_SECONDS = 5;
	
	private static Client client;
	private static Server _server;
	
	private ConcertService _service;

	@BeforeClass
	public static void createClientAndServer() throws Exception {
		// Use ClientBuilder to create a new client that can be used to create
		// connections to the Web service.
		client = ClientBuilder.newClient();
		
		// Start the embedded servlet container and host the Web service.
		ServletHolder servletHolder = new ServletHolder(new HttpServletDispatcher());
		servletHolder.setInitParameter("javax.ws.rs.Application", WEB_SERVICE_CLASS_NAME);
		ServletContextHandler servletCtxHandler = new ServletContextHandler();
		servletCtxHandler.setContextPath("/services");
		servletCtxHandler.addServlet(servletHolder, "/");
		_server = new Server(SERVER_PORT);
		_server.setHandler(servletCtxHandler);
	}
	
	@AfterClass
	public static void shutDown() {
		client.close();
	}
	
	@Before 
	public void startServer() throws Exception {
		_server.start();
		_service = new DefaultService();
	}

	@After
	public void stopServer() throws Exception {
		_server.stop();
	}
	
	@Test
	public void testRetrieveConcerts() {
		final int numberOfConcerts = 25;
		
		Set<ConcertDTO> concerts = _service.getConcerts();
		assertEquals(numberOfConcerts, concerts.size());
	}
	
	@Test
	public void testRetrievePerformers() {
		final int numberOfPerformers = 20;
		
		Set<PerformerDTO> performers = _service.getPerformers();
		assertEquals(numberOfPerformers, performers.size());
	}
	
	@Test
	public void testCreateUser() {
		try {
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
		} catch(ServiceException e) {
			fail();
		}
	}
	
	@Test
	public void testCreateUserWithMissingField() {
		try {
			UserDTO userDTO = new UserDTO(null, "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			fail();
		} catch(ServiceException e) {
			assertEquals(Messages.CREATE_USER_WITH_MISSING_FIELDS, e.getMessage());
		}
	}
	
	@Test 
	public void testCreateUserWithDuplicateUsername() {
		boolean createdFirstUser = false;
		try {
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			createdFirstUser = true;
			
			userDTO = new UserDTO("Bulldog", "123", "Thatcher", "Margaret");
			_service.createUser(userDTO);
			fail();
		} catch(ServiceException e) {
			if(!createdFirstUser) {
				fail();
			}
			assertEquals(Messages.CREATE_USER_WITH_NON_UNIQUE_NAME, e.getMessage());
		}
	}
	
	@Test
	public void testAuthenticateUser() {
		try {
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			
			UserDTO credentials = new UserDTO("Bulldog", "123");
			UserDTO filledDTO = _service.authenticateUser(credentials);

			assertEquals(userDTO, filledDTO);
		} catch(ServiceException e) {
			fail();
		}
	}
	
	@Test
	public void testAuthenticateWithNonExistentUser() {
		try {
			UserDTO credentials = new UserDTO("Bulldog", "123");
			_service.authenticateUser(credentials);
			fail();
		} catch(ServiceException e) {
			assertEquals(Messages.AUTHENTICATE_NON_EXISTENT_USER, e.getMessage());
		}
	}
	
	@Test
	public void testAuthenticateUserWithIncorrectPassword() {
		try {
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			
			UserDTO credentials = new UserDTO("Bulldog", "987");
			_service.authenticateUser(credentials);
			fail();
		} catch(ServiceException e) {
			assertEquals(Messages.AUTHENTICATE_USER_WITH_ILLEGAL_PASSWORD, e.getMessage());
		}
	}
	
	@Test
	public void testAuthenticateUserWithMissingPassword() {
		try {
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			
			UserDTO credentials = new UserDTO("Bulldog", null);
			_service.authenticateUser(credentials);
			fail();
		} catch(ServiceException e) {
			assertEquals(Messages.AUTHENTICATE_USER_WITH_MISSING_FIELDS, e.getMessage());
		}
	}
	
	@Test
	public void testMakeReservation() {
		try {
			final int numberOfSeatsToBook = 2;
			
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);

			LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
			ReservationRequestDTO request = new ReservationRequestDTO(numberOfSeatsToBook, PriceBand.PriceBandC, 1L, dateTime);

			ReservationDTO reservation = _service.reserveSeats(request);

			ReservationRequestDTO requestFromResponse = reservation.getReservationRequest();
			assertEquals(request, requestFromResponse);
			
			Set<SeatDTO> reservedSeats = reservation.getSeats();
			assertEquals(numberOfSeatsToBook, reservedSeats.size());

			// Check that the seats reserved are of the required type.
			for(SeatDTO seat : reservedSeats) {
				assertTrue(TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandC).contains(seat.getRow()));
			}
			
		} catch(ServiceException e) {
			fail();
		}
	}
	
	@Test
	public void testMakeReservationWithBadRequest() {
		try {
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			
			LocalDateTime dateTime = LocalDateTime.of(2018, 2, 24, 17, 00);
			ReservationRequestDTO request = new ReservationRequestDTO(2, PriceBand.PriceBandC, 1L, dateTime);
			
			ReservationDTO reservation = _service.reserveSeats(request);
			fail();
		} catch(ServiceException e) {
			assertEquals(Messages.CONCERT_NOT_SCHEDULED_ON_RESERVATION_DATE, e.getMessage());
		}
	}
	
	@Test
	public void testMakeReservationWhereSeatsAreNotAvailable() {
		try {
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			
			Set<SeatRow> rowsOfStandardSeats = TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandB);
			int totalNumberOfStandardSeats = 0;
			for (SeatRow row : rowsOfStandardSeats) {
				totalNumberOfStandardSeats += TheatreLayout.getNumberOfSeatsForRow(row);
			}
			
			LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
			ReservationRequestDTO request = new ReservationRequestDTO(totalNumberOfStandardSeats, PriceBand.PriceBandB, 1L, dateTime);
			
			ReservationDTO reservation = _service.reserveSeats(request);
			
			ReservationRequestDTO requestFromResponse = reservation.getReservationRequest();
			assertEquals(request, requestFromResponse);
			
			Set<SeatDTO> reservedSeats = reservation.getSeats();
			assertEquals(totalNumberOfStandardSeats, reservedSeats.size());

			// Check that the seats reserved are of the required type.
			for(SeatDTO seat : reservedSeats) {
				assertTrue(TheatreLayout.getRowsForPriceBand(PriceBand.PriceBandB).contains(seat.getRow()));
			}

			// Attempt to reserve another seat.
			request = new ReservationRequestDTO(1, PriceBand.PriceBandB, 1L, dateTime);
			reservation = _service.reserveSeats(request);
			fail();
		} catch(ServiceException e) {
			assertEquals(Messages.INSUFFICIENT_SEATS_AVAILABLE_FOR_RESERVATION, e.getMessage());
		}	
	}
	
	@Test
	public void testMakeReservationWithUnauthenticatedUser() {
		try {
			final int numberOfSeatsToBook = 10;
			
			LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
			ReservationRequestDTO request = new ReservationRequestDTO(numberOfSeatsToBook, PriceBand.PriceBandA, 1L, dateTime);
			
			ReservationDTO reservation = _service.reserveSeats(request);
			fail();
		} catch(ServiceException e) {
			assertEquals(Messages.UNAUTHENTICATED_REQUEST, e.getMessage());
		}
	}
	
	@Test
	public void testConfirmReservation() {
		try {
			final int numberOfSeatsToBook = 5;
			
			// Create a User and register a credit card.
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			CreditCardDTO creditCard = new CreditCardDTO(CreditCardDTO.Type.Visa, "Winston Churchill", "4929-1500-0055-9544", LocalDate.of(2019, 7, 31));
			_service.registerCreditCard(creditCard);
			
			// Make a reservation.
			LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
			ReservationRequestDTO request = new ReservationRequestDTO(numberOfSeatsToBook, PriceBand.PriceBandC, 1L, dateTime);
			ReservationDTO reservation = _service.reserveSeats(request);
			
			// Confirm the reservation.
			_service.confirmReservation(reservation);
			
			// Make a request to check that this user has a booking.
			Set<BookingDTO> bookings = _service.getBookings();
			assertEquals(1, bookings.size());
			BookingDTO bookingDTO = bookings.iterator().next();
			assertEquals(new Long(1), bookingDTO.getConcertId());
			assertEquals(dateTime, bookingDTO.getDateTime());
			assertEquals(reservation.getSeats(), bookingDTO.getSeats());
			assertEquals(PriceBand.PriceBandC, bookingDTO.getPriceBand());
		} catch(ServiceException e) {
			fail();
		}
	}	
	
	@Test
	public void testConfirmBookingWithExpiredReservation() {
		try {
			final int numberOfSeatsToBook = 12;
			
			// Create a User and register a credit card.
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			CreditCardDTO creditCard = new CreditCardDTO(CreditCardDTO.Type.Visa, "Winston Churchill", "4929-1500-0055-9544", LocalDate.of(2019, 7, 31));
			_service.registerCreditCard(creditCard);
			
			// Make a reservation.
			LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
			ReservationRequestDTO request = new ReservationRequestDTO(numberOfSeatsToBook, PriceBand.PriceBandA, 1L, dateTime);
			ReservationDTO reservation = _service.reserveSeats(request);
			
			// Wait for the reservation to expire. 
			Thread.sleep(RESERVATION_EXPIRY_TIME_IN_SECONDS * 1000);
			
			// Attempt to confirm the reservation.
			_service.confirmReservation(reservation);
			fail();
		} catch(ServiceException e) {
			assertEquals(Messages.EXPIRED_RESERVATION, e.getMessage());
		} catch(InterruptedException e) {
			e.printStackTrace();
		} finally {
			// Make a request to check that this user doesn't have a booking.
			Set<BookingDTO> bookings = _service.getBookings();
			assertTrue(bookings.isEmpty());
		}
	}
	
	@Test
	public void testConfirmReservationWithoutRegisteredCreditCard() {
		try {
			final int numberOfSeatsToBook = 6;
			
			// Create a User.
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			
			// Make a reservation.
			LocalDateTime dateTime = LocalDateTime.of(2017, 2, 24, 17, 00);
			ReservationRequestDTO request = new ReservationRequestDTO(numberOfSeatsToBook, PriceBand.PriceBandB, 1L, dateTime);
			ReservationDTO reservation = _service.reserveSeats(request);
			
			// Attempt to confirm the reservation.
			_service.confirmReservation(reservation);
			fail();
		} catch(ServiceException e) {
			assertEquals(Messages.CREDIT_CARD_NOT_REGISTERED, e.getMessage());
		} finally {
			// Make a request to check that this user doesn't have a booking.
			Set<BookingDTO> bookings = _service.getBookings();
			assertTrue(bookings.isEmpty());
		}
	}

	@Test
	public void testRegisterCreditCard() {
		try {
			UserDTO userDTO = new UserDTO("Bulldog", "123", "Churchill", "Winston");
			_service.createUser(userDTO);
			
			CreditCardDTO creditCard = new CreditCardDTO(CreditCardDTO.Type.Visa, "Winston Churchill", "4929-1500-0055-9544", LocalDate.of(2019, 7, 31));
			_service.registerCreditCard(creditCard);
		} catch(ServiceException e) {
			fail();
		} 
	}
	
	@Test
	public void testRegisterCreditCardWithUnauthenticatedUser() {
		try {
			CreditCardDTO creditCard = new CreditCardDTO(CreditCardDTO.Type.Visa, "Winston Churchill", "4929-1500-0055-9544", LocalDate.of(2019, 7, 31));
			_service.registerCreditCard(creditCard);
			fail();
		} catch(ServiceException e) {
			assertEquals(Messages.UNAUTHENTICATED_REQUEST, e.getMessage());
		} 
	}

	/**
	 * retrieves the images associated to all performers stored in the DB.
	 */
	@Test
    public void testRetrieveImagesFromPerformers(){
	    try {
	        Set<PerformerDTO> performerDTOS = _service.getPerformers();

	        //get the performer images.
	        for (PerformerDTO performerDTO : performerDTOS) {
				_service.getImageForPerformer(performerDTO);
			}

        } catch (ServiceException e) {
            fail();
	    }
    }

	/**
	 * ensures that cached concerts are returned when the cache is still valid.
	 */
	@Test
	public void testRetrieveConcertsFromCache() {
		final int numberOfConcerts = 25;

		//retrieve from DB
		Set<ConcertDTO> concerts = _service.getConcerts();
		//retrieve from cache
		Set<ConcertDTO> cachedConcerts = _service.getConcerts();

		assertEquals(concerts, cachedConcerts);
		assertEquals(numberOfConcerts, concerts.size());
	}

	/**
	 * ensures that concerts are retrieved from the DB again after a cache timeout occurs.
	 */
	@Test
	public void testRetrieveConcertsFromCacheUpdate() {
		final int numberOfConcerts = 25;

		//retrieve from DB
		Set<ConcertDTO> concerts = _service.getConcerts();

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//retrieve from DB
		Set<ConcertDTO> cachedConcerts = _service.getConcerts();

		assertEquals(concerts, cachedConcerts);
		assertEquals(numberOfConcerts, concerts.size());
	}

	/**
	 * ensures that only the cached performers are returned from the client when the cache is
	 * still valid.
	 */
	@Test
	public void testRetrievePerformersFromCache() {
		final int numberOfPerformers = 20;

		//retrieve from DB
		Set<PerformerDTO> performers = _service.getPerformers();
		//retrieve from cache
		Set<PerformerDTO> performersCached = _service.getPerformers();

		assertEquals(performers, performersCached);
		assertEquals(numberOfPerformers, performers.size());
	}

	/**
	 * Ensures that concerts are retrieved from the DB after the cache timeout event has occurred.
	 */
	@Test
	public void testRetrievePerformersFromCacheUpdate() {
		final int numberOfPerformers = 20;

		//retrieve from DB
		Set<PerformerDTO> performers = _service.getPerformers();

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//retrieve from DB again since cache will timeout.
		Set<PerformerDTO> performersCached = _service.getPerformers();

		assertEquals(performers, performersCached);
		assertEquals(numberOfPerformers, performers.size());
	}

}
