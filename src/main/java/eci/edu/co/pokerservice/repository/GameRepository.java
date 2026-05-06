package eci.edu.co.pokerservice.repository;

import eci.edu.co.pokerservice.model.document.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game,String> {
}
