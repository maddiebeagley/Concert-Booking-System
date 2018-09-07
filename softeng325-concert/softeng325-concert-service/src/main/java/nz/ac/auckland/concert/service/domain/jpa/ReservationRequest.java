package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.jaxb.LocalDateTimeAdapter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * DTO class to represent reservation requests. 
 * 
 * A ReservationRequestDTO describes a request to reserve seats in terms of:
 * _numberOfSeats the number of seats to try and reserve.
 * _seatType      the priceband (A, B or C) in which to reserve the seats.
 * _concertId     the identity of the concert for which to reserve seats.
 * _date          the date/time of the concert for which seats are to be 
 *                reserved.
 *
 */
@Entity
public class ReservationRequest {

	@Id
	@GeneratedValue
	@Column(name = "reservationRequestId")
	private Long _reservationRequestId;

	@Column(name = "numberOfSeats")
	private int _numberOfSeats;

	@Column(name = "seatType")
	private PriceBand _seatType;

	@Column(name = "concertId")
	private Long _concertId;

	@Column(name = "date")
	private LocalDateTime _date;

	public ReservationRequest() {
	}

	public ReservationRequest(int numberOfSeats, PriceBand seatType, Long concertId, LocalDateTime date) {
		_numberOfSeats = numberOfSeats;
		_seatType = seatType;
		_concertId = concertId;
		_date = date;
	}

	public Long getReservationRequestId() {
		return _reservationRequestId;
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