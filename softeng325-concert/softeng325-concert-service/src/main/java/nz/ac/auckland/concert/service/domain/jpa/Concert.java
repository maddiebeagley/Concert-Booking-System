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
@Table(name = "CONCERTS")
public class Concert {

	@Id
	@GeneratedValue
	@Column(name = "id", nullable = false, unique = true)
	private Long _id;

	@Column(name = "title", nullable = false)
	private String _title;

	@ElementCollection
	@CollectionTable(name = "CONCERT_DATES", joinColumns = @JoinColumn(name="id"))
	@Column(name = "dates")
	private Set<LocalDateTime> _dates = new HashSet<LocalDateTime>();

	@ElementCollection
	@JoinTable(name = "CONCERT_TARIFS",
			joinColumns = @JoinColumn(name = "concertId"))
	@MapKeyColumn(name = "price_band")
	@Column(name = "tariff", nullable = false)
	@MapKeyEnumerated(EnumType.STRING)
	private Map<PriceBand, BigDecimal> _tariff = new HashMap<PriceBand, BigDecimal>();

	@ManyToMany
	@ElementCollection
	@JoinTable(name = "CONCERT_PERFORMER",
			joinColumns = @JoinColumn(name = "concertId"),
			inverseJoinColumns = @JoinColumn(name = "performerId"))
	@Column(name = "performer", nullable = false, unique = true)
	private Set<Performer> _performers = new HashSet<Performer>();

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

	public void set_id(Long _id) {
		this._id = _id;
	}

	public void set_title(String _title) {
		this._title = _title;
	}

	public void set_dates(Set<LocalDateTime> _dates) {
		this._dates = _dates;
	}

	public void set_tariff(Map<PriceBand, BigDecimal> _tariff) {
		this._tariff = _tariff;
	}

	public void set_performers(Set<Performer> _performers) {
		this._performers = _performers;
	}
}
