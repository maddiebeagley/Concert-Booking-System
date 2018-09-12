package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.common.types.PriceBand;
import nz.ac.auckland.concert.service.domain.jpa.Reservation;
import nz.ac.auckland.concert.service.domain.jpa.ReservationRequest;
import nz.ac.auckland.concert.service.domain.jpa.Seat;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Simple helper class for converting to and from DTO and domain instances.
 */
public class ReservationMapper {

    /*
    Converts a domain instance of a reservation to a DTO instance.
     */
    public static ReservationDTO toReservationDTO(Reservation reservation) {

        if (reservation == null) {
            return null;
        }

        Set<SeatDTO> seatDTOs = new HashSet<>();

        for (Seat seat : reservation.getSeats()) {
            seatDTOs.add(SeatMapper.toDTO(seat));
        }

        return new ReservationDTO(
                reservation.getReservationId(),
                toRequestDTO(reservation.getReservationRequest()),
                seatDTOs
        );
    }

    /**
     * Converts a Reservation Request to its corresponding DTO object.
     */
    public static ReservationRequestDTO toRequestDTO(ReservationRequest reservationRequest) {
        return new ReservationRequestDTO(
                reservationRequest.getNumberOfSeats(),
                reservationRequest.getSeatType(),
                reservationRequest.getConcertId(),
                reservationRequest.getDate()
        );
    }

    /**
     * Converts a ReservationRequest DTO instance into a corresponding domain model instance.
     */
    public static ReservationRequest toRequestDomain(ReservationRequestDTO reservationRequestDTO) {
        return new ReservationRequest(
                reservationRequestDTO.getNumberOfSeats(),
                reservationRequestDTO.getSeatType(),
                reservationRequestDTO.getConcertId(),
                reservationRequestDTO.getDate()
        );
    }

    /**
     * Converts a Reservation into a booking DTO object since bookings and reservations are
     * used as equivalent objects with respect to the domain objects.
     */
    public static BookingDTO toBookingDTO(Reservation reservation, String concertTitle){

        Set<SeatDTO> seatDTOs = SeatMapper.toDTOSet(reservation.getSeats());

        return new BookingDTO(reservation.getReservationRequest().getConcertId(),
                concertTitle,
                reservation.getReservationRequest().getDate(),
                seatDTOs,
                reservation.getReservationRequest().getSeatType());

    }





}
