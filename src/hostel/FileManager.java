package hostel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The FileManager class is a helper utility responsible for handling persistence.
 * It serializes and deserializes application objects (students, rooms, waiting list queue)
 * to and from local binary files within the "data" directory.
 */
public class FileManager {
    // Directory and file path definitions for data persistence.
    private static final String DATA_FOLDER = "data";
    private static final String STUDENTS_FILE = DATA_FOLDER + File.separator + "students.dat";
    private static final String ROOMS_FILE = DATA_FOLDER + File.separator + "rooms.dat";
    private static final String WAITING_FILE = DATA_FOLDER + File.separator + "waitinglist.dat";

    /**
     * Private constructor to prevent instantiation, as FileManager is a utility class
     * with static methods only.
     */
    private FileManager() { }

    /**
     * Serializes the list of students and saves it to the students data file.
     *
     * @param students List of Student objects to save
     */
    public static void saveStudents(ArrayList<Student> students) {
        saveObject(STUDENTS_FILE, students);
    }

    /**
     * Deserializes and loads the list of students from the students data file.
     * If the file does not exist or loading fails, returns an empty ArrayList.
     *
     * @return ArrayList of Student objects
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Student> loadStudents() {
        Object data = loadObject(STUDENTS_FILE);
        if (data instanceof ArrayList<?>) {
            return (ArrayList<Student>) data;
        }
        return new ArrayList<Student>();
    }

    /**
     * Serializes the list of rooms and saves it to the rooms data file.
     *
     * @param rooms List of Room objects to save
     */
    public static void saveRooms(ArrayList<Room> rooms) {
        saveObject(ROOMS_FILE, rooms);
    }

    /**
     * Deserializes and loads the list of rooms from the rooms data file.
     * If the file does not exist or loading fails, returns an empty ArrayList.
     *
     * @return ArrayList of Room objects
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Room> loadRooms() {
        Object data = loadObject(ROOMS_FILE);
        if (data instanceof ArrayList<?>) {
            return (ArrayList<Room>) data;
        }
        return new ArrayList<Room>();
    }

    /**
     * Serializes the waiting queue and saves it to the waiting list data file.
     * Converts the Queue to a LinkedList to ensure it can be serialized.
     *
     * @param waitingQueue Queue of student IDs on the waiting list
     */
    public static void saveWaitingQueue(Queue<String> waitingQueue) {
        saveObject(WAITING_FILE, new LinkedList<String>(waitingQueue));
    }

    /**
     * Deserializes and loads the waiting list queue of student IDs.
     * If the file does not exist or loading fails, returns an empty Queue.
     *
     * @return Queue of String representing student IDs
     */
    @SuppressWarnings("unchecked")
    public static Queue<String> loadWaitingQueue() {
        Object data = loadObject(WAITING_FILE);
        if (data instanceof Queue<?>) {
            return (Queue<String>) data;
        }
        return new LinkedList<String>();
    }

    /**
     * Helper method to write any serializable Object to a local binary file.
     * It handles directory checks and handles file stream I/O exceptions.
     *
     * @param fileName The file path where the object should be stored
     * @param data     The serializable object to save
     */
    private static void saveObject(String fileName, Object data) {
        createDataFolder(); // Ensure the destination directory exists
        try {
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(fileName));
            output.writeObject(data);
            output.close();
        } catch (Exception exception) {
            System.err.println("Could not save " + fileName + ": " + exception.getMessage());
        }
    }

    /**
     * Helper method to read a serialized object from a local file.
     * Checks if the file exists before attempting to read to avoid exceptions.
     *
     * @param fileName The file path to load the object from
     * @return The deserialized Object, or null if loading fails or file doesn't exist
     */
    private static Object loadObject(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return null; // Return null if there's no data saved yet
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

    /**
     * Checks if the "data" directory exists, and creates it if it doesn't.
     * Prevents file saving failures caused by missing directories.
     */
    private static void createDataFolder() {
        File folder = new File(DATA_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs(); // Create parent directories if missing
        }
    }
}

