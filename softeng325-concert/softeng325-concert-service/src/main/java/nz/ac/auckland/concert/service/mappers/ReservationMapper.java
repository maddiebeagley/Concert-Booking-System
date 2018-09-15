package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.ReservationDTO;
import nz.ac.auckland.concert.common.dto.ReservationRequestDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.jpa.Reservation;
import nz.ac.auckland.concert.service.domain.jpa.Seat;

import java.util.HashSet;
import java.util.Set;

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

        ReservationRequestDTO reservationRequestDTO = new ReservationRequestDTO(
                reservation.getNumberOfSeats(),
                reservation.getSeatType(),
                reservation.getConcertId(),
                reservation.getDate()
        );

        return new ReservationDTO(
                reservation.getReservationId(),
                reservationRequestDTO,
                seatDTOs
        );
    }


    /**
     * Converts a Reservation into a booking DTO object since bookings and reservations are
     * used as equivalent objects with respect to the domain objects.
     */
    public static BookingDTO toBookingDTO(Reservation reservation, String concertTitle){

        Set<SeatDTO> seatDTOs = SeatMapper.toDTOSet(reservation.getSeats());

        return new BookingDTO(reservation.getConcertId(),
                concertTitle,
                reservation.getDate(),
                seatDTOs,
                reservation.getSeatType());

    }





}
