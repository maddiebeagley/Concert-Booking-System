package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.PriceBand;

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
 * _concertDateTime       the concert's scheduled date and time for which the booking
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
	@Column(name = "bookingId", nullable = false, unique = true)
	private Long _bookingId;

	@Column(name = "concertId")
	private Long _concertId;

	@Column(name = "concertTitle")
	private String _concertTitle;

	@Column(name = "_concertDateTime")
	private LocalDateTime _concertDateTime;

	@ElementCollection
	private Set<Seat> _seats = new HashSet<>();

	@Column(name = "numberOfSeats", nullable = false)
	private int _numberOfSeats;

	@Enumerated(EnumType.STRING)
	@Column(name = "priceBand", nullable = false)
	private PriceBand _priceBand;

    @Column(name = "timeStamp", nullable = false)
    private LocalDateTime _timeStamp;

    @Column(name = "confirmed", nullable = false)
	private boolean _confirmed;

	public Booking() {
	}

	public Booking(Long concertId, String concertTitle, LocalDateTime dateTime, Set<Seat> seats, int numberOfSeats, PriceBand priceBand) {
		_concertId = concertId;
		_concertTitle = concertTitle;
		_concertDateTime = dateTime;
        _numberOfSeats = numberOfSeats;
        _priceBand = priceBand;

        //when a booking is created all seats should be reserved
        for (Seat seat : seats) {
            seat.setSeatStatus(Seat.SeatStatus.RESERVED);
            _seats.add(seat);
        }

		//stores the instance the booking is made
		_timeStamp = LocalDateTime.now();
        //a booking must explicitly be confirmed
		_confirmed = false;
	}

    public Long getBookingId() {
        return _bookingId;
    }

    public Long getConcertId() {
		return _concertId;
	}

    public String getConcertTitle() {
        return _concertTitle;
    }

    public LocalDateTime getConcertDateTime() {
		return _concertDateTime;
	}

	public Set<Seat> getSeats() {
		return Collections.unmodifiableSet(_seats);
	}

	public PriceBand getPriceBand() {
		return _priceBand;
	}

    public int getNumberOfSeats() {
        return _numberOfSeats;
    }

    public LocalDateTime getTimeStamp() {
        return _timeStamp;
    }

    public boolean getConfirmed(){
        return _confirmed;
    }

    /*
     * When a booking is confirmed, all seats in the booking should be set to confirmed.
     */
    public void setConfirmed(boolean confirm) {
        _confirmed = confirm;

        if (confirm){
            for (Seat seat : _seats) {
                seat.setSeatStatus(Seat.SeatStatus.CONFIRMED);
            }
        }
    }

}
