package executor;

import database.Column;
import database.DataType;
import database.Database;
import database.ForeignKey;
import database.Table;
import index.IndexManager;
import parser.ParsedQuery;
import parser.SQLParser;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryExecutor {

    public interface QueryHandler {
        Object execute(ParsedQuery query, Database database, IndexManager indexManager);
    }

    private final Database database;
    private final SQLParser parser;
    private final IndexManager indexManager;
    private QueryHandler queryHandler;

    public QueryExecutor(Database database) {
        this(database, null);
    }

    public QueryExecutor(Database database, QueryHandler queryHandler) {
        if (database == null) {
            throw new IllegalArgumentException("База данных не может быть null");
        }

        this.database = database;
        this.queryHandler = queryHandler;
        this.parser = new SQLParser();
        this.indexManager = new IndexManager();

        indexManager.loadIndexes();
        indexManager.rebuildAllIndexes(database);
    }

    public void setQueryHandler(QueryHandler queryHandler) {
        this.queryHandler = queryHandler;
    }

    public Object execute(String sql) {
        ParsedQuery query = parser.parse(sql);
        return execute(query);
    }

    public Object execute(ParsedQuery query) {
        switch (query.getQueryType()) {
            case CREATE_TABLE:
                return createTable(query);
            case DROP_TABLE:
                return dropTable(query);
            case CREATE_INDEX:
                return createIndex(query);
            case INSERT:
            case UPDATE:
            case DELETE:
            case SELECT:
                return executeCrudQuery(query);
            default:
                throw new IllegalArgumentException("Неподдерживаемый тип запроса");
        }
    }

    private String createTable(ParsedQuery query) {
        String tableName = query.getTableName();

        if (database.getTable(tableName) != null) {
            throw new IllegalArgumentException("Таблица уже существует: " + tableName);
        }

        Table table = new Table(tableName);

        Pattern foreignKeyPattern = Pattern.compile(
                "(?i)^FOREIGN\\s+KEY\\s*\\(\\s*(\\w+)\\s*\\)"
                        + "\\s+REFERENCES\\s+(\\w+)\\s*\\(\\s*(\\w+)\\s*\\)$"
        );

        // Создаём обычные столбцы.
        for (String definition : query.getColumnDefinitions()) {
            String trimmed = definition.trim();

            if (trimmed.toUpperCase(Locale.ROOT).startsWith("FOREIGN KEY")) {
                continue;
            }

            String[] parts = trimmed.split("\\s+");

            if (parts.length < 2) {
                throw new IllegalArgumentException(
                        "Неверное описание столбца: " + definition
                );
            }

            String columnName = parts[0];
            String typeName = parts[1].toUpperCase(Locale.ROOT);

            DataType dataType;

            try {
                dataType = DataType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Неизвестный тип данных: " + typeName
                );
            }

            boolean primaryKey = false;

            for (int i = 2; i < parts.length; i++) {
                if (parts[i].equalsIgnoreCase("PRIMARY")) {
                    if (i + 1 >= parts.length
                            || !parts[i + 1].equalsIgnoreCase("KEY")) {
                        throw new IllegalArgumentException(
                                "Неверное описание PRIMARY KEY"
                        );
                    }

                    primaryKey = true;
                    i++;
                }
            }

            table.addColumn(new Column(columnName, dataType, primaryKey));
        }

        if (table.getColumns().isEmpty()) {
            throw new IllegalArgumentException(
                    "Таблица должна содержать хотя бы один столбец"
            );
        }

        // Обрабатываем внешние ключи.
        for (String definition : query.getColumnDefinitions()) {
            String trimmed = definition.trim();

            if (!trimmed.toUpperCase(Locale.ROOT).startsWith("FOREIGN KEY")) {
                continue;
            }

            Matcher matcher = foreignKeyPattern.matcher(trimmed);

            if (!matcher.matches()) {
                throw new IllegalArgumentException(
                        "Неверное описание внешнего ключа: " + definition
                );
            }

            String columnName = matcher.group(1);
            String referencedTableName = matcher.group(2);
            String referencedColumnName = matcher.group(3);

            Column localColumn = null;

            for (Column column : table.getColumns()) {
                if (column.getName().equalsIgnoreCase(columnName)) {
                    localColumn = column;
                    break;
                }
            }

            if (localColumn == null) {
                throw new IllegalArgumentException(
                        "Столбец внешнего ключа не найден: " + columnName
                );
            }

            Table referencedTable = database.getTable(referencedTableName);

            if (referencedTable == null) {
                throw new IllegalArgumentException(
                        "Таблица для внешнего ключа не найдена: "
                                + referencedTableName
                );
            }

            Column referencedColumn = null;

            for (Column column : referencedTable.getColumns()) {
                if (column.getName().equalsIgnoreCase(referencedColumnName)) {
                    referencedColumn = column;
                    break;
                }
            }

            if (referencedColumn == null) {
                throw new IllegalArgumentException(
                        "Столбец для ссылки не найден: " + referencedColumnName
                );
            }

            if (!referencedColumn.isPrimaryKey()) {
                throw new IllegalArgumentException(
                        "Внешний ключ должен ссылаться на первичный ключ"
                );
            }

            if (localColumn.getType() != referencedColumn.getType()) {
                throw new IllegalArgumentException(
                        "Типы столбцов внешнего ключа не совпадают"
                );
            }

            table.addForeignKey(
                    new ForeignKey(
                            columnName,
                            referencedTableName,
                            referencedColumnName
                    )
            );
        }

        database.createTable(table);

        return "Таблица " + tableName + " создана";
    }

    private String dropTable(ParsedQuery query) {
        String tableName = query.getTableName();

        Table table = database.getTable(tableName);

        if (table == null) {
            throw new IllegalArgumentException(
                    "Таблица не найдена: " + tableName
            );
        }

        for (Table otherTable : database.getTables().values()) {
            for (ForeignKey foreignKey : otherTable.getForeignKeys()) {
                if (foreignKey.getReferencedTable()
                        .equalsIgnoreCase(tableName)) {
                    throw new IllegalArgumentException(
                            "Нельзя удалить таблицу: на неё ссылается "
                                    + otherTable.getName()
                    );
                }
            }
        }

        database.removeTable(tableName);
        indexManager.removeTableIndexes(tableName);

        return "Таблица " + tableName + " удалена";
    }

    private String createIndex(ParsedQuery query) {
        Table table = database.getTable(query.getTableName());

        if (table == null) {
            throw new IllegalArgumentException(
                    "Таблица не найдена: " + query.getTableName()
            );
        }

        indexManager.createIndex(
                query.getIndexName(),
                table,
                query.getIndexedColumn()
        );

        return "Индекс " + query.getIndexName() + " создан";
    }

    private Object executeCrudQuery(ParsedQuery query) {
        if (queryHandler == null) {
            throw new IllegalStateException("CRUD-обработчик ещё не подключён");
        }

        if (query.getQueryType() == ParsedQuery.QueryType.SELECT
                && query.getConditionColumn() != null) {

            List<database.Row> indexedRows = indexManager.findRows(
                    database,
                    query.getTableName(),
                    query.getConditionColumn(),
                    query.getConditionOperator(),
                    query.getConditionValue()
            );

            if (indexedRows != null) {
                query.setCandidateRows(indexedRows);
                query.setIndexUsed(true);
            }
        }

        Object result = queryHandler.execute(query, database, indexManager);

        if (query.getQueryType() == ParsedQuery.QueryType.INSERT
                || query.getQueryType() == ParsedQuery.QueryType.UPDATE
                || query.getQueryType() == ParsedQuery.QueryType.DELETE) {

            Table table = database.getTable(query.getTableName());

            if (table != null) {
                indexManager.refreshTableIndexes(table);
            }
        }

        return result;
    }

    public Database getDatabase() {
        return database;
    }

    public IndexManager getIndexManager() {
        return indexManager;
    }
}
