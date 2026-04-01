package eci.edu.co.pokerservice.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PlayerMapper.class, CartMapper.class})
public interface LobbyMapper {
}
