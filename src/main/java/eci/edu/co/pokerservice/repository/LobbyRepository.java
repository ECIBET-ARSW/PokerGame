package eci.edu.co.pokerservice.repository;

import eci.edu.co.pokerservice.model.document.Lobby;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LobbyRepository extends MongoRepository<Lobby,String> {
}
