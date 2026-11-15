package database;

import java.io.Serializable;

/**
 * Описание столбца таблицы. Каждый столбец имеет имя, тип данных и может
 * являться первичным ключом.
 */
public class Column implements Serializable {
    private String name;
    private DataType type;
    private boolean primaryKey;

    /**
     * Создаёт новый столбец.
     *
     * @param name       имя столбца
     * @param type       тип данных столбца
     * @param primaryKey признак первичного ключа
     */
    public Column(String name, DataType type, boolean primaryKey) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
    }

    /**
     * Возвращает имя столбца.
     *
     * @return имя столбца
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает тип данных столбца.
     *
     * @return тип данных
     */
    public DataType getType() {
        return type;
    }

    /**
     * Проверяет, является ли столбец первичным ключом.
     *
     * @return true, если столбец - первичный ключ, иначе - false
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }
}
