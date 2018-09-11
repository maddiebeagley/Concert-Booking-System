package nz.ac.auckland.concert.service.domain.jpa;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO class to represent reservations. 
 * 
 * A ReservationDTO describes a reservation in terms of:
 * _id                 the unique identifier for a reservation.
 * _reservationRequest details of the corresponding reservation request, 
 *                     including the number of seats and their type, concert
 *                     identity, and the date/time of the concert for which a 
 *                     reservation was requested.
 * _seats              the seats that have been reserved (represented as a Set
 *                     of SeatDTO objects).
 *
 */
@Entity
public class Reservation {

	@Id
	@GeneratedValue
	@Column(name = "reservationId")
	private Long _reservationId;

	@Column(name = "userName")
	private String _userName;

	@OneToOne(cascade = CascadeType.ALL)
	private ReservationRequest _request;

	@Column(name = "reservationTime")
	private LocalDateTime _reservationTime;

	@ElementCollection
    @CollectionTable(name = "RESERVATION_SEATS", joinColumns = @JoinColumn(name="reservationId"))
	@Column(name = "seats", nullable = false)
	private Set<Seat> _seats = new HashSet<>();

	@Column(name = "confirmed")
	private boolean _confirmed;

	public Reservation() {}

	// Sets the field reservation time to store the instant the reservation was created
	public Reservation(ReservationRequest request, Set<Seat> seats, String userName) {
		_request = request;
		_seats = new HashSet<>(seats);
		_userName = userName;

		for (Seat seat : _seats) {
			seat.setSeatStatus(Seat.SeatStatus.RESERVED);
		}

		_reservationTime = LocalDateTime.now();
		_confirmed = false;
	}
	
	public Long getReservationId() {
		return _reservationId;
	}

	public LocalDateTime getReservationTime() {
		return _reservationTime;
	}

	public ReservationRequest getReservationRequest() {
		return _request;
	}
	
	public Set<Seat> getSeats() {
		return Collections.unmodifiableSet(_seats);
	}

	public boolean getConfirmed(){
		return _confirmed;
	}

	public void setConfirmed(boolean confirm) {
		_confirmed = confirm;

		if (confirm){
			for (Seat seat : _seats) {
				seat.setSeatStatus(Seat.SeatStatus.CONFIRMED);
			}
		}
	}

	/**
	 * This method should only be called when a reservation has expired. All associated seats should be made available
	 * to other reservations.
	 */
	public void freeSeats(){
		for (Seat seat : _seats){
			seat.setSeatStatus(Seat.SeatStatus.AVAILABLE);
		}
	}

}
