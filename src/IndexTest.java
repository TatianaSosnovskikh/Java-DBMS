
import database.Column;
import database.DataType;
import database.Database;
import database.Row;
import database.Table;
import index.IndexManager;
import java.io.File;
import java.util.List;

public class IndexTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        IndexManager manager = new IndexManager();
        Database database = new Database();

        Table goods = new Table("goods");
        goods.addColumn(new Column("id", DataType.INTEGER, true));
        goods.addColumn(new Column("brand", DataType.STRING, false));
        goods.addColumn(new Column("price", DataType.DOUBLE, false));

        addRow(goods, 1, "Toyota", 25000.0);
        addRow(goods, 2, "BMW", 30000.0);
        addRow(goods, 3, "Toyota", 18000.0);
        addRow(goods, 4, "Audi", 35000.0);

        database.createTable(goods);

        manager.createIndex("idx_goods_brand", goods, "brand");
        manager.createIndex("idx_goods_price", goods, "price");

        check(manager.getIndexes().size() == 2,
                "Создание двух индексов");

        List<Row> toyotas = manager.findRows(
                database, "goods", "brand", "=", "'Toyota'");
        check(toyotas != null && toyotas.size() == 2,
                "Поиск по строковому значению");

        List<Row> expensive = manager.findRows(
                database, "goods", "price", ">", "25000");
        check(expensive != null && expensive.size() == 2,
                "Поиск с оператором >");

        List<Row> cheap = manager.findRows(
                database, "goods", "price", "<", "25000");
        check(cheap != null && cheap.size() == 1,
                "Поиск с оператором <");

        addRow(goods, 5, "Honda", 22000.0);
        manager.refreshTableIndexes(goods);

        List<Row> hondas = manager.findRows(
                database, "goods", "brand", "=", "'Honda'");
        check(hondas != null && hondas.size() == 1,
                "Обновление индекса после добавления записи");

        File indexFile = new File("data/indexes.dat");
        check(indexFile.exists() && indexFile.length() > 0,
                "Сохранение индексов в файл");

        IndexManager loadedManager = new IndexManager();
        loadedManager.loadIndexes();
        loadedManager.rebuildAllIndexes(database);

        List<Row> loadedToyotas = loadedManager.findRows(
                database, "goods", "brand", "=", "'Toyota'");
        check(loadedManager.getIndexes().size() == 2
                        && loadedToyotas != null
                        && loadedToyotas.size() == 2,
                "Загрузка и восстановление индексов");

        try {
            manager.createIndex("idx_goods_unknown", goods, "unknown");
            check(false, "Проверка отсутствующего столбца");
        } catch (IllegalArgumentException e) {
            check(true, "Проверка отсутствующего столбца");
        }

        System.out.println();
        System.out.println("ИТОГ: " + passed
                + " успешно, " + failed + " с ошибками");
    }

    private static void addRow(Table table, int id,
                               String brand, double price) {
        Row row = new Row();
        row.setValue("id", id);
        row.setValue("brand", brand);
        row.setValue("price", price);
        table.addRow(row);
    }

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
