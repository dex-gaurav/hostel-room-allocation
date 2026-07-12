package hostel;

import java.io.Serializable;

/**
 * The Student class represents a student residing in or applying to the hostel.
 * It implements Serializable to allow student records to be stored and retrieved
 * from binary data files locally.
 */
public class Student implements Serializable {
    // Unique identifier for serialization integrity.
    private static final long serialVersionUID = 1L;

    // Student fields storing personal and academic details
    private String studentId;
    private String name;
    private String gender;
    private String department;
    private int year;
    private String phoneNumber;
    // Tracks the student's allocated room and bed (empty string means not allocated/waiting list)
    private String roomNumber;

    /**
     * Constructor to initialize a new student record.
     * By default, a new student does not have an allocated room (roomNumber is empty).
     *
     * @param studentId   Unique identification number of the student
     * @param name        Full name of the student
     * @param gender      Gender (e.g., Male or Female) used for room compatibility checks
     * @param department  Academic department / major
     * @param year        Current year of study (1 to 4)
     * @param phoneNumber 10-digit contact number
     */
    public Student(String studentId, String name, String gender, String department,
                   int year, String phoneNumber) {
        this.studentId = studentId;
        this.name = name;
        this.gender = gender;
        this.department = department;
        this.year = year;
        this.phoneNumber = phoneNumber;
        this.roomNumber = ""; // Initially unallocated
    }

    // ==========================================
    // Getters
    // Provide read access to encapsulated fields
    // ==========================================

    /**
     * @return The student's unique ID
     */
    public String getStudentId() { return studentId; }

    /**
     * @return The student's full name
     */
    public String getName() { return name; }

    /**
     * @return The student's gender (Male/Female)
     */
    public String getGender() { return gender; }

    /**
     * @return The student's academic department
     */
    public String getDepartment() { return department; }

    /**
     * @return The student's current year of study
     */
    public int getYear() { return year; }

    /**
     * @return The student's phone number
     */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * @return The student's allocated room number and bed label (e.g., "BH-01-A-101-BED-A")
     */
    public String getRoomNumber() { return roomNumber; }

    // ==========================================
    // Setters
    // Provide write access to encapsulated fields
    // ==========================================

    /**
     * Updates the student's ID
     * @param studentId New ID
     */
    public void setStudentId(String studentId) { this.studentId = studentId; }

    /**
     * Updates the student's name
     * @param name New name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Updates the student's gender
     * @param gender New gender
     */
    public void setGender(String gender) { this.gender = gender; }

    /**
     * Updates the student's department
     * @param department New department
     */
    public void setDepartment(String department) { this.department = department; }

    /**
     * Updates the student's academic year
     * @param year New academic year
     */
    public void setYear(int year) { this.year = year; }

    /**
     * Updates the student's contact number
     * @param phoneNumber New phone number
     */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /**
     * Sets or changes the student's allocated room/bed code.
     * Pass an empty string if checking out or unallocating.
     * @param roomNumber Allocated room/bed string
     */
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    /**
     * Generates a string representation of the student, primarily used
     * to display student selections in dropdowns or list components.
     *
     * @return String displaying student ID and name
     */
    public String toString() {
        return studentId + " - " + name;
    }
}

