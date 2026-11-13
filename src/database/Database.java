package database;

import java.util.HashMap;
import java.util.Map;

/*
База данных.
*/
public class Database {
    private Map<String, Table> tables;

    public Database() {
        tables = new HashMap<>();
    }

    public void createTable(Table table) {
        tables.put(table.getName(), table);
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    public void removeTable(String tableName) {
        tables.remove(tableName);
    }

    public Map<String, Table> getTables() {
        return tables;
    }
}
