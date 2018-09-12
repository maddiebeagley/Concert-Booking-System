package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.PerformerDTO;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.domain.jpa.Performer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple helper class for converting to and from DTO and domain instances.
 */
public class PerformerMapper {

    /**
     * Converts a domain instance of a performer into a DTO instance
     */
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
                performer.getImageName(),
                performer.getGenre(),
                concertIds
        );
    }

    /**
     * Converts a list of domain performers into a list of DTO performers
     */
    public static List<PerformerDTO> toDTOList(List<Performer> performers) {
        List<PerformerDTO> performerDTOS = new ArrayList<>();

        for (Performer performer : performers) {
            performerDTOS.add(toDTO(performer));
        }

        return performerDTOS;
    }


}
