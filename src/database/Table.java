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

    public Table(String name) {
        this.name = name;
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
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
                Object excistingValue = existingRow.getValue(primaryKey.getName());

                if (excistingValue != null && excistingValue.equals(newValue)) {
                    System.out.println("Ошибка: первичный ключ уже существует!");
                    return false;
                }
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
}
