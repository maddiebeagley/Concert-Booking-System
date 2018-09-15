package nz.ac.auckland.concert.service.util;

import nz.ac.auckland.concert.service.domain.jpa.Seat;

import java.util.*;

/**
 * Utility class with a search method that identifies seats that are available
 * to reserve.
 *
 */
public class TheatreUtility {

	/**
	 * Attempts to find a specified number of seats, within a given priceband,
	 * that aren't currently booked.
	 * 
	 * @param numberOfSeats
	 *            the number of seats required.
	 * @param availableSeats
	 *            the set of seats that are currently available.
	 * 
	 * @return a set of seats that are available to book. When successful the
	 *         set is non-empty and contains numberOfSeats seats that are within
	 *         the specified priceband. When not successful (i.e. when there are
	 *         not enough seats available in the required priceband, this method
	 *         returns the empty set.
	 *
	*/
	public static Set<Seat> findAvailableSeats(int numberOfSeats,
			Set<Seat> availableSeats) {

		if (availableSeats.size() < numberOfSeats) {
			return new HashSet<Seat>();
		}

		return getSpecificAvailableSeats(
				new Random().nextInt(availableSeats.size()), numberOfSeats,
				new ArrayList<>(availableSeats));
	}


	protected static Set<Seat> getSpecificAvailableSeats(int startIndex,
														 int numberOfSeats, List<Seat> openSeats) {
		Set<Seat> availableSeats = new HashSet<Seat>();
		while (numberOfSeats > 0) {
			if (startIndex > openSeats.size() - 1) {
				startIndex = 0;
			}
			availableSeats.add(openSeats.get(startIndex));
			startIndex++;
			numberOfSeats--;
		}
		return availableSeats;

	}
}
