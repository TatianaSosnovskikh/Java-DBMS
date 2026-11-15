
package storage;

import database.Database;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import database.Database;

/**
 * Класс для сохранения и загрузки базы данных.
 */
public class DatabaseStorage {

    private static final String FILE_NAME = "data/database.dat";
    private static final String TEMP_FILE_NAME = "data/database.tmp";

    /**
     * Сохраняет базу данных во временный файл,
     * а затем заменяет основной файл.
     *
     * @param database база данных для сохранения
     * @throws IOException если произошла ошибка сохранения
     */
    public void save(Database database) throws IOException {
        File file = new File(FILE_NAME);
        File directory = file.getParentFile();

        if (directory != null && !directory.exists()
                && !directory.mkdirs()) {
            throw new IOException("Не удалось создать папку для базы данных");
        }

        File tempFile = new File(TEMP_FILE_NAME);

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(tempFile))) {
            out.writeObject(database);
            out.flush();
        }

        try {
            Files.move(
                    tempFile.toPath(),
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException e) {
            // Если атомарная замена не поддерживается,
            // пробуем обычную замену.
            try {
                Files.move(
                        tempFile.toPath(),
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException moveError) {
                moveError.addSuppressed(e);
                throw moveError;
            }
        }

        System.out.println("База данных сохранена");
    }

    /**
     * Загружает базу данных из файла.
     *
     * @return загруженная база данных
     * @throws IOException если файл отсутствует или повреждён
     * @throws ClassNotFoundException если класс сохранённого объекта не найден
     */
    public Database load() throws IOException, ClassNotFoundException {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            System.out.println("Файл базы данных не найден. Создаётся новая база");
            return new Database();
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(file))) {

            Object object = in.readObject();

            if (!(object instanceof Database)) {
                throw new IOException("Файл содержит объект другого типа");
            }

            return (Database) object;
        }
    }
}
