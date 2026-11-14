import database.*;

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
    }
}
