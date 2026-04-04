package eci.edu.co.pokerservice.mapper;

import eci.edu.co.pokerservice.model.document.Game;
import eci.edu.co.pokerservice.model.dto.GameDTO;
import eci.edu.co.pokerservice.model.dto.GamePublicDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {CartMapper.class, PlayerPublicMapper.class, PlayerMapper.class})
public interface GameMapper {

    @Mapping(source = "id", target = "id")
    GamePublicDTO toPublicDTO(Game game);
}
