package eci.edu.co.pokerservice.mapper;


import eci.edu.co.pokerservice.model.document.Player;
import eci.edu.co.pokerservice.model.dto.PlayerPublicDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerPublicMapper {
    PlayerPublicDTO toPublicDTO(Player player);
}
