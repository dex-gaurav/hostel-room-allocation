package hostel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * HostelManager serves as the controller/business logic layer of the application.
 * It manages student allocations, room occupancies, the waiting list, and coordinates
 * saving/loading operations via FileManager. It also implements automatic room/bed allocation rules,
 * validation constraints (e.g., gender matching), and data consistency repairs.
 */
public class HostelManager {
    // Internal lists and queues tracking state
    private ArrayList<Student> students;
    private ArrayList<Room> rooms;
    private Queue<String> waitingQueue;

    /**
     * Constructor initializes the manager and immediately loads existing data from disk.
     */
    public HostelManager() { loadData(); }

    // ==========================================
    // Core State Accessors
    // ==========================================

    /**
     * @return The list of all registered students
     */
    public ArrayList<Student> getStudents() { return students; }

    /**
     * @return The list of all configured rooms
     */
    public ArrayList<Room> getRooms() { return rooms; }

    /**
     * @return The queue of student IDs currently waiting for room allocation
     */
    public Queue<String> getWaitingQueue() { return waitingQueue; }

    // ==========================================
    // Persistence Layer Integration
    // ==========================================

    /**
     * Loads student, room, and waiting queue data from storage files.
     * Also runs a validation/repair routine to correct potential data anomalies.
     */
    public void loadData() {
        students = FileManager.loadStudents();
        rooms = FileManager.loadRooms();
        waitingQueue = FileManager.loadWaitingQueue();
        repairOldDataIfNeeded(); // Validates consistency (e.g. correct counts, gender matches)
    }

    /**
     * Commits the current lists of students, rooms, and waiting queue back to storage files.
     */
    public void saveData() {
        FileManager.saveStudents(students);
        FileManager.saveRooms(rooms);
        FileManager.saveWaitingQueue(waitingQueue);
    }

    // ==========================================
    // Student Management Operations
    // ==========================================

    /**
     * Registers a new student in the system and triggers automatic room allocation.
     * If no compatible room has space, the student is added to the waiting queue.
     * Saves data automatically.
     *
     * @param student The new Student object to register
     * @return Status message indicating registration success and allocation details
     */
    public String addStudent(Student student) {
        if (findStudent(student.getStudentId()) != null) {
            return "A student with this ID already exists.";
        }
        students.add(student);
        String allocationMessage = allocateStudentAutomatically(student);
        saveData();
        return "Student added successfully.\n" + allocationMessage;
    }

