import database.*;

import executor.CrudHandler;
import executor.QueryExecutor;

import storage.DatabaseStorage;

import java.util.List;

/**
 * Главный класс приложения.
 *
 * <p>Отвечает за запуск программы, загрузку базы данных, создание необходимых таблиц, демонстрацию
 * SQL-операций и сохранение изменений на диск.
 *
 * <p>В программе используются две связанные таблицы: categories — категории товаров; goods —
 * товары, связанные с категориями внешним ключом.
 */
public class Main {

    /**
     * Точка входа в приложение.
     *
     * <p>При запуске выполняются следующие действия: 1. Загружается база данных из файла. 2.
     * Создаются таблицы, если они ещё не существуют. 3. Настраивается связь между таблицами. 4.
     * Выполняются демонстрационные CRUD-операции. 5. Проверяется работа внешнего ключа. 6. База
     * данных сохраняется на диск.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) throws Exception {

        // Инициализация хранилища и загрузка базы данных.
        // Если файл базы данных отсутствует, создаётся новая база.
        DatabaseStorage storage = new DatabaseStorage();
        Database database = storage.load();

        // Создание таблицы категорий.
        // Таблица содержит уникальный идентификатор и название категории.
        if (database.getTable("categories") == null) {
            Table categories = new Table("categories");

            categories.addColumn(new Column("id", DataType.INTEGER, true));
            categories.addColumn(new Column("name", DataType.STRING, false));

            database.createTable(categories);
        }

        // Создание таблицы товаров.
        // Каждый товар имеет идентификатор, категорию, название и цену.
        if (database.getTable("goods") == null) {
            Table goods = new Table("goods");

            goods.addColumn(new Column("id", DataType.INTEGER, true));
            goods.addColumn(new Column("categoryId", DataType.INTEGER, false));
            goods.addColumn(new Column("name", DataType.STRING, false));
            goods.addColumn(new Column("price", DataType.DOUBLE, false));

            database.createTable(goods);
        }

        // Получение ссылок на созданные или ранее загруженные таблицы.
        Table categories = database.getTable("categories");
        Table goods = database.getTable("goods");

        // Настройка внешнего ключа.
        // Поле categoryId таблицы goods ссылается на поле id таблицы categories.
        // Проверка выполняется также для таблицы, загруженной с диска.
        if (goods.getForeignKeys().isEmpty()) {
            goods.addForeignKey(new ForeignKey("categoryId", "categories", "id"));
        }

        // Создание обработчиков SQL-запросов.
        // CrudHandler выполняет операции над записями,
        // QueryExecutor разбирает и выполняет SQL-подобные запросы.
        CrudHandler crudHandler = new CrudHandler();
        QueryExecutor queryExecutor = new QueryExecutor(database, crudHandler);

        // Добавление начальной категории, если таблица categories пуста.
        if (categories.getRows().isEmpty()) {
            System.out.println("\n=== INSERT: добавление категории ===");

            System.out.println(
                    queryExecutor.execute("INSERT INTO categories (id, name) VALUES (1, 'Food')"));
        }

        // Демонстрация CRUD-операций выполняется только тогда,
        // когда таблица товаров не содержит записей.
        // Это позволяет не изменять пользовательские данные при повторном запуске.
        if (goods.getRows().isEmpty()) {

            // INSERT: добавление нового товара.
            System.out.println("\n=== INSERT: добавление товара ===");

            System.out.println(
                    queryExecutor.execute(
                            "INSERT INTO goods (id, categoryId, name, price) "
                                    + "VALUES (1, 1, 'Apple', 100.0)"));

            // SELECT: получение всех указанных столбцов товара.
            System.out.println("\n=== SELECT: просмотр товара ===");

            printSelectResult(
                    queryExecutor.execute(
                            "SELECT id, categoryId, name, price " + "FROM goods WHERE id = 1"),
                    "id",
                    "categoryId",
                    "name",
                    "price");

            // SELECT: получение только названия и цены товара.
            System.out.println("\n=== SELECT: только название и цена ===");

            printSelectResult(
                    queryExecutor.execute("SELECT name, price FROM goods WHERE id = 1"),
                    "name",
                    "price");

            // UPDATE: изменение цены товара по его идентификатору.
            System.out.println("\n=== UPDATE: изменение цены ===");

            System.out.println(
                    queryExecutor.execute("UPDATE goods SET price = 120.0 WHERE id = 1"));

            // Повторный SELECT для проверки результата обновления.
            System.out.println("\n=== SELECT: проверка изменения ===");

            printSelectResult(
                    queryExecutor.execute(
                            "SELECT id, categoryId, name, price " + "FROM goods WHERE id = 1"),
                    "id",
                    "categoryId",
                    "name",
                    "price");

            // DELETE: удаление товара по его идентификатору.
            System.out.println("\n=== DELETE: удаление товара ===");

            System.out.println(queryExecutor.execute("DELETE FROM goods WHERE id = 1"));

            // SELECT: проверка того, что товар был удалён.
            System.out.println("\n=== SELECT: проверка удаления ===");

            printSelectResult(
                    queryExecutor.execute(
                            "SELECT id, categoryId, name, price " + "FROM goods WHERE id = 1"),
                    "id",
                    "categoryId",
                    "name",
                    "price");

        } else {

            // Если данные уже существуют, выводим их без изменения.
            System.out.println("\nВ таблице goods уже есть данные.");
            System.out.println("Показываем существующие записи.");

            printTable(goods);
        }

        // Удаление старой тестовой записи, которая была добавлена
        // до исправления проверки внешнего ключа.
        for (Row row : goods.getRows()) {
            Object id = row.getValue("id");

            if (id != null && id.toString().equals("99")) {
                queryExecutor.execute("DELETE FROM goods WHERE id = 99");
                break;
            }
        }

        // Проверка ограничения внешнего ключа.
        // Нельзя добавить товар, если указанной категории не существует.
        System.out.println("\n=== ПРОВЕРКА ВНЕШНИХ КЛЮЧЕЙ ===");

        System.out.println("\n1. Попытка добавить товар с несуществующей категорией:");

        try {
            System.out.println(
                    queryExecutor.execute(
                            "INSERT INTO goods (id, categoryId, name, price) "
                                    + "VALUES (999, 999, 'Invalid', 10.0)"));

            System.out.println("ОШИБКА: товар с несуществующей категорией добавлен.");

        } catch (Exception e) {
            System.out.println("Ожидаемый отказ: " + e.getMessage());
        }

        // Проверка ограничения при удалении категории.
        // Категорию нельзя удалить, если на неё ссылается существующий товар.
        System.out.println("\n2. Проверка удаления используемой категории:");

        boolean hasCategoryReference = false;

        for (Row row : goods.getRows()) {
            Object categoryId = row.getValue("categoryId");

            if (categoryId != null && categoryId.toString().equals("1")) {
                hasCategoryReference = true;
                break;
            }
        }

        if (hasCategoryReference) {
            try {
                System.out.println(queryExecutor.execute("DELETE FROM categories WHERE id = 1"));

                System.out.println("ОШИБКА: используемая категория удалена.");

            } catch (Exception e) {
                System.out.println("Ожидаемый отказ: " + e.getMessage());
            }

        } else {
            System.out.println(
                    "Нет товаров, связанных с категорией 1. " + "Проверка удаления пропущена.");
        }

        // Вывод списка таблиц, содержащихся в базе данных.
        System.out.println("\n=== ТАБЛИЦЫ БАЗЫ ДАННЫХ ===");
        System.out.println(database.getTables().keySet());

        // Вывод итогового содержимого таблицы товаров.
        System.out.println("\n=== ТЕКУЩЕЕ СОДЕРЖИМОЕ GOODS ===");
        printTable(goods);

        // Сохранение всех изменений базы данных на диск.
        storage.save(database);

        System.out.println("\nБаза данных сохранена.");
    }

    /**
     * Выводит содержимое таблицы в консоль.
     *
     * <p>Сначала отображаются названия столбцов, затем разделитель и все строки таблицы.
     *
     * @param table таблица, содержимое которой необходимо вывести
     */
    private static void printTable(Table table) {

        // Вывод заголовков столбцов.
        for (Column column : table.getColumns()) {
            System.out.printf("| %-15s ", column.getName());
        }
        System.out.println("|");

        // Вывод разделительной строки.
        for (Column column : table.getColumns()) {
            System.out.print("-------------------");
        }
        System.out.println();

        // Вывод значений всех записей таблицы.
        for (Row row : table.getRows()) {
            for (Column column : table.getColumns()) {
                Object value = row.getValue(column.getName());

                System.out.printf("| %-15s ", value == null ? "null" : value);
            }
            System.out.println("|");
        }

        // Сообщение для пустой таблицы.
        if (table.getRows().isEmpty()) {
            System.out.println("Таблица пустая.");
        }
    }

    /**
     * Выводит результат SQL-запроса SELECT в табличном виде.
     *
     * <p>Метод получает список строк результата запроса и отображает только те столбцы, которые
     * были указаны при выполнении SELECT.
     *
     * @param result результат выполнения SELECT-запроса
     * @param columnNames названия столбцов для отображения
     */
    private static void printSelectResult(Object result, String... columnNames) {

        // Преобразование результата запроса в список строк.
        List<Row> rows = (List<Row>) result;

        // Вывод заголовков выбранных столбцов.
        for (String columnName : columnNames) {
            System.out.printf("| %-15s ", columnName);
        }
        System.out.println("|");

        // Вывод разделительной строки.
        for (String columnName : columnNames) {
            System.out.print("-------------------");
        }
        System.out.println();

        // Вывод значений выбранных столбцов для каждой строки.
        for (Row row : rows) {
            for (String columnName : columnNames) {
                Object value = row.getValue(columnName);

                System.out.printf("| %-15s ", value == null ? "null" : value);
            }
            System.out.println("|");
        }

        // Если запрос не вернул записей, выводим соответствующее сообщение.
        if (rows.isEmpty()) {
            System.out.println("Записи не найдены.");
        }
    }
}
