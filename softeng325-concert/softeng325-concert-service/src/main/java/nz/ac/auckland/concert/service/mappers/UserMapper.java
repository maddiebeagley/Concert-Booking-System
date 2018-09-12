package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.jpa.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple helper class for converting to and from DTO and domain instances.
 */
public class UserMapper {

    /*
    Converts an instance of a user DTO to an instance of a domain user.
     */
    public static User toDomain(UserDTO userDTO) {
        return new User(
                userDTO.getUserName(),
                userDTO.getPassword(),
                userDTO.getLastName(),
                userDTO.getFirstName());
    }

    /**
     * Converts an instance of a domain User to a DTO user.
     */
    public static UserDTO toDTO(User user) {
        return new UserDTO(user.getUserName(),
                user.getPassword(),
                user.getLastName(),
                user.getFirstName());
    }

}