    /**
     * Updates an existing student's details.
     * If the student's gender changes, they may no longer be compatible with their current room.
     * If incompatible, they are removed from the room, and auto-allocated elsewhere.
     *
     * @param studentId   ID of student to update
     * @param name        New name
     * @param gender      New gender
     * @param department  New department
     * @param year        New year of study
     * @param phoneNumber New contact number
     * @return Status message summarizing update details and potential re-allocations
     */
    public String editStudent(String studentId, String name, String gender,
                              String department, int year, String phoneNumber) {
        Student student = findStudent(studentId);
        if (student == null) return "Student record was not found.";

        String oldBed = student.getRoomNumber();
        Room oldRoom = findRoom(oldBed);
        
        // Update details
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
            
            // Try to find a new compatible room, and allocate waiting students to the newly freed bed
            message += "\n" + allocateStudentAutomatically(student);
            message += allocateWaitingStudentsToRoom(oldRoom);
        }
        saveData();
        return message;
    }

    /**
     * Deletes a student from the system.
     * Frees up their bed, removes them from the waiting queue, and allocates waiting
     * students into the freed room if applicable.
     *
     * @param studentId The ID of the student to delete
     * @return Status message
     */
    public String deleteStudent(String studentId) {
        Student student = findStudent(studentId);
        if (student == null) return "Student record was not found.";
        
        Room oldRoom = releaseCurrentRoom(student);
        waitingQueue.remove(student.getStudentId());
        students.remove(student);
        
        // If a bed was freed, try to immediately allocate a student from the waiting list
        String queueMessage = allocateWaitingStudentsToRoom(oldRoom);
        saveData();
        if (queueMessage.isEmpty()) return "Student deleted successfully.";
        return "Student deleted successfully." + queueMessage;
    }

    /**
     * Searches students based on match criteria (ID, Name, Gender, or Room Number).
     *
     * @param searchText Search filter text
     * @return Filtered list of matching students
     */
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

    /**
     * Configures and registers a new room in the system.
     * Normalizes default room configurations and attempts to fill the room
     * if there are students waiting on the waiting list.
     *
     * @param room The new Room object to add
     * @return Status message
     */
    public String addRoom(Room room) {
        normalizeRoom(room);
        if (findRoom(room.getRoomNumber()) != null) return "This room number already exists.";
        rooms.add(room);
        
        // Check if any waiting students can be accommodated in this new room
        String queueMessage = allocateWaitingStudentsToRoom(room);
        saveData();
        if (queueMessage.isEmpty()) return "Room added successfully.";
        return "Room added successfully." + queueMessage;
    }

    /**
     * Deletes a room config if it is unoccupied.
     * Occupied rooms cannot be deleted to prevent student record corruption.
     *
     * @param roomNumber The room code identifier to remove
     * @return Status message
     */
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

    /**
     * Allocates a student to a room automatically or processes a room-change request.
     * Validates gender compatibility constraints.
     *
     * @param student The student being allocated
     * @param room    Target room (if null, auto-allocates to any available)
     * @return Status message summarizing the result of the allocation
     */
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
        
        // If the student is already in this room, assign them a different bed slot inside this room
        if (hasRoom(student) && oldRoom != null
                && oldRoom.getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
            String newBed = findNextBedNumber(room, oldBed);
            if (newBed == null) return "No other free bed is available in the selected room.";
            student.setRoomNumber(newBed);
            waitingQueue.remove(student.getStudentId());
            saveData();
            return "Bed changed from " + oldBed + " to " + newBed + ".";
        }

        // Validate if room is full
        if (!isRoomAvailable(room)) {
            if (!hasRoom(student)) {
                addToWaitingQueue(student);
                saveData();
                return "Selected room is full. The student was added to the waiting list.";
            }
            return "Selected room is full. Please choose another compatible room.";
        }

        // Process standard relocation
        if (hasRoom(student)) {
            releaseCurrentRoom(student);
        }
        String bedNumber = allocateStudentToRoom(student, room);
        return finalizeAllocation(student, bedNumber, oldBed);
    }

    /**
     * Manually allocates a student to a specific, labeled bed inside a room.
     * Evaluates security rules such as occupancy conflicts and gender matches.
     *
     * @param student  The student record
     * @param room     Target room configuration
     * @param bedLabel Label code (e.g. "BED-A")
     * @return Status message
     */
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

        // Adjust room occupancy levels based on movement direction
        Room oldRoom = findRoom(oldBed);
        if (oldRoom == null) {
            room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        } else if (!oldRoom.getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
            releaseCurrentRoom(student);
            room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        }

        return finalizeAllocation(student, newBed, oldBed);
    }

    /**
     * Checks a student out of their room, removing them from the system.
     * Automatically attempts to allocate a waiting student to the newly freed slot.
     *
     * @param student Student checking out
     * @return Status message
     */
    public String checkoutStudent(Student student) {
        if (student == null) return "Please select a student.";
        if (!hasRoom(student)) return "This student does not have an allocated room.";
        Room room = releaseCurrentRoom(student);
        String freedBed = student.getRoomNumber();
        
        // Remove student records
        students.remove(student);
        waitingQueue.remove(student.getStudentId());

        // Allocate newly vacant spot to anyone waiting
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

    /**
     * Finds a student by their ID.
     *
     * @param studentId The student ID string
     * @return The Student object, or null if not found
     */
    public Student findStudent(String studentId) {
        for (Student student : students) {
            if (student.getStudentId().equalsIgnoreCase(studentId)) return student;
        }
        return null;
    }

    /**
     * Finds a room by either its room code or an individual bed code prefix.
     *
     * @param roomOrBedNumber String identifying room or bed
     * @return The Room object, or null if not found
     */
    public Room findRoom(String roomOrBedNumber) {
        if (roomOrBedNumber == null || roomOrBedNumber.isEmpty()) return null;
        String roomNumber = getRoomNumberFromBed(roomOrBedNumber);
        for (Room room : rooms) {
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) return room;
        }
        return null;
    }

    /**
     * Checks if a student has an assigned room.
     *
     * @param student Student object to inspect
     * @return True if student has room assigned, false otherwise
     */
    public boolean hasRoom(Student student) {
        return student != null && student.getRoomNumber() != null && !student.getRoomNumber().isEmpty();
    }

    /**
     * Evaluates if a room has at least one vacant bed.
     *
     * @param room Room object to evaluate
     * @return True if room occupancy is less than capacity
     */
    public boolean isRoomAvailable(Room room) {
        return room != null && room.getCurrentOccupancy() < room.getCapacity();
    }

    /**
     * Checks if a student ID is present on the waiting list.
     *
     * @param studentId Student ID
     * @return True if student is waiting
     */
    public boolean isWaiting(String studentId) {
        for (String waitingId : waitingQueue) {
            if (waitingId.equalsIgnoreCase(studentId)) return true;
        }
        return false;
    }

    /**
     * Counts the total number of rooms that have at least one vacant bed.
     *
     * @return Count of available rooms
     */
    public int getAvailableRoomCount() {
        int count = 0;
        for (Room room : rooms) {
            if (isRoomAvailable(room)) count++;
        }
        return count;
    }

    /**
     * Calculates the sum of all empty beds across all registered rooms.
     *
     * @return Total empty bed count
     */
    public int getAvailableBedCount() {
        int count = 0;
        for (Room room : rooms) {
            count += room.getCapacity() - room.getCurrentOccupancy();
        }
        return count;
    }

    /**
     * Verifies that student gender is compatible with hostel gender restrictions.
     * Boys hostel accommodates Male students; Girls hostel accommodates Female students.
     *
     * @param student The student in question
     * @param room    The target room
     * @return True if genders match the hostel block restriction
     */
    public boolean canStudentStayInRoom(Student student, Room room) {
        if (student == null || room == null) return false;
        String gender = student.getGender();
        String hostelType = room.getHostelType();
        if (gender == null || hostelType == null) return false;
        if (gender.equalsIgnoreCase("Male")) return hostelType.equalsIgnoreCase("Boys");
        if (gender.equalsIgnoreCase("Female")) return hostelType.equalsIgnoreCase("Girls");
        return false;
    }

    /**
     * Evaluates if a room can accept a student for a manual allocation operation.
     * Checks gender compliance and free slot availability.
     *
     * @param student Student
     * @param room    Target room
     * @return True if room can receive the student
     */
    public boolean canRoomAcceptStudentForManualAllocation(Student student, Room room) {
        if (!canStudentStayInRoom(student, room)) return false;
        Room currentRoom = findRoom(student.getRoomNumber());
        // If they are already in this room, they can move to another bed inside it
        if (currentRoom != null && currentRoom.getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
            return findNextBedNumber(room, student.getRoomNumber()) != null;
        }
        return isRoomAvailable(room);
    }

    /**
     * Evaluates if a specific bed slot label is free and compatible for a student.
     *
     * @param student  Student
     * @param room     Target room
     * @param bedLabel Specific bed label
     * @return True if target bed slot is unoccupied and compatible
     */
    public boolean canBedAcceptStudentForManualAllocation(Student student, Room room, String bedLabel) {
        if (!canStudentStayInRoom(student, room)) return false;
        if (bedLabel == null || bedLabel.trim().isEmpty()) return false;
        String bedNumber = room.getRoomNumber() + "-" + bedLabel;
        return !isBedOccupied(bedNumber);
    }

    /**
     * Searches for the first available room matching the student's gender constraints.
     *
     * @param student Student to search for
     * @return First compatible Room found with space, or null if none
     */
    public Room findAvailableRoomForStudent(Student student) {
        for (Room room : rooms) {
            if (isRoomAvailable(room) && canStudentStayInRoom(student, room)) {
                return room;
            }
        }
        return null;
    }

    /**
     * Determines the prefix acronym for the hostel block.
     *
     * @param hostelType "Boys" or "Girls"
     * @return "GH" for girls hostel, "BH" for boys hostel
     */
    public String getHostelPrefix(String hostelType) {
        if (hostelType != null && hostelType.equalsIgnoreCase("Girls")) return "GH";
        return "BH";
    }

    /**
     * Formats details into a standard room code string (e.g. "BH-01-A-101").
     *
     * @param hostelType   Hostel restriction type
     * @param hostelNumber Hostel number
     * @param block        Block letter
     * @param roomNumber   Room number digits
     * @return Standardized room number string
     */
    public String makeRoomNumber(String hostelType, String hostelNumber, String block, String roomNumber) {
        return getHostelPrefix(hostelType) + "-" + hostelNumber + "-" + block.toUpperCase() + "-" + roomNumber;
    }

    // ==========================================
    // Private Allocator Subroutines
    // ==========================================

    /**
     * Auto-allocates student to an available room and saves the state.
     *
     * @param student Student
     * @return Status report
     */
    private String allocateStudentAndSave(Student student) {
        String message = allocateStudentAutomatically(student);
        saveData();
        return message;
    }

    /**
     * Business logic for automatically assigning a bed to a student.
     * Places the student on the waiting queue if no room fits.
     *
     * @param student Student to allocate
     * @return Message describing where allocated or if placed on waiting list
     */
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

    /**
     * Commits a student to a room by determining the next bed letter and incrementing room occupancy.
     *
     * @param student Target student
     * @param room    Target room
     * @return Allocated bed identifier
     */
    private String allocateStudentToRoom(Student student, Room room) {
        String bedNumber = findNextBedNumber(room);
        student.setRoomNumber(bedNumber);
        room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        return bedNumber;
    }

    /**
     * Scans the waiting list and allocates as many qualifying students as possible to a vacant room.
     * Implements FIFO (first-in, first-out) matching relative to room availability.
     *
     * @param room The room that has free slots
     * @return Log of allocations that occurred
     */
    private String allocateWaitingStudentsToRoom(Room room) {
        if (room == null || !isRoomAvailable(room) || waitingQueue.isEmpty()) return "";
        int originalSize = waitingQueue.size();
        int allocatedCount = 0;
        String names = "";
        
        // Loop through the current waiting queue elements
        for (int i = 0; i < originalSize; i++) {
            String studentId = waitingQueue.poll();
            Student student = findStudent(studentId);
            // Ignore if student record is deleted or they already got a room
            if (student == null || hasRoom(student)) {
                continue;
            }
            // Check compatibility and room capacity
            if (isRoomAvailable(room) && canStudentStayInRoom(student, room)) {
                String bedNumber = allocateStudentToRoom(student, room);
                allocatedCount++;
                names += "\n- " + student.getName() + " -> " + bedNumber;
            } else {
                // If they cannot be accommodated in this room, place them back in the queue
                waitingQueue.offer(studentId);
            }
        }
        if (allocatedCount == 0) return "";
        return "\nAllocated " + allocatedCount + " waiting student(s):" + names;
    }

    /**
     * Appends a student ID to the waiting queue if they aren't already allocated or in queue.
     *
     * @param student Student record
     */
    private void addToWaitingQueue(Student student) {
        if (!hasRoom(student) && !isWaiting(student.getStudentId())) {
            waitingQueue.offer(student.getStudentId());
        }
    }

    /**
     * Wrapper to find next free bed letter. Defaulting to BED-A if no index matches.
     */
    private String findNextBedNumber(Room room) {
        String bedNumber = findNextBedNumber(room, "");
        if (bedNumber == null) return room.getRoomNumber() + "-BED-A";
        return bedNumber;
    }

    /**
     * Computes the first unused bed label ('A' to capacity index) in a room.
     * Allows skipping a specific bed if doing a relocation check.
     *
     * @param room      Room configuration
     * @param bedToSkip Bed ID to ignore
     * @return Unused bed code (e.g. "BH-01-A-101-BED-C"), or null if full
     */
    private String findNextBedNumber(Room room, String bedToSkip) {
        for (int i = 0; i < room.getCapacity(); i++) {
            String bedNumber = room.getRoomNumber() + "-BED-" + (char)('A' + i);
            boolean skipThisBed = bedToSkip != null && bedNumber.equalsIgnoreCase(bedToSkip);
            if (!skipThisBed && !isBedOccupied(bedNumber)) return bedNumber;
        }
        return null;
    }

    /**
     * Iterates all students to check if anyone is currently assigned to the given bed label.
     *
     * @param bedNumber Fully qualified bed code
     * @return True if bed is occupied
     */
    private boolean isBedOccupied(String bedNumber) {
        for (Student student : students) {
            if (bedNumber.equalsIgnoreCase(student.getRoomNumber())) return true;
        }
        return false;
    }

    /**
     * Parses the room code prefix from a bed code string.
     * E.g., extracts "BH-01-A-101" from "BH-01-A-101-BED-A".
     *
     * @param roomOrBedNumber String to parse
     * @return Extracted room code
     */
    private String getRoomNumberFromBed(String roomOrBedNumber) {
        int bedIndex = roomOrBedNumber.toUpperCase().indexOf("-BED-");
        if (bedIndex >= 0) return roomOrBedNumber.substring(0, bedIndex);
        return roomOrBedNumber;
    }

    // ==========================================
    // Data Recovery & Clean-up Operations
    // ==========================================

    /**
     * Evaluates data integrity of loaded files.
     * Fixes default fields for old room entries, fixes bed string formats,
     * and performs layout rebuilds/recounts when anomalies are found.
     */
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

    /**
     * Complete reset of room allocations. Rebuilds from scratch by clearing room occupancies,
     * resetting student assignments, and allocating everyone sequentially.
     */
    private void rebuildAllAllocations() {
        waitingQueue = new LinkedList<String>();
        for (Room room : rooms) room.setCurrentOccupancy(0);
        for (Student student : students) student.setRoomNumber("");
        for (Student student : students) allocateStudentAutomatically(student);
    }

    /**
     * Re-scans active students to count occupancy values for all rooms.
     * Detects invalid assignments, clears them, and places affected students on the waiting list.
     */
    private void recountRoomOccupancy() {
        for (Room room : rooms) room.setCurrentOccupancy(0);
        for (Student student : students) {
            if (hasRoom(student)) {
                Room room = findRoom(student.getRoomNumber());
                if (room != null && canStudentStayInRoom(student, room) && room.getCurrentOccupancy() < room.getCapacity()) {
                    room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
                } else {
                    // Invalid/overflow room assignment detected: clear and enqueue
                    student.setRoomNumber("");
                    addToWaitingQueue(student);
                }
            }
        }
    }

    /**
     * Removes duplicate or invalid entries from the waiting list.
     */
    private void cleanWaitingQueue() {
        Queue<String> oldQueue = waitingQueue;
        waitingQueue = new LinkedList<String>();
        for (String studentId : oldQueue) {
            Student student = findStudent(studentId);
            // Only keep in waiting list if student exists and has no room yet
            if (student != null && !hasRoom(student) && !isWaiting(studentId)) {
                waitingQueue.offer(studentId);
            }
        }
    }

    /**
     * Ensures room capacity, type restrictions, and occupancy limits conform to system standards.
     * Prevents null values and invalid ranges.
     *
     * @param room Room object to normalize
     */
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

    /**
     * Decrements the occupancy of the room currently assigned to the student (if any).
     * Reduces duplicate room deallocation code across various operations.
     *
     * @param student The resident student
     * @return The Room object that was freed, or null if none
     */
    private Room releaseCurrentRoom(Student student) {
        Room room = findRoom(student.getRoomNumber());
        if (room != null) {
            room.setCurrentOccupancy(room.getCurrentOccupancy() - 1);
        }
        return room;
    }

    /**
     * Finalizes the allocation by setting the bed number, removing the student from the waiting list,
     * saving the data, and returning the standardized success message.
     *
     * @param student The student being allocated
     * @param newBed  The fully qualified bed code assigned
     * @param oldBed  The student's previous bed code (if any)
     * @return Standardized success message detailing allocation or move
     */
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

