package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.UserDTO;
import nz.ac.auckland.concert.service.domain.jpa.User;

import java.util.ArrayList;
import java.util.List;

public class UserMapper {

    public static User toDomain(UserDTO userDTO) {
        return new User(
                userDTO.getUsername(),
                userDTO.getPassword(),
                userDTO.getFirstname(),
                userDTO.getLastname());
    }

    public static UserDTO toDTO(User user) {
        return new UserDTO(user.getUsername(),
                user.getPassword(),
                user.getLastname(),
                user.getFirstname());
    }

    public static List<UserDTO> toDTOList(List<User> users) {
        List<UserDTO> userDTOS = new ArrayList<>();

        for (User user : users) {
            userDTOS.add(toDTO(user));
        }
        return userDTOS;
    }
}