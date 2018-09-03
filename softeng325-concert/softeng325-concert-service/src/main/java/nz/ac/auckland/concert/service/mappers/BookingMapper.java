package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.BookingDTO;
import nz.ac.auckland.concert.common.dto.SeatDTO;
import nz.ac.auckland.concert.service.domain.jpa.Booking;
import nz.ac.auckland.concert.service.domain.jpa.Seat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookingMapper {

    public static BookingDTO toDTO(Booking booking) {

        Set<SeatDTO> seatDTOSet = new HashSet<>();

        for (Seat seat : booking.getSeats()) {
            seatDTOSet.add(new SeatDTO(seat.getRow(), seat.getNumber()));
        }

        return new BookingDTO(
                booking.getConcertId(),
                booking.getConcertTitle(),
                booking.getDateTime(),
                seatDTOSet,
                booking.getPriceBand());
    }

    public static List<BookingDTO> toDTOList(List<Booking> bookings) {
        List<BookingDTO> bookingDTOS = new ArrayList<>();

        for (Booking booking : bookings) {
            bookingDTOS.add(toDTO(booking));
        }

        return bookingDTOS;
    }
}
