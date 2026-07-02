package hostel;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roomNumber;
    private String block;
    private int capacity;
    private int currentOccupancy;
    private String roomType;
    private String hostelType;

    public Room(String roomNumber, String block, int capacity, String roomType) {
        this(roomNumber, block, capacity, roomType, "Boys");
    }

    public Room(String roomNumber, String block, int capacity, String roomType, String hostelType) {
        this.roomNumber = roomNumber;
        this.block = block;
        this.capacity = capacity;
        this.currentOccupancy = 0;
        this.roomType = roomType;
        this.hostelType = hostelType;
    }

    public String getRoomNumber() { return roomNumber; }
    public String getBlock() { return block; }
    public int getCapacity() { return capacity; }
    public int getCurrentOccupancy() { return currentOccupancy; }
    public String getRoomType() { return roomType; }
    public String getHostelType() { return hostelType; }

    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setBlock(String block) { this.block = block; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setCurrentOccupancy(int currentOccupancy) {
        this.currentOccupancy = Math.max(0, currentOccupancy);
    }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setHostelType(String hostelType) { this.hostelType = hostelType; }

    public String toString() {
        return roomNumber + " - " + hostelType + " (" + currentOccupancy + "/" + capacity + ")";
    }
}
