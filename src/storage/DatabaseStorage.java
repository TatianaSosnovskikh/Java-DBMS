package storage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.File;

import database.Database;

/*
Сохранение и загрузка базы данных.
 */
public class DatabaseStorage {
    private static final String FILE_NAME = "data/database.dat";

    public void save(Database database) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(database);
            System.out.println("База данных сохранена");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Database load() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            System.out.println("Файл базы данных был не найден. Создаётся новая база");
            return new Database();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (Database) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return new Database();
        }
    }
}
