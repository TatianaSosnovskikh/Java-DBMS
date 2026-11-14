package storage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import database.Database;

/*
Сохранение и загрузка базы данных.
 */
public class DatabaseStorage {
    private static final String FILE_NAME = "data/database.dat";

    public void save(Database database) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(database);
            System.out.println("База данных сохоранена");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Database load() {
        return new Database();
    }
}
