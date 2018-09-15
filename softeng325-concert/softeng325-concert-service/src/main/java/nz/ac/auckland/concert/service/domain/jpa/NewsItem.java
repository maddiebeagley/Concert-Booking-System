package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.jaxb.LocalDateAdapter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

@Entity
@XmlRootElement
public class NewsItem {

    @Id
    @GeneratedValue
    @XmlAttribute(name = "newsItemId")
    @Column(name = "newsItemId")
    private Long _newsItemId;

    @Column(name = "title")
    @XmlAttribute(name = "title")
    private String _title;

    @Column(name = "message")
    @XmlAttribute(name = "message")
    private String _message;

    @Column(name = "publicationDate")
    @XmlElement(name = "publicationDate")
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate _publicationDate;

    public NewsItem(){};

    public NewsItem(String title, String message, LocalDate publicationDate){
        _title = title;
        _message = message;
        _publicationDate = publicationDate;
    }

    public String getTitle() {
        return _title;
    }

    public String getMessage() {
        return _message;
    }

    public LocalDate getPublicationDate() {
        return _publicationDate;
    }
}
