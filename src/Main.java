import database.*;
import storage.DatabaseStorage;
import executor.CrudHandler;
import executor.QueryExecutor;
import database.Row;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Подключаем хранилище и загружаем базу
        DatabaseStorage storage = new DatabaseStorage();
        Database database = storage.load();

        // Создаём таблицу categories, если её нет
        if (database.getTable("categories") == null) {
            Table categories = new Table("categories");

            categories.addColumn(new Column("id", DataType.INTEGER, true));
            categories.addColumn(new Column("name", DataType.STRING, false));

            database.createTable(categories);
        }

        // Создаём таблицу goods, если её нет
        if (database.getTable("goods") == null) {
            Table goods = new Table("goods");

            goods.addColumn(new Column("id", DataType.INTEGER, true));
            goods.addColumn(new Column("categoryId", DataType.INTEGER, false));
            goods.addColumn(new Column("name", DataType.STRING, false));
            goods.addColumn(new Column("price", DataType.DOUBLE, false));

            database.createTable(goods);
        }

        // Создаём обработчик SQL-запросов
        CrudHandler crudHandler = new CrudHandler();
        QueryExecutor queryExecutor =
                new QueryExecutor(database, crudHandler);

        Table categories = database.getTable("categories");
        Table goods = database.getTable("goods");

        // Добавляем категорию, если таблица пустая
        if (categories.getRows().isEmpty()) {
            System.out.println("\n=== INSERT: добавление категории ===");

            System.out.println(queryExecutor.execute(
                    "INSERT INTO categories (id, name) VALUES (1, 'Food')"
            ));

            storage.save(database);
        }

        // Демонстрацию CRUD проводим на пустой таблице
        if (goods.getRows().isEmpty()) {

            System.out.println("\n=== INSERT: добавление товара ===");

            System.out.println(queryExecutor.execute(
                    "INSERT INTO goods (id, categoryId, name, price) VALUES (1, 1, 'Apple', 100.0)"
            ));

            storage.save(database);

            System.out.println("\n=== SELECT: просмотр товара ===");

            printSelectResult(
                    queryExecutor.execute(
                            "SELECT id, categoryId, name, price FROM goods WHERE id = 1"
                    ),
                    goods
            );

            System.out.println("\n=== UPDATE: изменение цены ===");

            System.out.println(queryExecutor.execute(
                    "UPDATE goods SET price = 120.0 WHERE id = 1"
            ));

            storage.save(database);

            System.out.println("\n=== SELECT: проверка изменения ===");

            printSelectResult(
                    queryExecutor.execute(
                            "SELECT id, categoryId, name, price FROM goods WHERE id = 1"
                    ),
                    goods
            );

            System.out.println("\n=== DELETE: удаление товара ===");

            System.out.println(queryExecutor.execute(
                    "DELETE FROM goods WHERE id = 1"
            ));

            storage.save(database);

            System.out.println("\n=== SELECT: проверка удаления ===");

            printSelectResult(
                    queryExecutor.execute(
                            "SELECT id, categoryId, name, price FROM goods WHERE id = 1"
                    ),
                    goods
            );

        } else {
            System.out.println("\nВ таблице goods уже есть данные.");
            System.out.println("Выполняем SELECT без изменения существующих записей.");

            printSelectResult(
                    queryExecutor.execute(
                            "SELECT id, categoryId, name, price FROM goods WHERE id = 1"
                    ),
                    goods
            );
        }

        System.out.println("\n=== Таблицы базы данных ===");
        System.out.println(database.getTables().keySet());

        System.out.println("\n=== Текущее содержимое goods ===");
        printTable(goods);

        // Финальное сохранение базы
        storage.save(database);
    }


    private static void printTable(Table table) {
        // Заголовки
        for (Column column : table.getColumns()) {
            System.out.printf("| %-15s ", column.getName());
        }
        System.out.println("|");

        // Разделитель
        for (Column column : table.getColumns()) {
            System.out.print("-------------------");
        }
        System.out.println();

        // Строки таблицы
        for (Row row : table.getRows()) {
            for (Column column : table.getColumns()) {
                Object value = row.getValue(column.getName());
                System.out.printf("| %-15s ", value == null ? "null" : value);
            }
            System.out.println("|");
        }
    }
    private static void printSelectResult(Object result, Table table) {
        List<Row> rows = (List<Row>) result;

        // Заголовки столбцов
        for (Column column : table.getColumns()) {
            System.out.printf("| %-15s ", column.getName());
        }
        System.out.println("|");

        // Разделитель
        for (Column column : table.getColumns()) {
            System.out.print("-------------------");
        }
        System.out.println();

        // Данные
        for (Row row : rows) {
            for (Column column : table.getColumns()) {
                Object value = row.getValue(column.getName());
                System.out.printf("| %-15s ", value == null ? "null" : value);
            }
            System.out.println("|");
        }

        if (rows.isEmpty()) {
            System.out.println("Записи не найдены.");
        }
    }
}
