package ru.otus.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.cachehw.HwCache;
import ru.otus.cachehw.HwListener;
import ru.otus.cachehw.MyCache;
import ru.otus.crm.model.Client;

public class DbServiceDemoCache {

    private static final Logger log = LoggerFactory.getLogger(DbServiceDemoCache.class);

    public static void main(String[] args) {
        HwCache<String, Client> cache = new MyCache<>();

        HwListener<String, Client> listener = new HwListener<String, Client>() {
            @Override
            public void notify(String key, Client value, String action) {
                log.info("key:{}, value:{}, action: {}", key, value.getName(), action);
            }
        };

        // Тест сохранения
        long startSave = System.currentTimeMillis();
        cache.addListener(listener);
        cache.put("1", new Client("dbServiceFirst"));
        long endSave = System.currentTimeMillis();
        System.out.println("SaveClient: " + (endSave - startSave) + " ms");

        // Тест получения
        long startRead = System.currentTimeMillis();
        Client clientFromCache = cache.get("1");
        long endRead = System.currentTimeMillis();
        System.out.println("ReadClient from cache: " + (endRead - startRead) + " ms");

        // Тест обновления
        long startUpdate = System.currentTimeMillis();
        cache.addListener(listener);
        cache.put("1", new Client("dbServiceFirst2"));
        long endUpdate = System.currentTimeMillis();
        System.out.println("UpdateClient: " + (endUpdate - startUpdate) + " ms");

    }
}
