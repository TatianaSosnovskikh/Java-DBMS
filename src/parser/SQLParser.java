package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLParser {
    public ParsedQuery parse(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("Пустая SQL-команда");
        }
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        if (sql.matches("(?is)^INSERT\\s+INTO\\b.*")) {
            return parseInsert(sql);
        }
        if (sql.matches("(?is)^SELECT\\b.*")) {
            return parseSelect(sql);
        }
        if (sql.matches("(?is)^UPDATE\\b.*")) {
            return parseUpdate(sql);
        }
        if (sql.matches("(?is)^DELETE\\s+FROM\\b.*")) {
            return parseDelete(sql);
        }
        if (sql.matches("(?is)^CREATE\\s+INDEX\\b.*")) {
            return parseCreateIndex(sql);
        }
        if (sql.matches("(?is)^CREATE\\s+TABLE\\b.*")) {
            return parseCreateTable(sql);
        }
        if (sql.matches("(?is)^DROP\\s+TABLE\\b.*")) {
            return parseDropTable(sql);
        }
        throw new IllegalArgumentException("Неизвестная SQL-команда: " + sql);
    }

    private ParsedQuery parseInsert(String sql) {
        Pattern pattern =
                Pattern.compile(
                        "(?is)^INSERT\\s+INTO\\s+(\\w+)\\s*(?:\\((.*?)\\))?\\s+VALUES\\s+(.+)$");
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат INSERT");
        }
        ParsedQuery query = new ParsedQuery();
        query.setQueryType(ParsedQuery.QueryType.INSERT);
        query.setTableName(matcher.group(1));
        if (matcher.group(2) != null) {
            query.setColumns(splitCommaSeparated(matcher.group(2)));
        }
        List<List<String>> rows = splitValueTuples(matcher.group(3));
        query.setRowsValues(rows);
        if (!rows.isEmpty()) {
            query.setValues(rows.get(0));
        }
        for (List<String> row : rows) {
            if (!query.getColumns().isEmpty() && query.getColumns().size() != row.size()) {
                throw new IllegalArgumentException(
                        "Количество столбцов не совпадает с количеством значений");
            }
        }
        return query;
    }

    private ParsedQuery parseSelect(String sql) {
        Pattern pattern =
                Pattern.compile("(?is)^SELECT\\s+(.*?)\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(.+))?$");
        Matcher matcher = pattern.matcher(sql);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат SELECT");
        }
        ParsedQuery query = new ParsedQuery();
        query.setQueryType(ParsedQuery.QueryType.SELECT);
        query.setTableName(matcher.group(2));
        String columnsPart = matcher.group(1).trim();
        if ("*".equals(columnsPart)) {
            query.getColumns().add("*");
        } else {
            query.setColumns(splitCommaSeparated(columnsPart));
        }
        if (matcher.group(3) != null) {
            parseCondition(matcher.group(3), query);
        }
        return query;
    }

    private ParsedQuery parseUpdate(String sql) {
        Pattern pattern =
                Pattern.compile("(?is)^UPDATE\\s+(\\w+)\\s+SET\\s+(.+?)(?:\\s+WHERE\\s+(.+))?$");
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат UPDATE");
        }
        ParsedQuery query = new ParsedQuery();
        query.setQueryType(ParsedQuery.QueryType.UPDATE);
        query.setTableName(matcher.group(1));
        List<String> assignments = splitCommaSeparated(matcher.group(2));
        for (String assignment : assignments) {
            int separator = assignment.indexOf('=');
            if (separator < 1) {
                throw new IllegalArgumentException("Неверное присваивание: " + assignment);
            }
            String column = assignment.substring(0, separator).trim();
            String value = assignment.substring(separator + 1).trim();
            if (column.isEmpty() || value.isEmpty()) {
                throw new IllegalArgumentException("Неверное присваивание: " + assignment);
            }
            query.getAssignments().put(column, value);
        }
        if (matcher.group(3) != null) {
            parseCondition(matcher.group(3), query);
        }
        return query;
    }

    private ParsedQuery parseDelete(String sql) {
        Pattern pattern = Pattern.compile("(?is)^DELETE\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(.+))?$");
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат DELETE");
        }
        ParsedQuery query = new ParsedQuery();
        query.setQueryType(ParsedQuery.QueryType.DELETE);
        query.setTableName(matcher.group(1));
        if (matcher.group(2) != null) {
            parseCondition(matcher.group(2), query);
        }
        return query;
    }

    private void parseCondition(String condition, ParsedQuery query) {
        Pattern pattern = Pattern.compile("(?is)^\\s*(\\w+)\\s*(>=|<=|<>|!=|=|>|<)\\s*(.+?)\\s*$");
        Matcher matcher = pattern.matcher(condition);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверное условие WHERE: " + condition);
        }
        query.setConditionColumn(matcher.group(1));
        query.setConditionOperator(matcher.group(2));
        query.setConditionValue(matcher.group(3).trim());
    }

    private ParsedQuery parseCreateIndex(String sql) {
        Pattern pattern =
                Pattern.compile(
                        "(?is)^CREATE\\s+INDEX\\s+(\\w+)\\s+ON\\s+(\\w+)\\s*\\(\\s*(\\w+)\\s*\\)$");
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат CREATE INDEX");
        }
        ParsedQuery query = new ParsedQuery();
        query.setQueryType(ParsedQuery.QueryType.CREATE_INDEX);
        query.setIndexName(matcher.group(1));
        query.setTableName(matcher.group(2));
        query.setIndexedColumn(matcher.group(3));
        return query;
    }

    private ParsedQuery parseCreateTable(String sql) {
        Pattern pattern = Pattern.compile("(?is)^CREATE\\s+TABLE\\s+(\\w+)\\s*\\((.*)\\)$");
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат CREATE TABLE");
        }
        ParsedQuery query = new ParsedQuery();
        query.setQueryType(ParsedQuery.QueryType.CREATE_TABLE);
        query.setTableName(matcher.group(1));
        query.setColumnDefinitions(splitCommaSeparated(matcher.group(2)));
        return query;
    }

    private ParsedQuery parseDropTable(String sql) {
        Pattern pattern = Pattern.compile("(?is)^DROP\\s+TABLE\\s+(\\w+)$");
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Неверный формат DROP TABLE");
        }
        ParsedQuery query = new ParsedQuery();
        query.setQueryType(ParsedQuery.QueryType.DROP_TABLE);
        query.setTableName(matcher.group(1));
        return query;
    }

    private List<String> splitCommaSeparated(String text) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\'') {
                insideQuotes = !insideQuotes;
            }
            if (ch == ',' && !insideQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (insideQuotes) {
            throw new IllegalArgumentException("Незакрытая строковая кавычка");
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        return result;
    }

    private List<List<String>> splitValueTuples(String text) {
        List<List<String>> result = new ArrayList<>();
        boolean insideQuotes = false;
        int depth = 0;
        int start = -1;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\'') {
                insideQuotes = !insideQuotes;
            }
            if (!insideQuotes) {
                if (ch == '(') {
                    if (depth == 0) {
                        start = i + 1;
                    }
                    depth++;
                } else if (ch == ')') {
                    depth--;
                    if (depth < 0) {
                        throw new IllegalArgumentException("Неверная структура VALUES");
                    }
                    if (depth == 0) {
                        result.add(splitCommaSeparated(text.substring(start, i)));
                    }
                }
            }
        }
        if (insideQuotes || depth != 0 || !result.isEmpty() == false) {
            throw new IllegalArgumentException("Неверная структура VALUES");
        }
        return result;
    }
}
