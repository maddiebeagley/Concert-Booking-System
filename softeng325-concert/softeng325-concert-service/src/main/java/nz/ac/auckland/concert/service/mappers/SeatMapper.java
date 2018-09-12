package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.jpa.Seat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple helper class for converting to and from DTO and domain instances.
 */
public class SeatMapper {

    /*
    Converts a domain instance of a seat to a DTO instance.
     */
    public static SeatDTO toDTO(Seat seat) {
        return new SeatDTO(seat.getRow(), seat.getNumber());
    }

    /**
     * Converts a set of domain seats to a set of DTO seats.
     */
    public static Set<SeatDTO> toDTOSet(Set<Seat> seats) {
        Set<SeatDTO> seatDTOS = new HashSet<>();

        for (Seat seat : seats) {
            seatDTOS.add(toDTO(seat));
        }
        return seatDTOS;
    }
}
