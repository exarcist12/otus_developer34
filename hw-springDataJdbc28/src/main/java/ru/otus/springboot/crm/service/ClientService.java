package ru.otus.springboot.crm.service;

import java.util.List;
import java.util.Optional;
import ru.otus.springboot.crm.model.Client;

public interface ClientService {

    Client saveClient(Client client);

    Optional<Client> findClient(long id);

    List<Client> findAllClients();

    void deleteClient(long id);

    Client updateClient(long id, Client client);
}
