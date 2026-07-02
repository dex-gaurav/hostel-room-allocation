package hostel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class FileManager {
    private static final String DATA_FOLDER = "data";
    private static final String STUDENTS_FILE = DATA_FOLDER + File.separator + "students.dat";
    private static final String ROOMS_FILE = DATA_FOLDER + File.separator + "rooms.dat";
    private static final String WAITING_FILE = DATA_FOLDER + File.separator + "waitinglist.dat";

    private FileManager() { }

    public static void saveStudents(ArrayList<Student> students) {
        saveObject(STUDENTS_FILE, students);
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Student> loadStudents() {
        Object data = loadObject(STUDENTS_FILE);
        if (data instanceof ArrayList<?>) {
            return (ArrayList<Student>) data;
        }
        return new ArrayList<Student>();
    }

    public static void saveRooms(ArrayList<Room> rooms) {
        saveObject(ROOMS_FILE, rooms);
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Room> loadRooms() {
        Object data = loadObject(ROOMS_FILE);
        if (data instanceof ArrayList<?>) {
            return (ArrayList<Room>) data;
        }
        return new ArrayList<Room>();
    }

    public static void saveWaitingQueue(Queue<String> waitingQueue) {
        saveObject(WAITING_FILE, new LinkedList<String>(waitingQueue));
    }

    @SuppressWarnings("unchecked")
    public static Queue<String> loadWaitingQueue() {
        Object data = loadObject(WAITING_FILE);
        if (data instanceof Queue<?>) {
            return (Queue<String>) data;
        }
        return new LinkedList<String>();
    }

    private static void saveObject(String fileName, Object data) {
        createDataFolder();
        try {
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(fileName));
            output.writeObject(data);
            output.close();
        } catch (Exception exception) {
            System.err.println("Could not save " + fileName + ": " + exception.getMessage());
        }
    }

    private static Object loadObject(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return null;
        }
        try {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
            Object data = input.readObject();
            input.close();
            return data;
        } catch (Exception exception) {
            System.err.println("Could not load " + fileName + ": " + exception.getMessage());
            return null;
        }
    }

    private static void createDataFolder() {
        File folder = new File(DATA_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}
