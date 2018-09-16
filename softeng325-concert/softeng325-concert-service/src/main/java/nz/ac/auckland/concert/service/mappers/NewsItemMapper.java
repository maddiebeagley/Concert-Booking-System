package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.NewsItemDTO;
import nz.ac.auckland.concert.service.domain.jpa.NewsItem;

public class NewsItemMapper {

    public static NewsItem toDomain(NewsItemDTO newsItemDTO){
        return new NewsItem(newsItemDTO.getTitle(),
                newsItemDTO.getMessage(),
                newsItemDTO.getPublicationDate());
    }

}
