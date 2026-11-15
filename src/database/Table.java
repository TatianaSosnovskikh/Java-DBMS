package database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Таблица базы данных.
 *
 * <p>
 * Таблица содержит список строк и список столбцов.
 */
public class Table implements Serializable {
    private String name;
    private List<Row> rows;
    private List<Column> columns;

    /**
     * Создаёт новую таблицу.
     *
     * @param name название таблицы
     */
    public Table(String name) {
        this.name = name;
        this.rows = new ArrayList<>();
        this.columns = new ArrayList<>();
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
     * Перед добавлением строки выполняется проверка уникальности значения
     * первичного ключа.
     *
     * @param row добавляемая строка
     * @return true - если строка добавлена, false - если обнаружен дубликат
     *         первичного ключа
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
}