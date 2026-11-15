package storage;

import database.Database;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Выполняет сохранение и загрузку базы данных.
 *
 * <p>
 * Использует механизм сериализации Java для записи базы данных в файл и
 * восстановления при
 * запуске программы.
 */
public class DatabaseStorage {
    private static final String FILE_NAME = "data/database.dat";

    /**
     * Сохраняет базу данных в файл.
     *
     * @param database сохраняемая база данных
     */
    public void save(Database database) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(database);
            System.out.println("База данных сохранена");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Загружает базу данных из файла.
     *
     * <p>
     * Если файл отсутствует, то создаётся новая пустая база данных.
     *
     * @return объект базы данных
     */
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
