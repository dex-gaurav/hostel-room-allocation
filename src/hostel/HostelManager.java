package hostel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

// Encapsulates hostel business logic and coordinates persistence.
// Demonstrates MVC controller design, validation rules, and collection management.
public class HostelManager {
    private ArrayList<Student> students;
    private ArrayList<Room> rooms;
    private Queue<String> waitingQueue;

    // Coordinates FileManager persistence.
    // Loads saved data from disk upon instantiation.
    public HostelManager() { loadData(); }

    // ==========================================
    // Core State Accessors
    // ==========================================

    // Encapsulates access to student records.
    // ArrayList stores dynamic student records.
    public ArrayList<Student> getStudents() { return students; }

    // Encapsulates access to room records.
    // ArrayList stores dynamic room configurations.
    public ArrayList<Room> getRooms() { return rooms; }

    // Encapsulates access to the waiting list.
    // Queue maintains FIFO waiting order.
    public Queue<String> getWaitingQueue() { return waitingQueue; }

    // ==========================================
    // Persistence Layer Integration
    // ==========================================

    // Responsibility: Data Recovery | Consistency
    // Restores saved hostel data and validates integrity.
    public void loadData() {
        students = FileManager.loadStudents();
        rooms = FileManager.loadRooms();
        waitingQueue = FileManager.loadWaitingQueue();
        
        if (students == null) students = new ArrayList<Student>();
        if (rooms == null) rooms = new ArrayList<Room>();
        if (waitingQueue == null) waitingQueue = new LinkedList<String>();
        
        repairOldDataIfNeeded();
    }

    // Responsibility: Serialization | File Handling
    // Persists students, rooms and waiting queue using serialization.
    public void saveData() {
        FileManager.saveStudents(students);
        FileManager.saveRooms(rooms);
        FileManager.saveWaitingQueue(waitingQueue);
    }

    // ==========================================
    // Student Management Operations
    // ==========================================

    // Responsibility: Validation | ArrayList | Auto Allocation
    // Business Rule: Student ID must be unique.
    // Registers a new student and triggers auto-allocation or waiting queue.
    public String addStudent(Student student) {
        if (findStudent(student.getStudentId()) != null) {
            return "A student with this ID already exists.";
        }
        students.add(student);
        String allocationMessage = allocateStudentAutomatically(student);
        saveData();
        return "Student added successfully.\n" + allocationMessage;
    }

