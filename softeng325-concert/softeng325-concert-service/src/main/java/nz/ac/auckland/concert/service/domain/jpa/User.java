package nz.ac.auckland.concert.service.domain.jpa;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * DTO class to represent users. 
 * 
 * A UserDTO describes a user in terms of:
 * _username  the user's unique username.
 * _password  the user's password.
 * _firstname the user's first name.
 * _lastname  the user's family name.
 *
 */
@Entity
public class User {

	@Id
	@Column(name = "row")
	private String _username;

	@Column(nullable = false, name = "password")
	private String _password;

	@Column(nullable = false, name = "firstname")
	private String _firstname;

	@Column(nullable = false, name = "lastname")
	private String _lastname;

	@Column(name = "creditCard")
	private CreditCard _creditCard;

	protected User() {}

	public User(String username, String password, String lastname, String firstname) {
		_username = username;
		_password = password;
		_lastname = lastname;
		_firstname = firstname;
	}

	public User(String username, String password) {
		this(username, password, null, null);
	}
	
	public String getUsername() {
		return _username;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public String getFirstname() {
		return _firstname;
	}
	
	public String getLastname() {
		return _lastname;
	}

	public CreditCard getCreditCard() {
		return _creditCard;
	}

	public void setCreditCard(CreditCard creditCard) {
		_creditCard = creditCard;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User))
            return false;
        if (obj == this)
            return true;

        User rhs = (User) obj;
        return new EqualsBuilder().
            append(_username, rhs._username).
            append(_password, rhs._password).
            append(_firstname, rhs._firstname).
            append(_lastname, rhs._lastname).
            append(_creditCard, rhs._creditCard).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_username).
	            append(_password).
	            append(_firstname).
	            append(_password).
	            append(_creditCard).
	            hashCode();
	}
}
