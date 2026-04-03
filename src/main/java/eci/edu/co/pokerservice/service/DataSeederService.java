package eci.edu.co.pokerservice.service;

import eci.edu.co.pokerservice.model.document.Cart;
import eci.edu.co.pokerservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeederService implements CommandLineRunner {

    private final CartRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {

        // Solo hace seed si la colección está vacía
        if (repository.count() > 0) {
            log.info("La colección ya tiene datos. Se omite el seed.");
            return;
        }

        log.info("Iniciando seed de datos...");

        // Lee el JSON desde resources
        ClassPathResource resource = new ClassPathResource("data/seed-data.json");
        List<Cart> datos = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Cart>>() {}
        );

        repository.saveAll(datos);
        log.info("Seed completado: {} documentos insertados.", datos.size());
    }
}
