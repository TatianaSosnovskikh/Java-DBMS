import database.*;

import executor.CrudHandler;
import executor.QueryExecutor;

import java.util.List;

/**
 * Набор интеграционных тестов для проверки основных CRUD-операций, типов данных, ограничений
 * первичного ключа и управления таблицами.
 */
public class CrudTest {
    private static int passed = 0;
    private static int failed = 0;

    /**
     * Запускает последовательность тестов для проверки работы {@link QueryExecutor} и операций над
     * таблицами базы данных.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        Database database = new Database();

        Table goods = new Table("goods");
        goods.addColumn(new Column("id", DataType.INTEGER, true));
        goods.addColumn(new Column("name", DataType.STRING, false));
        goods.addColumn(new Column("price", DataType.DOUBLE, false));
        database.createTable(goods);

        QueryExecutor executor = new QueryExecutor(database, new CrudHandler());

        // DATE и BOOLEAN
        Table typeTest = new Table("type_test");
        typeTest.addColumn(new Column("id", DataType.INTEGER, true));
        typeTest.addColumn(new Column("created", DataType.DATE, false));
        typeTest.addColumn(new Column("active", DataType.BOOLEAN, false));
        database.createTable(typeTest);

        executor.execute(
                "INSERT INTO type_test (id, created, active) " + "VALUES (1, '2026-11-15', true)");

        List<Row> typeRows = (List<Row>) executor.execute("SELECT * FROM type_test");

        check(typeRows.size() == 1, "DATE/BOOLEAN: добавление записи");
        check(
                typeRows.get(0).getValue("created").toString().equals("2026-11-15"),
                "DATE: сохранение даты");
        check(Boolean.TRUE.equals(typeRows.get(0).getValue("active")), "BOOLEAN: сохранение true");

        // INSERT
        executor.execute("INSERT INTO goods (id, name, price) " + "VALUES (1, 'Apple', 100.0)");

        // Проверка дубликата первичного ключа
        boolean duplicateRejected = false;

        try {
            executor.execute(
                    "INSERT INTO goods (id, name, price) " + "VALUES (1, 'Banana', 200.0)");
        } catch (Exception e) {
            duplicateRejected = true;
        }

        check(duplicateRejected, "PRIMARY KEY: дубликат отклонён");

        List<Row> rows = (List<Row>) executor.execute("SELECT * FROM goods");
        check(rows.size() == 1, "INSERT: добавление записи");

        // SELECT
        check(rows.get(0).getValue("name").equals("Apple"), "SELECT: чтение записи");

        // UPDATE
        executor.execute("UPDATE goods SET price = 120.0 WHERE id = 1");

        rows = (List<Row>) executor.execute("SELECT * FROM goods");
        check(rows.get(0).getValue("price").equals(120.0), "UPDATE: изменение цены");

        // DELETE
        executor.execute("DELETE FROM goods WHERE id = 1");

        rows = (List<Row>) executor.execute("SELECT * FROM goods");
        check(rows.isEmpty(), "DELETE: удаление записи");

        // CREATE TABLE
        executor.execute(
                "CREATE TABLE integration_test ("
                        + "id INTEGER PRIMARY KEY, "
                        + "name STRING, "
                        + "price DOUBLE)");

        check(database.getTable("integration_test") != null, "CREATE TABLE: создание таблицы");

        // INSERT нескольких записей
        executor.execute(
                "INSERT INTO integration_test (id, name, price) VALUES "
                        + "(1, 'Apple', 100.0), "
                        + "(2, 'Banana', 50.0), "
                        + "(3, 'Orange', 80.0)");

        List<Row> integrationRows = (List<Row>) executor.execute("SELECT * FROM integration_test");

        check(integrationRows.size() == 3, "INSERT: несколько записей");

        // SELECT WHERE
        List<Row> filteredRows =
                (List<Row>) executor.execute("SELECT * FROM integration_test WHERE price > 60");

        check(filteredRows.size() == 2, "SELECT WHERE: фильтрация");

        // UPDATE WHERE
        executor.execute("UPDATE integration_test SET price = 70.0 WHERE id = 2");

        List<Row> updatedRows =
                (List<Row>) executor.execute("SELECT * FROM integration_test WHERE id = 2");

        check(
                updatedRows.size() == 1 && updatedRows.get(0).getValue("price").equals(70.0),
                "UPDATE WHERE: изменение записи");

        // DELETE WHERE
        executor.execute("DELETE FROM integration_test WHERE id = 1");

        List<Row> remainingRows = (List<Row>) executor.execute("SELECT * FROM integration_test");

        check(remainingRows.size() == 2, "DELETE WHERE: удаление записи");

        // DROP TABLE
        executor.execute("DROP TABLE integration_test");

        check(database.getTable("integration_test") == null, "DROP TABLE: удаление таблицы");

        System.out.println();
        System.out.println("ИТОГ: " + passed + " успешно, " + failed + " с ошибками");
    }

    /**
     * Проверяет выполнение тестового условия, выводит результат в консоль и обновляет счётчики
     * пройденных или проваленных проверок.
     *
     * @param condition результат проверяемого условия
     * @param description текстовое описание тестового сценария
     */
    private static void check(boolean condition, String description) {
        if (condition) {
            System.out.println("[OK] " + description);
            passed++;
        } else {
            System.out.println("[FAIL] " + description);
            failed++;
        }
    }
}
