import database.*;
import storage.DatabaseStorage;

/**
 * Главный класс приложения.
 *
 * <p>
 * Создание тестовой базы данных, проверка сохранения и загрузки данных.
 */
public class Main {
    /**
     * Точка входа в программу.
     *
     * @param args аргументы командной строки
     */
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
    }
}
