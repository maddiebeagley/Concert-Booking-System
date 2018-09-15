package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * DTO class to represent seats at the concert venue. 
 * 
 * A SeatDTO describes a seat in terms of:
 * _row    the row of the seat.
 * _number the number of the seat.
 *
 */
@Entity
@Table(name = "SEATS")
public class Seat {

	public enum SeatStatus {
		AVAILABLE, RESERVED, CONFIRMED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "seatId", nullable = false, unique = true)
	private Long _seatId;

	@Version
	@Column(name = "version")
	private long _version;

//	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	private Reservation _reservation;

	@Enumerated(EnumType.STRING)
	private PriceBand _seatType;

	@Column(name = "concertId", nullable = false)
	private Long _concertId;

	@Column(name = "concertDateTime", nullable = false)
	private LocalDateTime _concertDateTime;

	@Column(name = "row", nullable = false)
	private SeatRow _row;

	@Column(name = "number", nullable = false)
	private SeatNumber _number;

	@Enumerated(EnumType.STRING)
	@Column(name = "seatStatus", nullable = false)
	private SeatStatus _seatStatus;

	public Seat() {}

	public Seat(SeatRow row, SeatNumber number, Long concertId, LocalDateTime localDateTime) {
		_row = row;
		_number = number;
		_seatStatus = SeatStatus.AVAILABLE;
		_concertId = concertId;
		_concertDateTime = localDateTime;
	}
	
	public SeatRow getRow() {
		return _row;
	}
	
	public SeatNumber getNumber() {
		return _number;
	}
//
//	public Reservation getReservation() {
//		return _reservation;
//	}
//
//	public void setReservation(Reservation _reservation) {
//		this._reservation = _reservation;
//	}

	public SeatStatus getSeatStatus(){
		return _seatStatus;
	}

	public PriceBand getSeatType() {
		return _seatType;
	}

	public void setSeatType(PriceBand _seatType) {
		this._seatType = _seatType;
	}

	public long getVersion() {
		return _version;
	}

	public void setVersion(long _version) {
		this._version = _version;
	}

	public Long getConcertId(){
		return _concertId;
	}

	public LocalDateTime getConcertDateTime() {
		return _concertDateTime;
	}

	public void setConcertId(Long _concertId) {
		this._concertId = _concertId;
	}

	public void setConcertDateTime(LocalDateTime _concertDateTime) {
		this._concertDateTime = _concertDateTime;
	}

	public void setSeatStatus(SeatStatus seatStatus){
		_seatStatus = seatStatus;
	}


}
