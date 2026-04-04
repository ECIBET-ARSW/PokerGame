package eci.edu.co.pokerservice.mapper;

import eci.edu.co.pokerservice.model.document.Lobby;
import eci.edu.co.pokerservice.model.dto.LobbyDTO;
import org.mapstruct.Mapper;



@Mapper(componentModel = "spring", uses = {GameMapper.class})
public interface LobbyMapper {

    LobbyDTO toDTO(Lobby lobby);

}
