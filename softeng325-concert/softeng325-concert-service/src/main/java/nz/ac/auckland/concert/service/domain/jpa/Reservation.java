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

	@OneToOne(cascade = CascadeType.ALL)
	private ReservationRequest _request;

	@Column(name = "reservationTime")
	private LocalDateTime _reservationTime;

	@OneToMany(mappedBy = "_reservation", cascade = CascadeType.MERGE)
	private Set<Seat> _seats = new HashSet<>();

	public Reservation() {}

	// Sets the field reservation time to store the instant the reservation was created
	public Reservation(ReservationRequest request, Set<Seat> seats) {
		_request = request;
		_seats = new HashSet<Seat>(seats);

		for (Seat seat : _seats) {
			seat.setSeatStatus(SeatStatus.RESERVED);
		}

		_reservationTime = LocalDateTime.now();
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
	
}
