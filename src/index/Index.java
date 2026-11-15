package index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Index implements Serializable {
    private static final long serialVersionUID = 1L;
    private String indexName;
    private String tableName;
    private String columnName;
    private String primaryKeyColumn;
    private Map<Object, List<Object>> entries = new HashMap<>();

    public Index(String indexName, String tableName, String columnName, String primaryKeyColumn) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public void add(Object indexedValue, Object primaryKeyValue) {
        if (indexedValue == null || primaryKeyValue == null) {
            return;
        }
        entries.computeIfAbsent(indexedValue, key -> new ArrayList<>()).add(primaryKeyValue);
    }

    public List<Object> getPrimaryKeys(Object indexedValue) {
        List<Object> result = entries.get(indexedValue);
        return result == null ? new ArrayList<>() : new ArrayList<>(result);
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public Map<Object, List<Object>> getEntries() {
        return entries;
    }

    public void clear() {
        entries.clear();
    }
}
