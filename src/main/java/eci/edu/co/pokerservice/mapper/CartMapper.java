package eci.edu.co.pokerservice.mapper;

import eci.edu.co.pokerservice.model.document.Cart;
import eci.edu.co.pokerservice.model.dto.CartDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper{

    CartDTO toDTO(Cart cart);
}
