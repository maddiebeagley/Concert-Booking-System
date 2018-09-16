package nz.ac.auckland.concert.service.domain.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class NewsItem {

    @Id
    @GeneratedValue
    @Column(name = "newsItemId")
    private Long _newsItemId;

    @Column(name = "title")
    private String _title;

    @Column(name = "message")
    private String _message;

    @Column(name = "publicationDate")
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
