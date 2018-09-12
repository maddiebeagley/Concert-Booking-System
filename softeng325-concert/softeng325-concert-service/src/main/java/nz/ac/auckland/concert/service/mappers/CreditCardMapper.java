package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.service.domain.jpa.CreditCard;

/**
 * Simple helper class for converting to and from DTO and domain instances.
 */
public class CreditCardMapper {

    public static CreditCard toDomain(CreditCardDTO creditCardDTO) {
        CreditCard.Type creditCardType;

        if (creditCardDTO.getType() == CreditCardDTO.Type.Visa){
            creditCardType = CreditCard.Type.Visa;
        } else {
            creditCardType = CreditCard.Type.Master;
        }

        return new CreditCard(
                creditCardType,
                creditCardDTO.getName(),
                creditCardDTO.getNumber(),
                creditCardDTO.getExpiryDate());
    }
}
