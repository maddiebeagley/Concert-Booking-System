package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

/**
 * DTO class to represent seats at the concert venue. 
 * 
 * A SeatDTO describes a seat in terms of:
 * _row    the row of the seat.
 * _number the number of the seat.
 *
 */
@Embeddable
public class Seat {

	@Column(name = "row")
	private SeatRow _row;

	@Column(name = "number")
	private SeatNumber _number;

	public Seat() {}

	public Seat(SeatRow row, SeatNumber number) {
		_row = row;
		_number = number;
	}
	
	public SeatRow getRow() {
		return _row;
	}
	
	public SeatNumber getNumber() {
		return _number;
	}
}
