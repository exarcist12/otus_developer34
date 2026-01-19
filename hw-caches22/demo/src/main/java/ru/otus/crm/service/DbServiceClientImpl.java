package ru.otus.crm.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.cachehw.HwCache;
import ru.otus.cachehw.MyCache;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.sessionmanager.TransactionRunner;
import ru.otus.crm.model.Client;

public class DbServiceClientImpl implements DBServiceClient {
    private static final Logger log = LoggerFactory.getLogger(DbServiceClientImpl.class);

    private final DataTemplate<Client> dataTemplate;
    private final TransactionRunner transactionRunner;
    private final HwCache<String, Client> cache = new MyCache<>();

    public DbServiceClientImpl(TransactionRunner transactionRunner, DataTemplate<Client> dataTemplate) {
        this.transactionRunner = transactionRunner;
        this.dataTemplate = dataTemplate;
    }

    @Override
    public Client saveClient(Client client) {
        return transactionRunner.doInTransaction(connection -> {
            if (client.getId() == null) {
                var clientId = dataTemplate.insert(connection, client);
                var createdClient = new Client(clientId, client.getName());
                log.info("created client: {}", createdClient);
                cache.put(String.valueOf(clientId), createdClient);
                log.info("Client added to cache: {}", client);
                return createdClient;
            }
            dataTemplate.update(connection, client);
            log.info("updated client: {}", client);
            removeClientFromCache(client);
            cache.put(String.valueOf(client.getId()), client);
            return client;
        });
    }

    public Optional<Client> getClient(long id) {
        String cacheKey = String.valueOf(id);
        Client cachedClient = cache.get(cacheKey);
        if (cachedClient != null) {
            log.info("Client found in cache: {}", cachedClient);
            return Optional.of(cachedClient);
        } else {
            return transactionRunner.doInTransaction(connection -> {
                var clientOptional = dataTemplate.findById(connection, id);
                log.info("client from DB: {}", clientOptional);
                clientOptional.ifPresent(client -> {
                    cache.put(cacheKey, client);
                    log.info("Client added to cache: {}", client);
                });

                return clientOptional;
            });
        }
    }

    @Override
    public List<Client> findAll() {
        return transactionRunner.doInTransaction(connection -> {
            var clientList = dataTemplate.findAll(connection);
            log.info("clientList:{}", clientList);
            return clientList;
        });
    }

    public void removeClientFromCache(Client client) {
        cache.remove(client.getId().toString());
    }
}
