
package executor;

import database.Column;
import database.DataType;
import database.Database;
import database.Row;
import database.Table;
import index.IndexManager;
import parser.ParsedQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
Обработчик операций INSERT, SELECT, UPDATE и DELETE.
*/
public class CrudHandler implements QueryExecutor.QueryHandler {

    @Override
    public Object execute(
            ParsedQuery query,
            Database database,
            IndexManager indexManager
    ) {
        Table table = database.getTable(query.getTableName());

        if (table == null) {
            throw new IllegalArgumentException("Таблица не найдена");
        }

        if (query.getQueryType() == ParsedQuery.QueryType.INSERT) {
            return insert(query, table, indexManager);
        }

        if (query.getQueryType() == ParsedQuery.QueryType.SELECT) {
            return select(query, table);
        }

        if (query.getQueryType() == ParsedQuery.QueryType.UPDATE) {
            return update(query, table, indexManager);
        }

        if (query.getQueryType() == ParsedQuery.QueryType.DELETE) {
            return delete(query, table, indexManager);
        }

        throw new UnsupportedOperationException(
                "Операция пока не реализована"
        );
    }

    /*
    Добавление записей в таблицу.
    */
    private String insert(
            ParsedQuery query,
            Table table,
            IndexManager indexManager
    ) {
        List<Column> columns = table.getColumns();
        List<String> insertColumns = query.getColumns();
        List<List<String>> rows = query.getRowsValues();

        int count = 0;

        for (List<String> values : rows) {
            if (insertColumns.isEmpty() && values.size() != columns.size()) {
                throw new IllegalArgumentException(
                        "Неверное количество значений"
                );
            }

            if (!insertColumns.isEmpty()
                    && insertColumns.size() != values.size()) {
                throw new IllegalArgumentException(
                        "Неверное количество значений"
                );
            }

            Row row = new Row();

            for (Column column : columns) {
                row.setValue(column.getName(), null);
            }

            for (int i = 0; i < values.size(); i++) {
                Column column;

                if (insertColumns.isEmpty()) {
                    column = columns.get(i);
                } else {
                    column = findColumn(table, insertColumns.get(i));
                }

                row.setValue(
                        column.getName(),
                        convertValue(values.get(i), column.getType())
                );
            }

            for (Column column : columns) {
                if (column.isPrimaryKey()
                        && row.getValue(column.getName()) == null) {
                    throw new IllegalArgumentException(
                            "Первичный ключ не может быть пустым"
                    );
                }
            }

            if (!table.addRow(row)) {
                throw new IllegalArgumentException(
                        "Запись с таким первичным ключом уже существует"
                );
            }

            count++;
        }

        return "Добавлено записей: " + count;
    }

    /*
    Выборка записей из таблицы.
    */
    private List<Row> select(ParsedQuery query, Table table) {
        List<Row> result = new ArrayList<>();
        List<Row> rows;

        if (query.isIndexUsed()) {
            rows = query.getCandidateRows();
        } else {
            rows = table.getRows();
        }

        for (Row row : rows) {
            if (query.getConditionColumn() == null
                    || checkCondition(query, row, table)) {

                if (query.getColumns().contains("*")) {
                    result.add(row);
                } else {
                    Row selectedRow = new Row();

                    for (String columnName : query.getColumns()) {
                        Column column = findColumn(table, columnName);

                        selectedRow.setValue(
                                column.getName(),
                                row.getValue(column.getName())
                        );
                    }

                    result.add(selectedRow);
                }
            }
        }

        return result;
    }

