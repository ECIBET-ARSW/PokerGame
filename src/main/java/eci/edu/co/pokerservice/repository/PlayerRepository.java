package eci.edu.co.pokerservice.repository;

import eci.edu.co.pokerservice.model.document.Player;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerRepository extends MongoRepository<Player,String> {
}
