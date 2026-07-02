package hostel;

import java.io.Serializable;

public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private String studentId;
    private String name;
    private String gender;
    private String department;
    private int year;
    private String phoneNumber;
    private String roomNumber;

    public Student(String studentId, String name, String gender, String department,
                   int year, String phoneNumber) {
        this.studentId = studentId;
        this.name = name;
        this.gender = gender;
        this.department = department;
        this.year = year;
        this.phoneNumber = phoneNumber;
        this.roomNumber = "";
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getGender() { return gender; }
    public String getDepartment() { return department; }
    public int getYear() { return year; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRoomNumber() { return roomNumber; }

    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setName(String name) { this.name = name; }
    public void setGender(String gender) { this.gender = gender; }
    public void setDepartment(String department) { this.department = department; }
    public void setYear(int year) { this.year = year; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String toString() {
        return studentId + " - " + name;
    }
}
