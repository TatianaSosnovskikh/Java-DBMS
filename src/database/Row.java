package database;

import java.util.HashMap;
import java.util.Map;

/*
Одна запись таблицы.
*/

public class Row {
    private Map<String, Object> values;

    public Row() {
        values = new HashMap<>();
    }

    public void setValue(String columnName, Object value) {
        values.put(columnName, value);
    }

    public Object getValue(String columnName) {
        return values.get(columnName);
    }

    public Map<String, Object> getValues() {
        return values;
    }
}
