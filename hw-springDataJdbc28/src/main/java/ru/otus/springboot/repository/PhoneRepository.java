package ru.otus.springboot.repository;

import org.springframework.data.repository.ListCrudRepository;
import ru.otus.springboot.model.Phone;

public interface PhoneRepository extends ListCrudRepository<Phone, Long> {}
