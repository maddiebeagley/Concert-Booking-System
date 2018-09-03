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
public class Performer {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long _id;

	@Column(name = "name")
	private String _name;

	@Column(name = "image")
	private String _imageName;

	@Enumerated
	@Column(name = "genre")
	private Genre _genre;

	@ManyToMany
	@Column(name = "concerts")
	private Set<Concert> _concerts = new HashSet<>();

	public Performer() {}

	public Performer(Long id, String name, String image, Genre genre, Set<Concert> concerts) {
		_id = id;
		_name = name;
		_imageName = image;
		_genre = genre;
		_concerts = new HashSet<Concert>(concerts);
	}
	
	public Long getId() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getImage() {
		return _imageName;
	}
	
	public Set<Concert> getConcerts() {
		return Collections.unmodifiableSet(_concerts);
	}

    public Genre getGenre() {
        return _genre;
    }

    @Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Performer))
            return false;
        if (obj == this)
            return true;

        Performer rhs = (Performer) obj;
        return new EqualsBuilder().
            append(_name, rhs._name).
            append(_imageName, rhs._imageName).
            append(_genre, rhs._genre).
            append(_concerts, rhs._concerts).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_name).
	            append(_imageName).
	            append(_genre).
	            append(_concerts).
	            hashCode();
	}
}
