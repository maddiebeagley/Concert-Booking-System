package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.Genre;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO class to represent performers. 
 * 
 * A PerformerDTO describes a performer in terms of:
 * _id         the unique identifier for a performer.
 * _name       the performer's name.
 * _imageName  the name of an image file for the performer.
 * _genre      the performer's genre.
 * _concertIds identification of each concert in which the performer is 
 *             playing. 
 *             
 */
@Entity
@Table(name = "PERFORMERS")
public class Performer {

	@Id
	@GeneratedValue
	@Column(name = "performerId", nullable = false)
	private Long _performerId;

	@Column(name = "name", nullable = false)
	private String _name;

	@Column(name = "imageName")
	private String _imageName;

	@Enumerated(EnumType.STRING)
	@Column(name = "genre")
	private Genre _genre;

	@ManyToMany(mappedBy = "_performers")
	@Column(name = "concerts", nullable = false)
	private Set<Concert> _concerts = new HashSet<>();

	public Performer() {
	}

	public Performer(Long id, String name, String image, Genre genre, Set<Concert> concerts) {
		_performerId = id;
		_name = name;
		_imageName = image;
		_genre = genre;
		_concerts = new HashSet<Concert>(concerts);
	}

	public Long getPerformerId() {
		return _performerId;
	}

	public String getName() {
		return _name;
	}

	public String getImageName() {
		return _imageName;
	}

	public Set<Concert> getConcerts() {
		return Collections.unmodifiableSet(_concerts);
	}

	public Genre getGenre() {
		return _genre;
	}
}