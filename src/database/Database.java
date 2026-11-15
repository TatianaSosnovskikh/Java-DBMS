package database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * База данных.
 *
 * <p>Содержит коллекцию таблиц и методы управления ими.
 */
public class Database implements Serializable {
    private Map<String, Table> tables;

    /** Создаёт пустую таблицу. */
    public Database() {
        tables = new HashMap<>();
    }

    /**
     * Добавляет новую таблицу в базу данных.
     *
     * @param table таблица
     */
    public void createTable(Table table) {
        tables.put(table.getName(), table);
    }

    /**
     * Добавляет таблицу по имени.
     *
     * @param tableName название таблицы
     * @return объект таблицы
     */
    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    /**
     * Удаляет таблицу из базы данных.
     *
     * @param tableName название таблицы
     */
    public void removeTable(String tableName) {
        tables.remove(tableName);
    }

    /**
     * Возвращает все таблицы базы данных.
     *
     * @return коллекция таблиц
     */
    public Map<String, Table> getTables() {
        return tables;
    }
}
