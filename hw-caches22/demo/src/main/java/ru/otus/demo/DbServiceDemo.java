package ru.otus.demo;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.repository.executor.DbExecutorImpl;
import ru.otus.core.sessionmanager.TransactionRunnerJdbc;
import ru.otus.crm.datasource.DriverManagerDataSource;
import ru.otus.crm.model.Client;
import ru.otus.crm.repository.ClientDataTemplateJdbc;
import ru.otus.crm.service.DbServiceClientImpl;

public class DbServiceDemo {
    private static final String URL = "jdbc:postgresql://localhost:5430/demoDB";
    private static final String USER = "usr";
    private static final String PASSWORD = "pwd";

    private static final Logger log = LoggerFactory.getLogger(DbServiceDemo.class);

    public static void main(String[] args) {
        var dataSource = new DriverManagerDataSource(URL, USER, PASSWORD);
        flywayMigrations(dataSource);
        var transactionRunner = new TransactionRunnerJdbc(dataSource);
        var dbExecutor = new DbExecutorImpl();
        ///
        var clientTemplate = new ClientDataTemplateJdbc(dbExecutor); // реализация DataTemplate, заточена на Client

        ///
        var dbServiceClient = new DbServiceClientImpl(transactionRunner, clientTemplate);

        // Тест сохранения клиента 1
        long startSave = System.currentTimeMillis();
        Client clientFirst = dbServiceClient.saveClient(new Client("dbServiceFirst"));
        long endSave = System.currentTimeMillis();
        System.out.println("SaveClientFirst: " + (endSave - startSave) + " ms");
        // Тест получения клиента 1 из кэша
        long startReadClientFirst = System.currentTimeMillis();
        var clientFirstSelected = dbServiceClient
                .getClient(clientFirst.getId())
                .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientFirst.getId()));
        long endReadClientFirst = System.currentTimeMillis();
        System.out.println("ReadClientFirst: " + (endReadClientFirst - startReadClientFirst) + " ms");
        log.info("clientFirstSelected:{}", clientFirstSelected);
        // Тест получения клиента 1 без кэша
        dbServiceClient.removeClientFromCache(clientFirst);
        startReadClientFirst = System.currentTimeMillis();
        clientFirstSelected = dbServiceClient
                .getClient(clientFirst.getId())
                .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientFirst.getId()));
        endReadClientFirst = System.currentTimeMillis();
        System.out.println("ReadClientFirst: " + (endReadClientFirst - startReadClientFirst) + " ms");
        log.info("clientFirstSelected:{}", clientFirstSelected);
        // Тест получения клиента 1 из кэша
        startReadClientFirst = System.currentTimeMillis();
        clientFirstSelected = dbServiceClient
                .getClient(clientFirst.getId())
                .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientFirst.getId()));
        endReadClientFirst = System.currentTimeMillis();
        System.out.println("ReadClientFirst: " + (endReadClientFirst - startReadClientFirst) + " ms");
        log.info("clientFirstSelected:{}", clientFirstSelected);

        // Тест получения
        var clientSecond = dbServiceClient.saveClient(new Client("dbServiceSecond"));
        long startRead = System.currentTimeMillis();
        var clientSecondSelected = dbServiceClient
                .getClient(clientSecond.getId())
                .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientSecond.getId()));
        long endRead = System.currentTimeMillis();
        System.out.println("ReadClient: " + (endRead - startRead) + " ms");
        log.info("clientSecondSelected:{}", clientSecondSelected);
        ///
        // Тест получения 2
        long startRead2 = System.currentTimeMillis();
        var clientSecondSelected2 = dbServiceClient
                .getClient(clientSecond.getId())
                .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientSecond.getId()));
        long endRead2 = System.currentTimeMillis();
        System.out.println("ReadClient2: " + (endRead2 - startRead2) + " ms");
        log.info("clientSecondSelected2:{}", clientSecondSelected2);
        ///

        long startUpdate = System.currentTimeMillis();
        dbServiceClient.saveClient(new Client(clientSecondSelected.getId(), "dbServiceSecondUpdated"));
        var clientUpdated = dbServiceClient
                .getClient(clientSecondSelected.getId())
                .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientSecondSelected.getId()));
        log.info("clientUpdated:{}", clientUpdated);
        long endUpdate = System.currentTimeMillis();
        System.out.println("UpdateClient: " + (endUpdate - startUpdate) + " ms");

        log.info("All clients");
        long startFindAll = System.currentTimeMillis();
        var clients = dbServiceClient.findAll();
        long endFindAll = System.currentTimeMillis();
        System.out.println("findAll (всех клиентов): " + (endFindAll - startFindAll) + " ms");
        System.out.println("Найдено клиентов: " + clients.size());

        clients.forEach(client -> log.info("client:{}", client));
        System.out.println("Найдено клиентов: " + clients.size());
    }

    private static void flywayMigrations(DataSource dataSource) {
        log.info("db migration started...");
        var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:/db/migration")
                .load();
        flyway.migrate();
        log.info("db migration finished.");
        log.info("***");
    }
}
