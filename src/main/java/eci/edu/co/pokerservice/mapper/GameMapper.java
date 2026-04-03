package eci.edu.co.pokerservice.mapper;

import eci.edu.co.pokerservice.model.document.Game;
import eci.edu.co.pokerservice.model.dto.GameDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {CartMapper.class, PlayerMapper.class})
public interface GameMapper {

    @Mapping(target = "players", ignore = true)
    @Mapping(target = "carts", ignore = true)
    @Mapping(target = "cartsInTable", ignore = true)
    @Mapping(target = "winner", ignore = true)
    GameDTO toDTO(Game game);
}
