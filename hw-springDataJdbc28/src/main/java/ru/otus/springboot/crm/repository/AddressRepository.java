package ru.otus.springboot.crm.repository;

import org.springframework.data.repository.ListCrudRepository;
import ru.otus.springboot.crm.model.Address;

public interface AddressRepository extends ListCrudRepository<Address, Long> {}
