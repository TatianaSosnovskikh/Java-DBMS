package database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Таблица базы данных.
 *
 * <p>Таблица содержит список строк и список столбцов.
 */
public class Table implements Serializable {
    private static final long serialVersionUID = 7723786006977745143L;
    private String name;
    private List<Row> rows;
    private List<Column> columns;
    private List<ForeignKey> foreignKeys;
    private transient java.util.Map<Object, Row> rowsByPrimaryKey;

    /**
     * Создаёт новую таблицу.
     *
     * @param name название таблицы
     */
    public Table(String name) {
        this.name = name;
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
        this.foreignKeys = new ArrayList<>();
        this.rowsByPrimaryKey = new java.util.HashMap<>();
    }

    /**
     * Возвращает название таблицы.
     *
     * @return название таблицы
     */
    public String getName() {
        return name;
    }

    /**
     * Добавляет новый столбец.
     *
     * @param column столбец
     */
    public void addColumn(Column column) {
        columns.add(column);
    }

    /**
     * Возвращает список столбцов.
     *
     * @return список столбцов
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Возвращает список строк.
     *
     * @return список строк
     */
    public List<Row> getRows() {
        return rows;
    }

    /**
     * Перед добавлением строки выполняется проверка уникальности значения первичного ключа.
     *
     * @param row добавляемая строка
     * @return true - если строка добавлена, false - если обнаружен дубликат первичного ключа
     */
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
     *
     * @param foreignKey внешний ключ
     */
    public void addForeignKey(ForeignKey foreignKey) {
        foreignKeys.add(foreignKey);
    }

    /**
     * Возвращает список внешних ключей таблицы.
     *
     * @return список внешних ключей таблицы
     */
    public List<ForeignKey> getForeignKeys() {
        return foreignKeys;
    }

    /**
     * Возвращает строку по первичному ключу.
     *
     * @param value значение
     * @return строку таблицы
     */
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

    /**
     * Перестраивает индекс строк таблицы по значению первичного ключа.
     *
     * <p>Метод очищает текущий индекс, находит столбец с первичным ключом и заново связывает
     * непустые значения этого ключа с соответствующими строками. Если первичный ключ в таблице
     * отсутствует, индекс остаётся пустым.
     */
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
