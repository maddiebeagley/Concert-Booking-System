package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO class to represent bookings (confirmed reservations). 
 * 
 * A BookingDTO describes a booking in terms of:
 * _concertId      the unique identifier for a concert.
 * _concertTitle   the concert's title.
 * _dateTime       the concert's scheduled date and time for which the booking 
 *                 applies.
 * _seats          the seats that have been booked (represented as a  Set of 
 *                 SeatDTO objects).
 * _priceBand      the price band of the booked seats (all seats are within the 
 *                 same price band).
 *
 */
@Entity
public class Booking {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long _id;

	@Column(name = "concertId")
	private Long _concertId;

	@Column(name = "concertTitle")
	private String _concertTitle;

	@Column(name = "dateTime")
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime _dateTime;

	@ElementCollection
	@Column(name = "seats")
	private Set<Seat> _seats;

	@Enumerated(EnumType.STRING)
	@Column(name = "priceBand")
	private PriceBand _priceBand;

	public Booking() {
	}

	public Booking(Long concertId, String concertTitle,
                   LocalDateTime dateTime, Set<Seat> seats, PriceBand priceBand) {
		_concertId = concertId;
		_concertTitle = concertTitle;
		_dateTime = dateTime;

		_seats = new HashSet<Seat>();
		_seats.addAll(seats);

		_priceBand = priceBand;
	}

	public Long getConcertId() {
		return _concertId;
	}

	public String getConcertTitle() {
		return _concertTitle;
	}

	public LocalDateTime getDateTime() {
		return _dateTime;
	}

	public Set<Seat> getSeats() {
		return Collections.unmodifiableSet(_seats);
	}

	public PriceBand getPriceBand() {
		return _priceBand;
	}

}
