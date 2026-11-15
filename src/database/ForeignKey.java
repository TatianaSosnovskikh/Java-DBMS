package database;

import java.io.Serializable;

/**
 * Описание внешнего ключа между двумя таблицами.
 */
public class ForeignKey implements Serializable {

    private String columnName;
    private String referencedTable;
    private String referencedColumn;

    public ForeignKey(
            String columnName,
            String referencedTable,
            String referencedColumn
    ) {
        this.columnName = columnName;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getReferencedTable() {
        return referencedTable;
    }

    public String getReferencedColumn() {
        return referencedColumn;
    }
}
