package eci.edu.co.pokerservice.repository;

import eci.edu.co.pokerservice.model.document.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartRepository extends MongoRepository<Cart, Long> {
}
