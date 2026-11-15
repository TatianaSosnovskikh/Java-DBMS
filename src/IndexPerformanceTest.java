import database.Column;
import database.DataType;
import database.Database;
import database.Row;
import database.Table;

import index.IndexManager;

import java.util.ArrayList;
import java.util.List;

public class IndexPerformanceTest {

    private static void addRow(Table table, int id, String brand, double price) {
        Row row = new Row();
        row.setValue("id", id);
        row.setValue("brand", brand);
        row.setValue("price", price);
        table.addRow(row);
    }

    public static void main(String[] args) {
        int count = 50000;

        Database database = new Database();
        Table goods = new Table("performance_goods");

        goods.addColumn(new Column("id", DataType.INTEGER, true));
        goods.addColumn(new Column("brand", DataType.STRING, false));
        goods.addColumn(new Column("price", DataType.DOUBLE, false));

        for (int i = 1; i <= count; i++) {
            addRow(goods, i, "Brand" + (i % 100), i * 1.0);
        }

        database.createTable(goods);

        IndexManager manager = new IndexManager();
        manager.createIndex("idx_performance_brand", goods, "brand");

        long start = System.nanoTime();
        List<Row> withoutIndex = new ArrayList<>();

        for (Row row : goods.getRows()) {
            if ("Brand42".equals(row.getValue("brand"))) {
                withoutIndex.add(row);
            }
        }

        long withoutIndexTime = System.nanoTime() - start;

        start = System.nanoTime();
        List<Row> withIndex =
                manager.findRows(database, "performance_goods", "brand", "=", "'Brand42'");

        long withIndexTime = System.nanoTime() - start;

        System.out.println("Количество записей: " + count);
        System.out.println("Без индекса найдено: " + withoutIndex.size());
        System.out.println("С индексом найдено: " + withIndex.size());
        System.out.printf("Без индекса: %.3f мс%n", withoutIndexTime / 1_000_000.0);
        System.out.printf("С индексом: %.3f мс%n", withIndexTime / 1_000_000.0);
        System.out.println(
                withoutIndex.size() == withIndex.size()
                        ? "Результаты совпадают."
                        : "ОШИБКА: результаты различаются!");
    }
}
