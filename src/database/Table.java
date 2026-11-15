package database;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;

/*
Таблица базы данных.
*/
public class Table implements Serializable {
    private String name;
    private List<Row> rows;
    private List<Column> columns;
    private List<ForeignKey> foreignKeys;
    private transient java.util.Map<Object, Row> rowsByPrimaryKey;

    public Table(String name) {
        this.name = name;
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
        this.rowsByPrimaryKey = new java.util.HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Row> getRows() {
        return rows;
    }

    public boolean addRow(Row row) {
        Column primaryKey = null;

        for (Column column : columns) {
            if (column.isPrimaryKey()) {
                primaryKey = column;
                break;
            }
        }

        if (primaryKey != null) {
            Object newValue = row.getValue(primaryKey.getName());

            for (Row existingRow : rows) {
                Object existingValue = existingRow.getValue(primaryKey.getName());

                if (existingValue != null && existingValue.equals(newValue)) {
                    System.out.println("Ошибка: первичный ключ уже существует!");
                    return false;
                }
            }

            if (rowsByPrimaryKey != null && newValue != null) {
                rowsByPrimaryKey.put(newValue, row);
            }
        }

        rows.add(row);
        return true;
    }
    /**
     * Добавляет внешний ключ таблицы.
     */
    public void addForeignKey(ForeignKey foreignKey) {
        foreignKeys.add(foreignKey);
    }

    /**
     * Возвращает список внешних ключей таблицы.
     */
    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }
    public Row getRowByPrimaryKey(Object value) {
        if (rowsByPrimaryKey == null) {
            rowsByPrimaryKey = new java.util.HashMap<>();

            Column primaryKey = null;
            for (Column column : columns) {
                if (column.isPrimaryKey()) {
                    primaryKey = column;
                    break;
                }
            }

            if (primaryKey == null) {
                return null;
            }

            for (Row row : rows) {
                Object key = row.getValue(primaryKey.getName());
                if (key != null) {
                    rowsByPrimaryKey.put(key, row);
                }
            }
        }

        return rowsByPrimaryKey.get(value);
    }
    public void rebuildPrimaryKeyIndex() {
        rowsByPrimaryKey = new java.util.HashMap<>();

        Column primaryKey = null;

        for (Column column : columns) {
            if (column.isPrimaryKey()) {
                primaryKey = column;
                break;
            }
        }

        if (primaryKey == null) {
            return;
        }

        for (Row row : rows) {
            Object key = row.getValue(primaryKey.getName());

            if (key != null) {
                rowsByPrimaryKey.put(key, row);
            }
        }
    }
}
