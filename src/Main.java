import database.*;
import storage.DatabaseStorage;
import executor.CrudHandler;
import executor.QueryExecutor;

public class Main {
    public static void main(String[] args) {
        Database database = new Database();
        Table categories = new Table("categories");

        categories.addColumn(new Column("id", DataType.INTEGER, true));
        categories.addColumn(new Column("name", DataType.STRING, false));

        database.createTable(categories);

        Table goods = new Table("goods");
        goods.addColumn(new Column("id", DataType.INTEGER, true));
        goods.addColumn(new Column("categoryId", DataType.INTEGER, false));
        goods.addColumn(new Column("name", DataType.STRING, false));
        goods.addColumn(new Column("price", DataType.DOUBLE, false));

        database.createTable(goods);

        System.out.println("База данных создана");

        DatabaseStorage storage = new DatabaseStorage();
        storage.save(database);
        Database loadedDatabase = storage.load();

        System.out.println("Таблицы:");
        System.out.println(loadedDatabase.getTables().keySet());

// Подключение обработчика CRUD-операций
        CrudHandler crudHandler = new CrudHandler();
        QueryExecutor queryExecutor =
                new QueryExecutor(loadedDatabase, crudHandler);

// Добавление категории
        queryExecutor.execute(
                "INSERT INTO categories (id, name) VALUES (1, 'Food')"
        );

// Добавление товара
        queryExecutor.execute(
                "INSERT INTO goods (id, categoryId, name, price) VALUES (1, 1, 'Apple', 100.0)"
        );

// Вывод товаров
        System.out.println("Товары:");
        printTable(loadedDatabase.getTable("goods"));

// Обновление товара
        System.out.println(
                queryExecutor.execute(
                        "UPDATE goods SET price = 120.0 WHERE id = 1"
                )
        );

// Удаление товара
        System.out.println(
                queryExecutor.execute(
                        "DELETE FROM goods WHERE id = 1"
                )
        );

// Сохраняем изменения
        storage.save(loadedDatabase);
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
}
