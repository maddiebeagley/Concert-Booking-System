package nz.ac.auckland.concert.service.domain.jpa;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * DTO class to represent users. 
 * 
 * A UserDTO describes a user in terms of:
 * _userName  the user's unique userName.
 * _password  the user's password.
 * _firstName the user's first Name.
 * _lastName  the user's family Name.
 *
 */
@Entity
@Table(name = "USERS")
public class User {

	@Id
	@Column(name = "userName", nullable = false)
	private String _userName;

	@Column(nullable = false, name = "password")
	private String _password;

	@Column(nullable = false, name = "firstName")
	private String _firstName;

	@Column(nullable = false, name = "lastName")
	private String _lastName;

	@Column(name = "creditCard")
	private CreditCard _creditCard;

	protected User() {}

	public User(String userName, String password, String lastName, String firstName) {
		_userName = userName;
		_password = password;
		_lastName = lastName;
		_firstName = firstName;
	}

	public User(String userName, String password) {
		this(userName, password, null, null);
	}
	
	public String getUserName() {
		return _userName;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public String getFirstName() {
		return _firstName;
	}
	
	public String getLastName() {
		return _lastName;
	}

	public CreditCard getCreditCard() {
		return _creditCard;
	}

	public void setCreditCard(CreditCard creditCard) {
		_creditCard = creditCard;
	}

	public void setFirstName(String _firstName) {
		this._firstName = _firstName;
	}

	public void setLastName(String _lastName) {
		this._lastName = _lastName;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User))
            return false;
        if (obj == this)
            return true;

        User rhs = (User) obj;
        return new EqualsBuilder().
            append(_userName, rhs._userName).
            append(_password, rhs._password).
            append(_firstName, rhs._firstName).
            append(_lastName, rhs._lastName).
            append(_creditCard, rhs._creditCard).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_userName).
	            append(_password).
	            append(_firstName).
	            append(_password).
	            append(_creditCard).
	            hashCode();
	}
}
