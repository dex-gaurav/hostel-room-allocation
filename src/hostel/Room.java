package hostel;

import java.io.Serializable;

/**
 * The Room class represents a physical room inside a hostel block.
 * It stores capacity configurations, current occupancy levels,
 * block locations, and hostel-type constraints (Boys/Girls).
 * It implements Serializable for persistent storage.
 */
public class Room implements Serializable {
    // Unique identifier for serialization integrity.
    private static final long serialVersionUID = 1L;

    // Room characteristics and allocation tracking fields
    private String roomNumber;       // Unique structured room code (e.g. "BH-01-A-101")
    private String block;            // Block name (e.g. "A", "B", "C", "D")
    private int capacity;            // Max students allowed in the room (e.g. 1 to 4)
    private int currentOccupancy;    // Current number of students allocated to this room
    private String roomType;         // Description of layout (e.g. "Single", "Four Sharing")
    private String hostelType;       // Hostel type restriction: "Boys" or "Girls"

    /**
     * Helper constructor to create a room defaulting to a "Boys" hostel.
     *
     * @param roomNumber String identifying the room
     * @param block      String block identifier
     * @param capacity   Maximum number of beds in the room
     * @param roomType   Layout configuration type
     */
    public Room(String roomNumber, String block, int capacity, String roomType) {
        this(roomNumber, block, capacity, roomType, "Boys");
    }

    /**
     * Primary constructor to fully initialize a room.
     *
     * @param roomNumber String identifying the room
     * @param block      String block identifier
     * @param capacity   Maximum number of beds in the room
     * @param roomType   Layout configuration type
     * @param hostelType Gender accommodation type restriction ("Boys" or "Girls")
     */
    public Room(String roomNumber, String block, int capacity, String roomType, String hostelType) {
        this.roomNumber = roomNumber;
        this.block = block;
        this.capacity = capacity;
        this.currentOccupancy = 0; // Starts with zero students allocated
        this.roomType = roomType;
        this.hostelType = hostelType;
    }

    // ==========================================
    // Getters
    // Provide read access to encapsulated fields
    // ==========================================

    /**
     * @return The room's unique code
     */
    public String getRoomNumber() { return roomNumber; }

    /**
     * @return The block letter/code
     */
    public String getBlock() { return block; }

    /**
     * @return Total bed capacity
     */
    public int getCapacity() { return capacity; }

    /**
     * @return Current number of allocated beds
     */
    public int getCurrentOccupancy() { return currentOccupancy; }

    /**
     * @return Description of room type
     */
    public String getRoomType() { return roomType; }

    /**
     * @return Hostel type restriction ("Boys" / "Girls")
     */
    public String getHostelType() { return hostelType; }

    // ==========================================
    // Setters
    // Provide write access to encapsulated fields
    // ==========================================

    /**
     * Updates the room number code
     * @param roomNumber New room number
     */
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    /**
     * Updates the block assignment
     * @param block New block
     */
    public void setBlock(String block) { this.block = block; }

    /**
     * Updates the maximum capacity of the room
     * @param capacity New capacity
     */
    public void setCapacity(int capacity) { this.capacity = capacity; }

    /**
     * Updates the current occupancy count of the room.
     * Guarantees occupancy doesn't drop below zero.
     * @param currentOccupancy New occupancy count
     */
    public void setCurrentOccupancy(int currentOccupancy) {
        this.currentOccupancy = Math.max(0, currentOccupancy);
    }

    /**
     * Updates the layout type description
     * @param roomType New room type
     */
    public void setRoomType(String roomType) { this.roomType = roomType; }

    /**
     * Updates the hostel gender restriction type
     * @param hostelType New hostel type
     */
    public void setHostelType(String hostelType) { this.hostelType = hostelType; }

    /**
     * Generates a string representation of the room, showing its code,
     * type restriction, and occupancy status. Useful for selection dropdowns.
     *
     * @return String displaying room number, hostel type, and occupancy/capacity ratio
     */
    public String toString() {
        return roomNumber + " - " + hostelType + " (" + currentOccupancy + "/" + capacity + ")";
    }
}