    /*
    Обновление записей таблицы.
    */
    private String update(
            ParsedQuery query,
            Table table,
            IndexManager indexManager
    ) {
        int count = 0;

        for (Row row : table.getRows()) {
            if (query.getConditionColumn() == null
                    || checkCondition(query, row, table)) {

                for (Map.Entry<String, String> assignment
                        : query.getAssignments().entrySet()) {

                    Column column = findColumn(table, assignment.getKey());

                    Object newValue = convertValue(
                            assignment.getValue(),
                            column.getType()
                    );

                    if (column.isPrimaryKey()) {
                        for (Row otherRow : table.getRows()) {
                            if (otherRow != row
                                    && newValue.equals(
                                    otherRow.getValue(column.getName()))) {
                                throw new IllegalArgumentException(
                                        "Запись с таким первичным ключом уже существует"
                                );
                            }
                        }
                    }

                    row.setValue(column.getName(), newValue);
                }

                count++;
            }
        }

        return "Обновлено записей: " + count;
    }

    /*
    Удаление записей из таблицы.
    */
    private String delete(
            ParsedQuery query,
            Table table,
            IndexManager indexManager
    ) {
        int count = 0;

        Iterator<Row> iterator = table.getRows().iterator();

        while (iterator.hasNext()) {
            Row row = iterator.next();

            if (query.getConditionColumn() == null
                    || checkCondition(query, row, table)) {
                iterator.remove();
                count++;
            }
        }

        return "Удалено записей: " + count;
    }

    /*
    Проверка условия WHERE.
    */
    private boolean checkCondition(
            ParsedQuery query,
            Row row,
            Table table
    ) {
        Column column = findColumn(table, query.getConditionColumn());

        Object rowValue = row.getValue(column.getName());
        Object conditionValue = convertValue(
                query.getConditionValue(),
                column.getType()
        );

        if (rowValue == null || conditionValue == null) {
            return false;
        }

        int comparison = compareValues(
                rowValue,
                conditionValue,
                column.getType()
        );

        String operator = query.getConditionOperator();

        if (operator.equals("=")) {
            return comparison == 0;
        }

        if (operator.equals("!=") || operator.equals("<>")) {
            return comparison != 0;
        }

        if (operator.equals(">")) {
            return comparison > 0;
        }

        if (operator.equals("<")) {
            return comparison < 0;
        }

        if (operator.equals(">=")) {
            return comparison >= 0;
        }

        if (operator.equals("<=")) {
            return comparison <= 0;
        }

        throw new IllegalArgumentException(
                "Неизвестный оператор: " + operator
        );
    }

    /*
    Сравнение значений.
    */
    private int compareValues(
            Object first,
            Object second,
            DataType type
    ) {
        switch (type) {
            case INTEGER:
                return Integer.compare(
                        (Integer) first,
                        (Integer) second
                );
            case DOUBLE:
                return Double.compare(
                        (Double) first,
                        (Double) second
                );
            case STRING:
                return ((String) first).compareTo((String) second);
            case DATE:
                return ((LocalDate) first).compareTo((LocalDate) second);
            case BOOLEAN:
                return Boolean.compare(
                        (Boolean) first,
                        (Boolean) second
                );
            default:
                throw new IllegalArgumentException(
                        "Неизвестный тип данных"
                );
        }
    }

    /*
    Поиск столбца по названию.
    */
    private Column findColumn(Table table, String name) {
        for (Column column : table.getColumns()) {
            if (column.getName().equalsIgnoreCase(name)) {
                return column;
            }
        }

        throw new IllegalArgumentException(
                "Столбец не найден: " + name
        );
    }

    /*
    Преобразование значения к нужному типу.
    */
    private Object convertValue(String value, DataType type) {
        String text = value.trim();

        if (text.startsWith("'") && text.endsWith("'")) {
            text = text.substring(1, text.length() - 1);
        }

        switch (type) {
            case INTEGER:
                return Integer.parseInt(text);
            case DOUBLE:
                return Double.parseDouble(text);
            case STRING:
                return text;
            case DATE:
                return LocalDate.parse(text);
            case BOOLEAN:
                if (!text.equalsIgnoreCase("true")
                        && !text.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException(
                            "Неверное значение BOOLEAN"
                    );
                }
                return Boolean.parseBoolean(text);
            default:
                throw new IllegalArgumentException(
                        "Неизвестный тип данных"
                );
        }
    }
}
