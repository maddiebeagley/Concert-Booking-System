package nz.ac.auckland.concert.service.domain.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

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
    private LocalDateTime _publicationDate;

    public NewsItem(){};

    public NewsItem(String title, String message, LocalDateTime publicationDate){
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

    public LocalDateTime getPublicationDate() {
        return _publicationDate;
    }
}
