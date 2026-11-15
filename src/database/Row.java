package database;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Представляет одну запись таблицы.
 *
 * <p>Значения строки хрантся в виде пар "имя столбца - значение".
 */
public class Row implements Serializable {
    private Map<String, Object> values;

    /** Создаёт пустую строку таблицы. */
    public Row() {
        values = new HashMap<>();
    }

    /**
     * Записывает значение в указанный столбец.
     *
     * @param columnName имя столбца
     * @param value значение
     */
    public void setValue(String columnName, Object value) {
        values.put(columnName, value);
    }

    /**
     * Возвращает значение указанного столбца.
     *
     * @param columnName имя столбца
     * @return значение столбца
     */
    public Object getValue(String columnName) {
        return values.get(columnName);
    }

    /**
     * Возвращает все значения строки.
     *
     * @return коллекция значений
     */
    public Map<String, Object> getValues() {
        return values;
    }
}