    // Responsibility: Room Allocation | Validation
    // Re-allocates student if gender details change.
    // Demonstrates reference sharing and status validation.
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
        // Check if the gender modification invalidates the current room assignment
        if (oldRoom != null && !canStudentStayInRoom(student, oldRoom)) {
            releaseCurrentRoom(student);
            student.setRoomNumber("");
            waitingQueue.remove(student.getStudentId());
            
            message += "\n" + allocateStudentAutomatically(student);
            message += allocateWaitingStudentsToRoom(oldRoom);
        }
        saveData();
        return message;
    }

    // Responsibility: Room Allocation | FIFO | Waiting List
    // Deletes student, frees bed slot, and allocates freed bed to waiting queue.
    public String deleteStudent(String studentId) {
        Student student = findStudent(studentId);
        if (student == null) return "Student record was not found.";
        
        Room oldRoom = releaseCurrentRoom(student);
        waitingQueue.remove(student.getStudentId());
        students.remove(student);
        
        String queueMessage = allocateWaitingStudentsToRoom(oldRoom);
        saveData();
        if (queueMessage.isEmpty()) return "Student deleted successfully.";
        return "Student deleted successfully." + queueMessage;
    }

    // Responsibility: Linear Search | ArrayList
    // Time: O(n) - Linear search through students matching filter fields.
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

    // ==========================================
    // Room Management Operations
    // ==========================================

    // Responsibility: Validation | Consistency
    // Business Rule: Room number must be unique.
    // Adds a room and auto-allocates to waiting students.
    public String addRoom(Room room) {
        normalizeRoom(room);
        if (findRoom(room.getRoomNumber()) != null) return "This room number already exists.";
        rooms.add(room);
        
        String queueMessage = allocateWaitingStudentsToRoom(room);
        saveData();
        if (queueMessage.isEmpty()) return "Room added successfully.";
        return "Room added successfully." + queueMessage;
    }

    // Responsibility: Validation | Business Rules
    // Business Rule: Occupied rooms can never be deleted.
    // Removes room configuration only if occupancy is zero.
    public String deleteRoom(String roomNumber) {
        Room room = findRoom(roomNumber);
        if (room == null) return "Room was not found.";
        if (room.getCurrentOccupancy() > 0) return "An occupied room cannot be deleted.";
        rooms.remove(room);
        saveData();
        return "Room deleted successfully.";
    }

    // ==========================================
    // Manual Allocation Operations
    // ==========================================

    // Responsibility: Room Allocation | Relocation
    // Business Rule: Male students -> Boys hostel; Female students -> Girls hostel.
    // Performs auto or manual allocation, handling room-change and waiting list fallbacks.
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

        if (hasRoom(student)) {
            releaseCurrentRoom(student);
        }
        String bedNumber = allocateStudentToRoom(student, room);
        return finalizeAllocation(student, bedNumber, oldBed);
    }

    // Responsibility: Occupancy | Relocation
    // Business Rule: Room occupancy can never exceed capacity.
    // Assigns student to a specific bed, updating room occupancy counts.
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
        if (oldRoom == null) {
            room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        } else if (!oldRoom.getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
            releaseCurrentRoom(student);
            room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        }

        return finalizeAllocation(student, newBed, oldBed);
    }

    // Responsibility: Room Allocation | FIFO | Waiting List
    // Business Rule: Freed beds are immediately offered to waiting students.
    // Releases student's bed and triggers FIFO matching for waiting list.
    public String checkoutStudent(Student student) {
        if (student == null) return "Please select a student.";
        if (!hasRoom(student)) return "This student does not have an allocated room.";
        Room room = releaseCurrentRoom(student);
        String freedBed = student.getRoomNumber();
        
        students.remove(student);
        waitingQueue.remove(student.getStudentId());

        String queueMessage = allocateWaitingStudentsToRoom(room);
        saveData();
        if (queueMessage.isEmpty()) {
            return "Checkout completed. Bed " + freedBed + " is now available.";
        }
        return "Checkout completed." + queueMessage;
    }

    // ==========================================
    // Querying and Helper Utilities
    // ==========================================

    // Responsibility: Linear Search | OOP
    // Time: O(n) - Scans student records for matching student ID.
    public Student findStudent(String studentId) {
        for (Student student : students) {
            if (student.getStudentId().equalsIgnoreCase(studentId)) return student;
        }
        return null;
    }

    // Responsibility: Linear Search | OOP
    // Time: O(rooms) - Locates room by parsing bed code prefixes.
    public Room findRoom(String roomOrBedNumber) {
        if (roomOrBedNumber == null || roomOrBedNumber.isEmpty()) return null;
        String roomNumber = getRoomNumberFromBed(roomOrBedNumber);
        for (Room room : rooms) {
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) return room;
        }
        return null;
    }

    // Checks if student object has assigned bed code.
    public boolean hasRoom(Student student) {
        return student != null && student.getRoomNumber() != null && !student.getRoomNumber().isEmpty();
    }

    // Business Rule: Room occupancy can never exceed capacity.
    public boolean isRoomAvailable(Room room) {
        return room != null && room.getCurrentOccupancy() < room.getCapacity();
    }

    // Time: O(queue size) - Checks if student ID exists in waiting list.
    public boolean isWaiting(String studentId) {
        for (String waitingId : waitingQueue) {
            if (waitingId.equalsIgnoreCase(studentId)) return true;
        }
        return false;
    }

    // Counts rooms with at least one empty bed slot.
    public int getAvailableRoomCount() {
        int count = 0;
        for (Room room : rooms) {
            if (isRoomAvailable(room)) count++;
        }
        return count;
    }

    // Sums empty beds across all rooms.
    public int getAvailableBedCount() {
        int count = 0;
        for (Room room : rooms) {
            count += room.getCapacity() - room.getCurrentOccupancy();
        }
        return count;
    }

    // Business Rule: Male -> Boys hostel; Female -> Girls hostel.
    public boolean canStudentStayInRoom(Student student, Room room) {
        if (student == null || room == null) return false;
        String gender = student.getGender();
        String hostelType = room.getHostelType();
        if (gender == null || hostelType == null) return false;
        if (gender.equalsIgnoreCase("Male")) return hostelType.equalsIgnoreCase("Boys");
        if (gender.equalsIgnoreCase("Female")) return hostelType.equalsIgnoreCase("Girls");
        return false;
    }

    // Checks gender restrictions and occupancy limits for manual allocation.
    public boolean canRoomAcceptStudentForManualAllocation(Student student, Room room) {
        if (!canStudentStayInRoom(student, room)) return false;
        Room currentRoom = findRoom(student.getRoomNumber());
        if (currentRoom != null && currentRoom.getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
            return findNextBedNumber(room, student.getRoomNumber()) != null;
        }
        return isRoomAvailable(room);
    }

    // Checks if specific bed slot is unoccupied and gender compatible.
    public boolean canBedAcceptStudentForManualAllocation(Student student, Room room, String bedLabel) {
        if (!canStudentStayInRoom(student, room)) return false;
        if (bedLabel == null || bedLabel.trim().isEmpty()) return false;
        String bedNumber = room.getRoomNumber() + "-" + bedLabel;
        return !isBedOccupied(bedNumber);
    }

    // Time: O(rooms) - Scans for first compatible room with space.
    public Room findAvailableRoomForStudent(Student student) {
        for (Room room : rooms) {
            if (isRoomAvailable(room) && canStudentStayInRoom(student, room)) {
                return room;
            }
        }
        return null;
    }

    // Map hostel category to code prefixes BH/GH.
    public String getHostelPrefix(String hostelType) {
        if (hostelType != null && hostelType.equalsIgnoreCase("Girls")) return "GH";
        return "BH";
    }

    // Formats room components into unified database code.
    public String makeRoomNumber(String hostelType, String hostelNumber, String block, String roomNumber) {
        return getHostelPrefix(hostelType) + "-" + hostelNumber + "-" + block.toUpperCase() + "-" + roomNumber;
    }

    // ==========================================
    // Private Allocator Subroutines
    // ==========================================

    // Executes auto-allocation and saves state.
    private String allocateStudentAndSave(Student student) {
        String message = allocateStudentAutomatically(student);
        saveData();
        return message;
    }

    // Finds first compatible room or falls back to waiting queue.
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

    // Assigns next available bed and updates occupancy.
    private String allocateStudentToRoom(Student student, Room room) {
        String bedNumber = findNextBedNumber(room);
        student.setRoomNumber(bedNumber);
        room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        return bedNumber;
    }

    // Business Rule: Waiting queue follows FIFO.
    // Matches waiting students to newly freed room slots using FIFO.
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

    // Time: O(1) - Queue insertion of student ID.
    private void addToWaitingQueue(Student student) {
        if (!hasRoom(student) && !isWaiting(student.getStudentId())) {
            waitingQueue.offer(student.getStudentId());
        }
    }

    // Returns first free bed label in the room.
    private String findNextBedNumber(Room room) {
        String bedNumber = findNextBedNumber(room, "");
        if (bedNumber == null) return room.getRoomNumber() + "-BED-A";
        return bedNumber;
    }

    // Computes first unused bed label, ignoring specified skip label.
    private String findNextBedNumber(Room room, String bedToSkip) {
        for (int i = 0; i < room.getCapacity(); i++) {
            String bedNumber = room.getRoomNumber() + "-BED-" + (char)('A' + i);
            boolean skipThisBed = bedToSkip != null && bedNumber.equalsIgnoreCase(bedToSkip);
            if (!skipThisBed && !isBedOccupied(bedNumber)) return bedNumber;
        }
        return null;
    }

    // Time: O(n) - Scans if any student is assigned to bed code.
    private boolean isBedOccupied(String bedNumber) {
        for (Student student : students) {
            if (bedNumber.equalsIgnoreCase(student.getRoomNumber())) return true;
        }
        return false;
    }

    // Truncates bed label from room code.
    private String getRoomNumberFromBed(String roomOrBedNumber) {
        int bedIndex = roomOrBedNumber.toUpperCase().indexOf("-BED-");
        if (bedIndex >= 0) return roomOrBedNumber.substring(0, bedIndex);
        return roomOrBedNumber;
    }

    // ==========================================
    // Data Recovery & Clean-up Operations
    // ==========================================

    // Repairs legacy data and restores allocation consistency.
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

    // Resets allocations and rebuilds from scratch sequentially.
    private void rebuildAllAllocations() {
        waitingQueue = new LinkedList<String>();
        for (Room room : rooms) room.setCurrentOccupancy(0);
        for (Student student : students) student.setRoomNumber("");
        for (Student student : students) allocateStudentAutomatically(student);
    }

    // Rebuilds occupancy counts from active student allocations.
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

    // Removes invalid and duplicate waiting entries.
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

    // Applies default room configurations.
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

    // Frees current room by updating occupancy only.
    private Room releaseCurrentRoom(Student student) {
        Room room = findRoom(student.getRoomNumber());
        if (room != null) {
            room.setCurrentOccupancy(room.getCurrentOccupancy() - 1);
        }
        return room;
    }

    // Completes allocation, updating student room, queue and storage.
    private String finalizeAllocation(Student student, String newBed, String oldBed) {
        student.setRoomNumber(newBed);
        waitingQueue.remove(student.getStudentId());
        saveData();
        if (oldBed != null && !oldBed.isEmpty()) {
            return "Student moved from " + oldBed + " to " + newBed + ".";
        }
        return "Bed " + newBed + " allocated successfully.";
    }
}
