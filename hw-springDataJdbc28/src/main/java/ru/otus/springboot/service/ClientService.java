package ru.otus.springboot.service;

import java.util.List;
import java.util.Optional;
import ru.otus.springboot.model.Client;

public interface ClientService {

    Client saveClient(Client client);

    Client saveClientWithStreet(String name, String street);

    Client saveClientWithAddressAndPhones(String name, String street, List<String> phoneNumbers);

    Optional<Client> findClient(long id);

    List<Client> findAllClients();

    void deleteClient(long id);

    Client updateClient(long id, Client client);
}
