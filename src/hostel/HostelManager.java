package hostel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class HostelManager {
    private ArrayList<Student> students;
    private ArrayList<Room> rooms;
    private Queue<String> waitingQueue;

    public HostelManager() { loadData(); }

    public ArrayList<Student> getStudents() { return students; }
    public ArrayList<Room> getRooms() { return rooms; }
    public Queue<String> getWaitingQueue() { return waitingQueue; }

    public void loadData() {
        students = FileManager.loadStudents();
        rooms = FileManager.loadRooms();
        waitingQueue = FileManager.loadWaitingQueue();
        repairOldDataIfNeeded();
    }

    public void saveData() {
        FileManager.saveStudents(students);
        FileManager.saveRooms(rooms);
        FileManager.saveWaitingQueue(waitingQueue);
    }

    public String addStudent(Student student) {
        if (findStudent(student.getStudentId()) != null) {
            return "A student with this ID already exists.";
        }
        students.add(student);
        String allocationMessage = allocateStudentAutomatically(student);
        saveData();
        return "Student added successfully.\n" + allocationMessage;
    }

    public String editStudent(String studentId, String name, String gender,
                              String department, int year, String phoneNumber) {
        Student student = findStudent(studentId);
        if (student == null) return "Student record was not found.";

        String oldBed = student.getRoomNumber();
        Room oldRoom = findRoom(oldBed);
        student.setName(name);
        student.setGender(gender);
        student.setDepartment(department);
        student.setYear(year);
        student.setPhoneNumber(phoneNumber);

        String message = "Student record updated successfully.";
        if (oldRoom != null && !canStudentStayInRoom(student, oldRoom)) {
            oldRoom.setCurrentOccupancy(oldRoom.getCurrentOccupancy() - 1);
            student.setRoomNumber("");
            waitingQueue.remove(student.getStudentId());
            message += "\n" + allocateStudentAutomatically(student);
            message += allocateWaitingStudentsToRoom(oldRoom);
        }
        saveData();
        return message;
    }

    public String deleteStudent(String studentId) {
        Student student = findStudent(studentId);
        if (student == null) return "Student record was not found.";
        Room oldRoom = findRoom(student.getRoomNumber());
        if (oldRoom != null) {
            oldRoom.setCurrentOccupancy(oldRoom.getCurrentOccupancy() - 1);
        }
        waitingQueue.remove(student.getStudentId());
        students.remove(student);
        String queueMessage = allocateWaitingStudentsToRoom(oldRoom);
        saveData();
        if (queueMessage.isEmpty()) return "Student deleted successfully.";
        return "Student deleted successfully." + queueMessage;
    }

    public ArrayList<Student> searchStudents(String searchText) {
        ArrayList<Student> results = new ArrayList<Student>();
        String text = searchText.trim().toLowerCase();
        for (Student student : students) {
            String roomNumber = student.getRoomNumber();
            if (roomNumber == null) roomNumber = "";
            if (text.isEmpty()
                    || student.getStudentId().toLowerCase().contains(text)
                    || student.getName().toLowerCase().contains(text)
                    || student.getGender().toLowerCase().contains(text)
                    || roomNumber.toLowerCase().contains(text)) {
                results.add(student);
            }
        }
        return results;
    }

    public String addRoom(Room room) {
        normalizeRoom(room);
        if (findRoom(room.getRoomNumber()) != null) return "This room number already exists.";
        rooms.add(room);
        String queueMessage = allocateWaitingStudentsToRoom(room);
        saveData();
        if (queueMessage.isEmpty()) return "Room added successfully.";
        return "Room added successfully." + queueMessage;
    }

    public String deleteRoom(String roomNumber) {
        Room room = findRoom(roomNumber);
        if (room == null) return "Room was not found.";
        if (room.getCurrentOccupancy() > 0) return "An occupied room cannot be deleted.";
        rooms.remove(room);
        saveData();
        return "Room deleted successfully.";
    }

    public String allocateRoom(Student student, Room room) {
        if (student == null) return "Please select a student.";
        if (room == null) {
            if (hasRoom(student)) return "Please select a room to change this student's bed.";
            return allocateStudentAndSave(student);
        }
        if (!canStudentStayInRoom(student, room)) {
            return student.getGender() + " student cannot be allocated to " + room.getHostelType() + " hostel.";
        }

        String oldBed = student.getRoomNumber();
        Room oldRoom = findRoom(oldBed);
        if (hasRoom(student) && oldRoom != null
                && oldRoom.getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
            String newBed = findNextBedNumber(room, oldBed);
            if (newBed == null) return "No other free bed is available in the selected room.";
            student.setRoomNumber(newBed);
            waitingQueue.remove(student.getStudentId());
            saveData();
            return "Bed changed from " + oldBed + " to " + newBed + ".";
        }

        if (!isRoomAvailable(room)) {
            if (!hasRoom(student)) {
                addToWaitingQueue(student);
                saveData();
                return "Selected room is full. The student was added to the waiting list.";
            }
            return "Selected room is full. Please choose another compatible room.";
        }

        if (hasRoom(student) && oldRoom != null) {
            oldRoom.setCurrentOccupancy(oldRoom.getCurrentOccupancy() - 1);
        }
        waitingQueue.remove(student.getStudentId());
        String bedNumber = allocateStudentToRoom(student, room);
        saveData();
        if (oldBed != null && !oldBed.isEmpty()) {
            return "Student moved from " + oldBed + " to " + bedNumber + ".";
        }
        return "Bed " + bedNumber + " allocated successfully.";
    }

    public String allocateBed(Student student, Room room, String bedLabel) {
        if (student == null) return "Please select a student.";
        if (room == null) return "Please select a room.";
        if (bedLabel == null || bedLabel.trim().isEmpty() || bedLabel.equals("null")) return "Please select a bed.";
        if (!canStudentStayInRoom(student, room)) {
            return student.getGender() + " student cannot be allocated to " + room.getHostelType() + " hostel.";
        }

        String newBed = room.getRoomNumber() + "-" + bedLabel;
        String oldBed = student.getRoomNumber();
        if (newBed.equalsIgnoreCase(oldBed)) {
            return "This student is already allocated to " + newBed + ".";
        }
        if (isBedOccupied(newBed)) {
            return "Selected bed is already occupied. Please choose another bed.";
        }

        Room oldRoom = findRoom(oldBed);
        if (oldRoom != null && !oldRoom.getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
            oldRoom.setCurrentOccupancy(oldRoom.getCurrentOccupancy() - 1);
            room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        } else if (oldRoom == null) {
            room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        }

        waitingQueue.remove(student.getStudentId());
        student.setRoomNumber(newBed);
        saveData();
        if (oldBed != null && !oldBed.isEmpty()) {
            return "Student moved from " + oldBed + " to " + newBed + ".";
        }
        return "Bed " + newBed + " allocated successfully.";
    }
    public String checkoutStudent(Student student) {
        if (student == null) return "Please select a student.";
        if (!hasRoom(student)) return "This student does not have an allocated room.";
        Room room = findRoom(student.getRoomNumber());
        String freedBed = student.getRoomNumber();
        if (room != null) room.setCurrentOccupancy(room.getCurrentOccupancy() - 1);
        students.remove(student);
        waitingQueue.remove(student.getStudentId());

        String queueMessage = allocateWaitingStudentsToRoom(room);
        saveData();
        if (queueMessage.isEmpty()) {
            return "Checkout completed. Bed " + freedBed + " is now available.";
        }
        return "Checkout completed." + queueMessage;
    }

    public Student findStudent(String studentId) {
        for (Student student : students) {
            if (student.getStudentId().equalsIgnoreCase(studentId)) return student;
        }
        return null;
    }

    public Room findRoom(String roomOrBedNumber) {
        if (roomOrBedNumber == null || roomOrBedNumber.isEmpty()) return null;
        String roomNumber = getRoomNumberFromBed(roomOrBedNumber);
        for (Room room : rooms) {
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) return room;
        }
        return null;
    }

    public boolean hasRoom(Student student) {
        return student != null && student.getRoomNumber() != null && !student.getRoomNumber().isEmpty();
    }

    public boolean isRoomAvailable(Room room) {
        return room != null && room.getCurrentOccupancy() < room.getCapacity();
    }

    public boolean isWaiting(String studentId) {
        for (String waitingId : waitingQueue) {
            if (waitingId.equalsIgnoreCase(studentId)) return true;
        }
        return false;
    }

    public int getAvailableRoomCount() {
        int count = 0;
        for (Room room : rooms) {
            if (isRoomAvailable(room)) count++;
        }
        return count;
    }

    public int getAvailableBedCount() {
        int count = 0;
        for (Room room : rooms) {
            count += room.getCapacity() - room.getCurrentOccupancy();
        }
        return count;
    }

    public boolean canStudentStayInRoom(Student student, Room room) {
        if (student == null || room == null) return false;
        String gender = student.getGender();
        String hostelType = room.getHostelType();
        if (gender == null || hostelType == null) return false;
        if (gender.equalsIgnoreCase("Male")) return hostelType.equalsIgnoreCase("Boys");
        if (gender.equalsIgnoreCase("Female")) return hostelType.equalsIgnoreCase("Girls");
        return false;
    }

    public boolean canRoomAcceptStudentForManualAllocation(Student student, Room room) {
        if (!canStudentStayInRoom(student, room)) return false;
        Room currentRoom = findRoom(student.getRoomNumber());
        if (currentRoom != null && currentRoom.getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
            return findNextBedNumber(room, student.getRoomNumber()) != null;
        }
        return isRoomAvailable(room);
    }
    public boolean canBedAcceptStudentForManualAllocation(Student student, Room room, String bedLabel) {
        if (!canStudentStayInRoom(student, room)) return false;
        if (bedLabel == null || bedLabel.trim().isEmpty()) return false;
        String bedNumber = room.getRoomNumber() + "-" + bedLabel;
        return !isBedOccupied(bedNumber);
    }
    public Room findAvailableRoomForStudent(Student student) {
        for (Room room : rooms) {
            if (isRoomAvailable(room) && canStudentStayInRoom(student, room)) {
                return room;
            }
        }
        return null;
    }

    public String getHostelPrefix(String hostelType) {
        if (hostelType != null && hostelType.equalsIgnoreCase("Girls")) return "GH";
        return "BH";
    }

    public String makeRoomNumber(String hostelType, String hostelNumber, String block, String roomNumber) {
        return getHostelPrefix(hostelType) + "-" + hostelNumber + "-" + block.toUpperCase() + "-" + roomNumber;
    }

    private String allocateStudentAndSave(Student student) {
        String message = allocateStudentAutomatically(student);
        saveData();
        return message;
    }

    private String allocateStudentAutomatically(Student student) {
        Room room = findAvailableRoomForStudent(student);
        if (room == null) {
            addToWaitingQueue(student);
            return "No suitable bed is available. Student is added to waiting list.";
        }
        waitingQueue.remove(student.getStudentId());
        String bedNumber = allocateStudentToRoom(student, room);
        return "Bed " + bedNumber + " allocated automatically.";
    }

    private String allocateStudentToRoom(Student student, Room room) {
        String bedNumber = findNextBedNumber(room);
        student.setRoomNumber(bedNumber);
        room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        return bedNumber;
    }

    private String allocateWaitingStudentsToRoom(Room room) {
        if (room == null || !isRoomAvailable(room) || waitingQueue.isEmpty()) return "";
        int originalSize = waitingQueue.size();
        int allocatedCount = 0;
        String names = "";
        for (int i = 0; i < originalSize; i++) {
            String studentId = waitingQueue.poll();
            Student student = findStudent(studentId);
            if (student == null || hasRoom(student)) {
                continue;
            }
            if (isRoomAvailable(room) && canStudentStayInRoom(student, room)) {
                String bedNumber = allocateStudentToRoom(student, room);
                allocatedCount++;
                names += "\n- " + student.getName() + " -> " + bedNumber;
            } else {
                waitingQueue.offer(studentId);
            }
        }
        if (allocatedCount == 0) return "";
        return "\nAllocated " + allocatedCount + " waiting student(s):" + names;
    }

    private void addToWaitingQueue(Student student) {
        if (!hasRoom(student) && !isWaiting(student.getStudentId())) {
            waitingQueue.offer(student.getStudentId());
        }
    }

    private String findNextBedNumber(Room room) {
        String bedNumber = findNextBedNumber(room, "");
        if (bedNumber == null) return room.getRoomNumber() + "-BED-A";
        return bedNumber;
    }

    private String findNextBedNumber(Room room, String bedToSkip) {
        for (int i = 0; i < room.getCapacity(); i++) {
            String bedNumber = room.getRoomNumber() + "-BED-" + (char)('A' + i);
            boolean skipThisBed = bedToSkip != null && bedNumber.equalsIgnoreCase(bedToSkip);
            if (!skipThisBed && !isBedOccupied(bedNumber)) return bedNumber;
        }
        return null;
    }

    private boolean isBedOccupied(String bedNumber) {
        for (Student student : students) {
            if (bedNumber.equalsIgnoreCase(student.getRoomNumber())) return true;
        }
        return false;
    }

    private String getRoomNumberFromBed(String roomOrBedNumber) {
        int bedIndex = roomOrBedNumber.toUpperCase().indexOf("-BED-");
        if (bedIndex >= 0) return roomOrBedNumber.substring(0, bedIndex);
        return roomOrBedNumber;
    }

    private void repairOldDataIfNeeded() {
        boolean oldDataFound = false;
        for (Room room : rooms) {
            if (room.getHostelType() == null || room.getHostelType().trim().isEmpty()) oldDataFound = true;
            normalizeRoom(room);
        }
        for (Student student : students) {
            if (hasRoom(student) && student.getRoomNumber().toUpperCase().indexOf("-BED-") < 0) {
                oldDataFound = true;
            }
        }
        if (oldDataFound) {
            rebuildAllAllocations();
            saveData();
        } else {
            recountRoomOccupancy();
            cleanWaitingQueue();
        }
    }

    private void rebuildAllAllocations() {
        waitingQueue = new LinkedList<String>();
        for (Room room : rooms) room.setCurrentOccupancy(0);
        for (Student student : students) student.setRoomNumber("");
        for (Student student : students) allocateStudentAutomatically(student);
    }

    private void recountRoomOccupancy() {
        for (Room room : rooms) room.setCurrentOccupancy(0);
        for (Student student : students) {
            if (hasRoom(student)) {
                Room room = findRoom(student.getRoomNumber());
                if (room != null && canStudentStayInRoom(student, room) && room.getCurrentOccupancy() < room.getCapacity()) {
                    room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
                } else {
                    student.setRoomNumber("");
                    addToWaitingQueue(student);
                }
            }
        }
    }

    private void cleanWaitingQueue() {
        Queue<String> oldQueue = waitingQueue;
        waitingQueue = new LinkedList<String>();
        for (String studentId : oldQueue) {
            Student student = findStudent(studentId);
            if (student != null && !hasRoom(student) && !isWaiting(studentId)) {
                waitingQueue.offer(studentId);
            }
        }
    }

    private void normalizeRoom(Room room) {
        if (room.getHostelType() == null || room.getHostelType().trim().isEmpty()) {
            String number = room.getRoomNumber() == null ? "" : room.getRoomNumber().toUpperCase();
            if (number.startsWith("GH") || room.getBlock().equalsIgnoreCase("B")) room.setHostelType("Girls");
            else room.setHostelType("Boys");
        }
        if (room.getRoomType() == null || room.getRoomType().trim().isEmpty()) {
            room.setRoomType("Four Sharing");
        }
        if (room.getCapacity() < 1) room.setCapacity(4);
        if (room.getCapacity() > 4) room.setCapacity(4);
        if (room.getCurrentOccupancy() < 0) room.setCurrentOccupancy(0);
    }
}
