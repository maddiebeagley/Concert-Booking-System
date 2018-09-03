package nz.ac.auckland.concert.service.mappers;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.service.domain.jpa.CreditCard;

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
