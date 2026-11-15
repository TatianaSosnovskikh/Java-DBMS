package index;

import database.Column;
import database.Database;
import database.Row;
import database.Table;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexManager {
    private static final String FILE_NAME = "data/indexes.dat";
    private Map<String, Index> indexes = new HashMap<>();

    public void createIndex(String indexName, Table table, String columnName) {
        if (indexes.containsKey(indexName)) {
            throw new IllegalArgumentException("Индекс уже существует: " + indexName);
        }

        boolean columnExists = false;
        for (Column column : table.getColumns()) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                columnExists = true;
                break;
            }
        }

        if (!columnExists) {
            throw new IllegalArgumentException("Столбец не найден: " + columnName);
        }

        String primaryKeyColumn = null;
        for (Column column : table.getColumns()) {
            if (column.isPrimaryKey()) {
                primaryKeyColumn = column.getName();
                break;
            }
        }

        if (primaryKeyColumn == null) {
            throw new IllegalArgumentException("Для индексирования требуется первичный ключ");
        }

        Index index = new Index(indexName, table.getName(), columnName, primaryKeyColumn);
        indexes.put(indexName, index);
        rebuildIndex(index, table);
        saveIndexes();
    }

    private void rebuildIndex(Index index, Table table) {
        index.clear();

        for (Row row : table.getRows()) {
            Object indexedValue = row.getValue(index.getColumnName());
            Object primaryKeyValue = row.getValue(index.getPrimaryKeyColumn());
            index.add(indexedValue, primaryKeyValue);
        }
    }

    public void refreshTableIndexes(Table table) {
        for (Index index : indexes.values()) {
            if (index.getTableName().equalsIgnoreCase(table.getName())) {
                rebuildIndex(index, table);
            }
        }

        saveIndexes();
    }

    public List<Row> findRows(
            Database database,
            String tableName,
            String columnName,
            String operator,
            String conditionValue) {
        Index foundIndex = null;

        for (Index index : indexes.values()) {
            if (index.getTableName().equalsIgnoreCase(tableName)
                    && index.getColumnName().equalsIgnoreCase(columnName)) {
                foundIndex = index;
                break;
            }
        }

        if (foundIndex == null) {
            return null;
        }

        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException("Таблица не найдена: " + tableName);
        }

        Set<Object> matchingKeys = new HashSet<>();

        for (Map.Entry<Object, List<Object>> entry : foundIndex.getEntries().entrySet()) {
            if (matches(entry.getKey(), operator, conditionValue)) {
                matchingKeys.addAll(entry.getValue());
            }
        }

        List<Row> result = new ArrayList<>();

        for (Object key : matchingKeys) {
            Row row = table.getRowByPrimaryKey(key);

            if (row != null) {
                result.add(row);
            }
        }

        return result;
    }

    private boolean matches(Object actual, String operator, String expectedText) {
        if (actual == null) {
            return false;
        }

        Object expected = convertValue(expectedText, actual);
        int comparison;

        if (actual instanceof Number && expected instanceof Number) {
            comparison =
                    Double.compare(
                            ((Number) actual).doubleValue(), ((Number) expected).doubleValue());
        } else if (actual instanceof Boolean && expected instanceof Boolean) {
            comparison = Boolean.compare((Boolean) actual, (Boolean) expected);
        } else {
            comparison = actual.toString().compareTo(expected.toString());
        }

        switch (operator) {
            case "=":
                return comparison == 0;
            case "!=":
            case "<>":
                return comparison != 0;
            case ">":
                return comparison > 0;
            case "<":
                return comparison < 0;
            case ">=":
                return comparison >= 0;
            case "<=":
                return comparison <= 0;
            default:
                throw new IllegalArgumentException("Неизвестный оператор: " + operator);
        }
    }

    private Object convertValue(String value, Object sample) {
        String text = value.trim();

        if (text.length() >= 2 && text.startsWith("'") && text.endsWith("'")) {
            text = text.substring(1, text.length() - 1);
        }

        if (sample instanceof Integer) {
            return Integer.valueOf(text);
        }

        if (sample instanceof Long) {
            return Long.valueOf(text);
        }

        if (sample instanceof Double) {
            return Double.valueOf(text);
        }

        if (sample instanceof Float) {
            return Float.valueOf(text);
        }

        if (sample instanceof Boolean) {
            return Boolean.valueOf(text);
        }

        return text;
    }

    public void removeTableIndexes(String tableName) {
        List<String> toRemove = new ArrayList<>();

        for (Index index : indexes.values()) {
            if (index.getTableName().equalsIgnoreCase(tableName)) {
                toRemove.add(index.getIndexName());
            }
        }

        for (String indexName : toRemove) {
            indexes.remove(indexName);
        }

        saveIndexes();
    }

    public void saveIndexes() {
        try {
            File file = new File(FILE_NAME);
            File parent = file.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
                out.writeObject(indexes);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения индексов", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadIndexes() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            indexes = new HashMap<>();
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            indexes = (Map<String, Index>) in.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки индексов", e);
        }
    }

    public void rebuildAllIndexes(Database database) {
        for (Index index : indexes.values()) {
            Table table = database.getTable(index.getTableName());

            if (table != null) {
                rebuildIndex(index, table);
            }
        }

        saveIndexes();
    }

    public Map<String, Index> getIndexes() {
        return indexes;
    }
}
