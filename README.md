# CampusStay - Hostel Room Allocation System

CampusStay is a standalone Java Swing college project for managing hostel students,
rooms, allocation, a FIFO waiting list, checkout, and search. It uses only Core Java,
Swing/AWT, object-oriented programming, and Java serialization.

## Project structure

The complete application uses only six Java source files:

- `Main.java` starts the program.
- `MainFrame.java` contains the login and every application screen.
- `HostelManager.java` contains all hostel business rules.
- `Student.java` stores student data.
- `Room.java` stores room data.
- `FileManager.java` reads and writes serialized files.

## Run the project

JDK 8 or newer is required. Open PowerShell in the project folder and run:

```powershell
New-Item -ItemType Directory -Force out
javac -d out src\hostel\*.java
java -cp out hostel.Main
```

Login credentials:

- Username: `admin`
- Password: `admin123`

## Data storage

The program automatically loads and saves:

- `data/students.dat`
- `data/rooms.dat`
- `data/waitinglist.dat`

## Viva summary

- `ArrayList` stores students and rooms.
- `Queue` implements first-in, first-out waiting allocation.
- Java serialization stores objects in local `.dat` files.
- `MainFrame` replaces its center panel when a sidebar item is selected.
- `HostelManager` separates application rules from file handling and UI code.
- On checkout, the first valid waiting student receives the released room.
