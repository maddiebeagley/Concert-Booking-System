package nz.ac.auckland.concert.service.domain.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

	@Column(name = "token", nullable = false, unique = true)
	private String _token;

	protected User() {
	}

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

	public String getToken(){
		return _token;
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

	public void setToken(String token) {
		_token = token;
	}

}