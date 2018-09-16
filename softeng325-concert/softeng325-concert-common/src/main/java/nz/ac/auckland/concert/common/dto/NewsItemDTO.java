package nz.ac.auckland.concert.common.dto;

import nz.ac.auckland.concert.common.jaxb.LocalDateAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

@XmlRootElement
public class NewsItemDTO {

    @XmlAttribute(name = "newsItemId")
    private Long _newsItemId;

    @XmlAttribute(name = "title")
    private String _title;

    @XmlAttribute(name = "message")
    private String _message;

    @XmlElement(name = "publicationDate")
    @XmlJavaTypeAdapter(value = LocalDateAdapter.class)
    private LocalDate _publicationDate;

    public NewsItemDTO(){
    }

    public NewsItemDTO(String title, String message, LocalDate publicationDate){
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
