package nz.ac.auckland.concert.service.domain.jpa;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import java.time.LocalDate;

import nz.ac.auckland.concert.common.jaxb.LocalDateAdapter;

/**
 * DTO class to represent credit cards. 
 * 
 * A CreditCardDTO describes a credit card in terms of:
 * _type       type of credit card, Visa or Mastercard.
 * _name       the name of the person who owns the credit card.
 * _number     16-digit credit card number. 
 * _expiryDate the credit card's expiry date. 
 *
 */
@Embeddable
public class CreditCard {

	public enum Type {Visa, Master};

	@Column(name = "type")
	private Type _type;

	@Column(name = "name")
	private String _name;

	@Column(name = "number")
	private String _number;

	@Column(name = "expiryDate")
	private LocalDate _expiryDate;

	public CreditCard() {}

	public CreditCard(Type type, String name, String number, LocalDate expiryDate) {
		_type = type;
		_name = name;
		_number = number;
		_expiryDate = expiryDate;
	}
	
	public Type getType() {
		return _type;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getNumber() {
		return _number;
	}

	public LocalDate getExpiryDate() {
		return _expiryDate;
	}
	
}
