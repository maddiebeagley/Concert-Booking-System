package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.jpa.Seat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatMapper {

    //TODO revise these methods.
//    public static Seat toDomain(SeatDTO seatDTO) {
//        return new Seat(seatDTO.getRow(), seatDTO.getNumber());
//    }
//
//    public static Set<Seat> toDomainSet(Set<SeatDTO> seatDTOList) {
//        Set<Seat> seats = new HashSet<>();
//
//        for (SeatDTO seatDTO : seatDTOList) {
//            seats.add(toDomain(seatDTO));
//        }
//        return seats;
//    }

    public static SeatDTO toDTO(Seat seat) {
        return new SeatDTO(seat.getRow(), seat.getNumber());
    }

    public static Set<SeatDTO> toDTOSet(Set<Seat> seats) {
        Set<SeatDTO> seatDTOS = new HashSet<>();

        for (Seat seat : seats) {
            seatDTOS.add(toDTO(seat));
        }
        return seatDTOS;
    }
}
