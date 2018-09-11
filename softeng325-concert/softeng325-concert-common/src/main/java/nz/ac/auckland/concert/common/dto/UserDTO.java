package nz.ac.auckland.concert.common.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.*;

/**
 * DTO class to represent users. 
 * 
 * A UserDTO describes a user in terms of:
 * _userName  the user's unique userName.
 * _password  the user's password.
 * _firstName the user's first Name.
 * _lastName  the user's family Name.R
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserDTO {

	@XmlAttribute(name="userName")
	private String _userName;

	@XmlAttribute(name="password")
	private String _password;

	@XmlAttribute(name="firstName")
	private String _firstName;

	@XmlAttribute(name="lastName")
	private String _lastName;
	
	protected UserDTO() {}
	
	public UserDTO(String userName, String password, String lastName, String firstName) {
		_userName = userName;
		_password = password;
		_lastName = lastName;
		_firstName = firstName;
	}
	
	public UserDTO(String userName, String password) {
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

    @Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UserDTO))
			return false;
        if (obj == this)
            return true;

        UserDTO rhs = (UserDTO) obj;
        return new EqualsBuilder().
            append(_userName, rhs._userName).
            append(_password, rhs._password).
            append(_firstName, rhs._firstName).
            append(_lastName, rhs._lastName).
            isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31). 
	            append(_userName).
	            append(_password).
	            append(_firstName).
	            append(_password).
	            hashCode();
	}
}
