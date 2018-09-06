package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
	@Column(name = "id")
	private Long _id;

	@OneToOne
	private ReservationRequest _request;

	@Column(name = "reservationTime")
	private LocalDateTime _reservationTime;

	@ElementCollection
	@Column(name = "request")
	private Set<Seat> _seats = new HashSet<>();

	public Reservation() {}

	// Sets the field reservation time to store the instant the reservation was created
	public Reservation(ReservationRequest request, Set<Seat> seats) {
		_request = request;
		_seats = new HashSet<Seat>(seats);
		_reservationTime = LocalDateTime.now();
	}
	
	public Long getId() {
		return _id;
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
