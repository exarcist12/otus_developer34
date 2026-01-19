package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import ru.otus.crm.model.Id;

public class EntitySQLMetaDataImpl implements EntitySQLMetaData {

    private final EntityClassMetaData<?> entityClassMetaData;
    private final String tableName;
    private final String idColumnName;

    public EntitySQLMetaDataImpl(EntityClassMetaData<?> entityClassMetaData) {
        this.entityClassMetaData = entityClassMetaData;
        this.tableName = entityClassMetaData.getName().toLowerCase();
        this.idColumnName = entityClassMetaData.getAllFields().stream()
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .get()
                .getName();
    }

    @Override
    public String getSelectAllSql() {
        return "select * from " + tableName;
    }

    @Override
    public String getSelectByIdSql() {
        return "select * from " + tableName + " where " + idColumnName + " = ?";
    }

    @Override
    public String getInsertSql() {
        List<Field> fields = entityClassMetaData.getFieldsWithoutId();
        String fieldsNames =
                String.join(", ", fields.stream().map(f -> f.getName()).toList());
        String questionMarks = String.join(", ", Collections.nCopies(fields.size(), "?"));
        String query = "insert into " + tableName + "(" + fieldsNames + ") values (" + questionMarks + ")";
        return query;
    }

    @Override
    public String getUpdateSql() {
        String query = "update " + tableName + " set name = ? where " + idColumnName + " = ?";
        return query;
    }

    public EntityClassMetaData<?> getEntityClassMetaData() {
        return entityClassMetaData;
    }
}
