package ru.otus.springboot.repository;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import ru.otus.springboot.model.Client;

public interface ClientRepository extends ListCrudRepository<Client, Long> {

    Optional<Client> findByName(String name);
}
