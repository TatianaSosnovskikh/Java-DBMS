import parser.ParsedQuery;
import parser.SQLParser;

public class ParserTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        SQLParser parser = new SQLParser();

        check(
                parser,
                "INSERT INTO goods VALUES (1, 'Toyota', 25000.0, TRUE)",
                q ->
                        q.getTableName().equals("goods")
                                && q.getRowsValues().size() == 1
                                && q.getRowsValues().get(0).size() == 4,
                "INSERT: таблица и значения");

        check(
                parser,
                "INSERT INTO goods (id, name) VALUES (2, 'BMW')",
                q ->
                        q.getTableName().equals("goods")
                                && q.getColumns().size() == 2
                                && q.getRowsValues().size() == 1,
                "INSERT: указанные столбцы");

        check(
                parser,
                "INSERT INTO goods VALUES (1, 'Toyota'), (2, 'BMW')",
                q -> q.getRowsValues().size() == 2,
                "INSERT: несколько записей");

        check(
                parser,
                "SELECT name, price FROM goods WHERE price > 20000",
                q ->
                        q.getTableName().equals("goods")
                                && q.getColumns().size() == 2
                                && q.getConditionColumn().equals("price")
                                && q.getConditionOperator().equals(">")
                                && q.getConditionValue().equals("20000"),
                "SELECT: столбцы и условие");

        check(
                parser,
                "UPDATE goods SET price = 30000 WHERE id = 2",
                q ->
                        q.getTableName().equals("goods")
                                && q.getAssignments().containsKey("price")
                                && q.getConditionColumn().equals("id")
                                && q.getConditionValue().equals("2"),
                "UPDATE: изменения и условие");

        check(
                parser,
                "DELETE FROM goods WHERE name = 'Toyota'",
                q ->
                        q.getTableName().equals("goods")
                                && q.getConditionColumn().equals("name")
                                && q.getConditionValue().equals("'Toyota'"),
                "DELETE: условие");

        check(
                parser,
                "CREATE INDEX idx_goods_name ON goods (name)",
                q ->
                        q.getIndexName().equals("idx_goods_name")
                                && q.getTableName().equals("goods")
                                && q.getIndexedColumn().equals("name"),
                "CREATE INDEX: параметры индекса");

        check(
                parser,
                "CREATE TABLE shops (id INT, name VARCHAR(50))",
                q -> q.getTableName().equals("shops") && q.getColumnDefinitions().size() == 2,
                "CREATE TABLE: описание столбцов");

        check(
                parser,
                "DROP TABLE shops",
                q -> q.getTableName().equals("shops"),
                "DROP TABLE: название таблицы");

        System.out.println();
        System.out.println("ИТОГ: " + passed + " успешно, " + failed + " с ошибками");
    }

    private interface QueryCheck {
        boolean test(ParsedQuery query);
    }

    private static void check(
            SQLParser parser, String sql, QueryCheck condition, String description) {
        try {
            ParsedQuery query = parser.parse(sql);

            if (condition.test(query)) {
                System.out.println("[OK] " + description);
                passed++;
            } else {
                System.out.println("[FAIL] " + description);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] " + description + ": " + e.getMessage());
            failed++;
        }
    }
}
