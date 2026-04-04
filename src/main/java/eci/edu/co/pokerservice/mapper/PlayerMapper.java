package eci.edu.co.pokerservice.mapper;

import eci.edu.co.pokerservice.model.document.Player;
import eci.edu.co.pokerservice.model.dto.PlayerDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerMapper {
    PlayerDTO toDTO(Player player);
}
