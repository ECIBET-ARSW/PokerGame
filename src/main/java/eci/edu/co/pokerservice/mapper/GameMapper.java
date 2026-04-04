package eci.edu.co.pokerservice.mapper;

import eci.edu.co.pokerservice.model.document.Game;
import eci.edu.co.pokerservice.model.dto.GameDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {CartMapper.class, PlayerMapper.class})
public interface GameMapper {

    GameDTO toDTO(Game game);
}
