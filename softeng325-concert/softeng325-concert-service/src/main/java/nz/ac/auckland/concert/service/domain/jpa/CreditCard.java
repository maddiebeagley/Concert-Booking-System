package nz.ac.auckland.concert.service.domain.jpa;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;

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
	@Enumerated(EnumType.STRING)
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
