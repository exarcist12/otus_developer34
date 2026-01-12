package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.executor.DbExecutor;

/** Сохраняет объект в базу, читает объект из базы */
@SuppressWarnings("java:S1068")
public class DataTemplateJdbc<T> implements DataTemplate<T> {

    private final DbExecutor dbExecutor;
    private final EntitySQLMetaData entitySQLMetaData;
    private final EntityClassMetaData<T> entityClassMetaData;

    public DataTemplateJdbc(DbExecutor dbExecutor, EntitySQLMetaData entitySQLMetaData) {
        this.dbExecutor = dbExecutor;
        this.entitySQLMetaData = entitySQLMetaData;
        this.entityClassMetaData =
                (EntityClassMetaData<T>) ((EntitySQLMetaDataImpl) entitySQLMetaData).getEntityClassMetaData();
    }

    @Override
    public Optional<T> findById(Connection connection, long id) {
        String sql = entitySQLMetaData.getSelectByIdSql();
        List<Object> params = List.of(id);

        return dbExecutor.executeSelect(connection, sql, params, rs -> {
            try {
                if (rs.next()) {
                    T obj = createObjectFromResultSet(rs);
                    return obj;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public List<T> findAll(Connection connection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long insert(Connection connection, T object) {
        String sql = entitySQLMetaData.getInsertSql();
        List<Field> fields = entityClassMetaData.getFieldsWithoutId();

        try (PreparedStatement pst = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Подставляем значения полей
            for (int i = 0; i < fields.size(); i++) {
                fields.get(i).setAccessible(true);
                Object value = fields.get(i).get(object);
                pst.setObject(i + 1, value);
            }
            pst.executeUpdate();

            // Получаем сгенерированный id
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return -1; // если ничего не вставилось
    }

    @Override
    public void update(Connection connection, T client) {
        throw new UnsupportedOperationException();
    }

    private T createObjectFromResultSet(ResultSet rs) {
        try {
            T obj = entityClassMetaData.getConstructor().newInstance(); // no-args!
            for (Field field : entityClassMetaData.getAllFields()) {
                field.setAccessible(true);
                Object value = rs.getObject(field.getName());
                field.set(obj, value);
            }
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate object from ResultSet", e);
        }
    }
}
