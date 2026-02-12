package ru.otus.springboot.repository;

import org.springframework.data.repository.ListCrudRepository;
import ru.otus.springboot.model.Address;

public interface AddressRepository extends ListCrudRepository<Address, Long> {}
