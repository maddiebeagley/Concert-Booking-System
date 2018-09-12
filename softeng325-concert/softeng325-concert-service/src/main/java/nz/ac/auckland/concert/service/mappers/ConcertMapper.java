package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.ConcertDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.jpa.Concert;
import nz.ac.auckland.concert.service.domain.jpa.Performer;

import java.math.BigDecimal;
import java.util.*;

/**
 * Simple helper class for converting to and from DTO and domain instances.
 */
public class ConcertMapper {

    /**
     * Method converts a domain model Concert object into a data transfer object.
     * @param concert : concert object to convert to a concertDTO
     * @return converted concertDTO
     */
    public static ConcertDTO toDTO(Concert concert) {

        if (concert == null) {
            return null;
        }

        HashMap<PriceBand, BigDecimal> ticketPrices = new HashMap<PriceBand, BigDecimal>();
        Set<Long> performerIds = new HashSet<>();

        //find all the ticket prices for concert DTO
        for (PriceBand priceBand : PriceBand.values()) {
            ticketPrices.put(priceBand, concert.getTicketPrice(priceBand));
        }

        //find all the performers of the current concerts.
        for (Performer performer : concert.getPerformers()) {
            performerIds.add(performer.getPerformerId());
        }

        return new ConcertDTO(concert.getId(),
                concert.getTitle(),
                concert.getDates(),
                ticketPrices,
                performerIds);
    }

    /**
     * Converts a list of domain objects to a list of DTO objects.
     */
    public static List<ConcertDTO> toDTOList(List<Concert> concerts) {
        List<ConcertDTO> concertDTOS = new ArrayList<>();

        for (Concert concert : concerts ) {
            concertDTOS.add(toDTO(concert));
        }

        return concertDTOS;
    }
}
