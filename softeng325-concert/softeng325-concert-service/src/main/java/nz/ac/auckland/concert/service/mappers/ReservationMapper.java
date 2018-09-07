package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.jpa.Reservation;
import nz.ac.auckland.concert.service.domain.jpa.ReservationRequest;
import nz.ac.auckland.concert.service.domain.jpa.Seat;

import java.util.*;

public class ReservationMapper {

    public static ReservationDTO toDTO(Reservation reservation) {

        if (reservation == null) {
            return null;
        }

        Set<SeatDTO> seatDTOs = new HashSet<>();

        for (Seat seat : reservation.getSeats()) {
            seatDTOs.add(new SeatDTO(seat.getRow(), seat.getNumber()));
        }

        return new ReservationDTO(
                reservation.getReservationId(),
                toRequestDTO(reservation.getReservationRequest()),
                seatDTOs
        );
    }

    public static ReservationRequestDTO toRequestDTO(ReservationRequest reservationRequest) {
        return new ReservationRequestDTO(
                reservationRequest.getNumberOfSeats(),
                reservationRequest.getSeatType(),
                reservationRequest.getConcertId(),
                reservationRequest.getDate()
        );
    }

    public static ReservationRequest toRequestDomain(ReservationRequestDTO reservationRequestDTO) {
        return new ReservationRequest(
                reservationRequestDTO.getNumberOfSeats(),
                reservationRequestDTO.getSeatType(),
                reservationRequestDTO.getConcertId(),
                reservationRequestDTO.getDate()
        );
    }



}
