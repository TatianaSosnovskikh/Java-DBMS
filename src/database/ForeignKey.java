package database;

import java.io.Serializable;

/**
 * Описание внешнего ключа между двумя таблицами. Связывает столбец текущей таблицы с целевым
 * столбцом другой таблицы.
 */
public class ForeignKey implements Serializable {

    private String columnName;
    private String referencedTable;
    private String referencedColumn;

    /**
     * Создаёт новый внешний ключ.
     *
     * @param columnName имя столбца в текущей таблице
     * @param referencedTable имя таблицы, на которую ссылается внешний ключ
     * @param referencedColumn имя столбца в целевой таблице
     */
    public ForeignKey(String columnName, String referencedTable, String referencedColumn) {
        this.columnName = columnName;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
    }

    /**
     * Возвращает имя столбца в текущей таблице.
     *
     * @return имя столбца
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Возвращает имя таблицы, на которую ссылается внешний ключ.
     *
     * @return имя связанной таблицы
     */
    public String getReferencedTable() {
        return referencedTable;
    }

    /**
     * Возвращает имя столбца в целевой таблице.
     *
     * @return имя связанного столбца
     */
    public String getReferencedColumn() {
        return referencedColumn;
    }
}
