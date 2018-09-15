package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.PriceBand;

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

    public enum ReservationStatus {
        RESERVED, CONFIRMED, EXPIRED;
    }

	@Id
	@GeneratedValue
	@Column(name = "reservationId")
	private Long _reservationId;

	@Column(name = "userName")
	private String _userName;

	@Column(name = "numberOfSeats")
	private int _numberOfSeats;

	@Column(name = "seatType")
	private PriceBand _seatType;

	@Column(name = "concertId")
	private Long _concertId;

	@Column(name = "concertDate")
	private LocalDateTime _date;

	@Column(name = "reservationTime")
	private LocalDateTime _reservationTime;

	@ElementCollection
    @CollectionTable(name = "RESERVATION_SEATS", joinColumns = @JoinColumn(name="reservationId"))
	@Column(name = "seats", nullable = false)
	private Set<Seat> _seats = new HashSet<>();

	@Column(name = "reservationStatus")
    @Enumerated(EnumType.STRING)
    private ReservationStatus _reservationStatus;

	public Reservation() {}

	// Sets the field reservation time to store the instant the reservation was created
	public Reservation(Set<Seat> seats, String userName, int numberOfSeats, PriceBand seatType,
                       long concertId, LocalDateTime date) {
		_seats = new HashSet<>(seats);
		_userName = userName;
        _reservationTime = LocalDateTime.now();
        _reservationStatus = ReservationStatus.RESERVED;
        _numberOfSeats = numberOfSeats;
        _seatType = seatType;
        _concertId = concertId;
        _date = date;
    }
	
	public Long getReservationId() {
		return _reservationId;
	}

	public LocalDateTime getReservationTime() {
		return _reservationTime;
	}

	public void setSeats(Set<Seat> seats) {
		_seats = seats;
	}

	public Set<Seat> getSeats() {
		return Collections.unmodifiableSet(_seats);
	}

	public ReservationStatus getReservationStatus(){
		return _reservationStatus;
	}

	public void setReservationStatus(ReservationStatus reservationStatus) {
        _reservationStatus = reservationStatus;
	}

	public int getNumberOfSeats() {
		return _numberOfSeats;
	}

	public PriceBand getSeatType() {
		return _seatType;
	}

	public Long getConcertId() {
		return _concertId;
	}

	public LocalDateTime getDate() {
		return _date;
	}

}
