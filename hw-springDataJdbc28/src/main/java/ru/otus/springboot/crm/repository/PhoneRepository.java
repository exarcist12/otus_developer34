package ru.otus.springboot.crm.repository;

import org.springframework.data.repository.ListCrudRepository;
import ru.otus.springboot.crm.model.Phone;

public interface PhoneRepository extends ListCrudRepository<Phone, Long> {}
