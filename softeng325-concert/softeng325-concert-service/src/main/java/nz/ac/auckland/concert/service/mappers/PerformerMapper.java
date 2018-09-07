package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.domain.jpa.Performer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PerformerMapper {

    public static PerformerDTO toDTO(Performer performer){

        if (performer == null) {
            return null;
        }

        Set<Long> concertIds = new HashSet<>();

        for (Concert concert : performer.getConcerts()) {
            concertIds.add(concert.getId());
        }

        return new PerformerDTO(
                performer.getPerformerId(),
                performer.getName(),
                "image",
                performer.getGenre(),
                concertIds
        );
    }

    public static List<PerformerDTO> toDTOList(List<Performer> performers) {
        List<PerformerDTO> performerDTOS = new ArrayList<>();

        for (Performer performer : performers) {
            performerDTOS.add(toDTO(performer));
        }

        return performerDTOS;
    }


}
