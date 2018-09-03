package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.PriceBand;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DTO class to represent concerts. 
 * 
 * A ConcertDTO describes a concert in terms of:
 * _id           the unique identifier for a concert.
 * _title        the concert's title.
 * _dates        the concert's scheduled dates and times (represented as a 
 *               Set of LocalDateTime instances).
 * _tariff       concert pricing - the cost of a ticket for each price band 
 *               (A, B and C) is set individually for each concert. 
 * _performerIds identification of each performer playing at a concert 
 *               (represented as a set of performer identifiers).
 *
 */
@Entity
public class Concert {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long _id;

	@Column(name = "title")
	private String _title;

	@Column(name = "dates")
	@Convert(converter=LocalDateTimeConverter.class)
	private Set<LocalDateTime> _dates;

	@ElementCollection
	@JoinTable(name = "CONCERT_TARIFS", joinColumns = @JoinColumn(name = "_id"))
	@MapKeyColumn(name = "price_band")
	@Column(name = "tariff", nullable = false)
	@MapKeyEnumerated(EnumType.STRING)
	private Map<PriceBand, BigDecimal> _tariff;

	@ManyToMany
	@ElementCollection
	@Column(name = "performers")
	private Set<Performer> _performers = new HashSet<>();

	public Concert() {
	}

	public Concert(Long id, String title, Set<LocalDateTime> dates,
                   Map<PriceBand, BigDecimal> ticketPrices, Set<Performer> performers) {
		_id = id;
		_title = title;
		_dates = new HashSet<LocalDateTime>(dates);
		_tariff = new HashMap<PriceBand, BigDecimal>(ticketPrices);
		_performers = new HashSet<Performer>(performers);
	}

	public Long getId() {
		return _id;
	}

	public String getTitle() {
		return _title;
	}

	public Set<LocalDateTime> getDates() {
		return Collections.unmodifiableSet(_dates);
	}

	public BigDecimal getTicketPrice(PriceBand seatType) {
		return _tariff.get(seatType);
	}

	public Set<Performer> getPerformers() {
		return Collections.unmodifiableSet(_performers);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().
            append(_title, rhs._title).
            append(_dates, rhs._dates).
            append(_tariff, rhs._tariff).
            append(_performers, rhs._performers).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_title).
	            append(_dates).
	            append(_tariff).
	            append(_performers).
	            hashCode();
	}
}
