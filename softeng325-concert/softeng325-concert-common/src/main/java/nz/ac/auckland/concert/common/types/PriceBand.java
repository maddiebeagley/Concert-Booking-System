package nz.ac.auckland.concert.common.types;

import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Enumerated type for classifying seats according to price bands.
 *
 */
@XmlType(name = "priceBand")
@XmlEnum
public enum PriceBand {
	PriceBandA, PriceBandB, PriceBandC;
}
