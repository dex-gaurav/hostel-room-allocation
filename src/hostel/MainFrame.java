package hostel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Main application window.
// Demonstrates Swing components, custom painting, layouts, and event listeners.
// Integrates with HostelManager for room allocation logic.
public class MainFrame extends JFrame {
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color DARK_BLUE = new Color(30, 64, 175);
    private static final Color SIDEBAR = new Color(20, 35, 61);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color TEXT = new Color(31, 41, 55);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color BORDER = new Color(226, 232, 240);
    
    private static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 26);

    private HostelManager manager;
    private JPanel contentPanel;
    private Map<String, JButton> menuButtons;

    // Configures main window constraints (title, close operation, bounds).
    // Displays the initial Login view and links the shared HostelManager controller.
    public MainFrame(HostelManager manager) {
        this.manager = manager;
        this.menuButtons = new LinkedHashMap<String, JButton>();
        setTitle("CampusStay - Hostel Room Allocation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        showLogin();
    }

    // Builds the Login UI using BorderLayout + GridBagLayout.
    // Handles admin authentication before opening the main workspace.
    private void showLogin() {
        setSize(920, 590);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        JPanel brandPanel = new JPanel(new GridBagLayout());
        brandPanel.setPreferredSize(new Dimension(390, 0));
        brandPanel.setBackground(SIDEBAR);
        GridBagConstraints brand = new GridBagConstraints();
        brand.gridx = 0;
        brand.insets = new Insets(8, 25, 8, 25);
        
        brand.gridy = 0;
        brandPanel.add(new JLabel(new ImageIcon("resources/logo.png")), brand);
        
        JLabel title = new JLabel("CampusStay");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        brand.gridy = 1;
        brandPanel.add(title, brand);
        
        JLabel subtitle = new JLabel("Hostel Room Allocation System");
        subtitle.setForeground(new Color(190, 204, 226));
        subtitle.setFont(NORMAL_FONT);
        brand.gridy = 2;
        brandPanel.add(subtitle, brand);

        final JTextField usernameField = new JTextField();
        final JPasswordField passwordField = new JPasswordField();
        styleInput(usernameField);
        styleInput(passwordField);
        
        JPanel loginCard = createCard();
        loginCard.setLayout(new GridBagLayout());
        loginCard.setPreferredSize(new Dimension(390, 390));
        GridBagConstraints form = new GridBagConstraints();
        form.gridx = 0;
        form.fill = GridBagConstraints.HORIZONTAL;
        form.weightx = 1;
        
        JLabel loginTitle = new JLabel("Welcome back");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 27));
        loginTitle.setForeground(TEXT);
        form.gridy = 0;
        form.insets = new Insets(6, 12, 6, 12);
        loginCard.add(loginTitle, form);
        
        JLabel loginText = muted("Sign in to manage hostel rooms");
        form.gridy = 1;
        form.insets = new Insets(0, 12, 20, 12);
        loginCard.add(loginText, form);
        
        form.insets = new Insets(6, 12, 6, 12);
        form.gridy = 2;
        loginCard.add(new JLabel("Username"), form);
        
        form.gridy = 3;
        loginCard.add(usernameField, form);
        
        form.gridy = 4;
        loginCard.add(new JLabel("Password"), form);
        
        form.gridy = 5;
        loginCard.add(passwordField, form);
        
        JButton loginButton = primaryButton("Sign In");
        form.gridy = 6;
        form.insets = new Insets(22, 12, 8, 12);
        loginCard.add(loginButton, form);
        
        JLabel hint = new JLabel("Demo: admin / admin123", SwingConstants.CENTER);
        hint.setForeground(MUTED);
        form.gridy = 7;
        form.insets = new Insets(6, 12, 6, 12);
        loginCard.add(hint, form);

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                if (username.equals("admin") && password.equals("admin123")) {
                    showMainWindow();
                } else {
                    showError("Incorrect username or password.", "Login Failed");
                    passwordField.setText("");
                }
            }
        });

        JPanel formArea = new JPanel(new GridBagLayout());
        formArea.setOpaque(false);
        formArea.add(loginCard);
        root.add(brandPanel, BorderLayout.WEST);
        root.add(formArea, BorderLayout.CENTER);
        setContentPane(root);
        getRootPane().setDefaultButton(loginButton);
        revalidate();
        repaint();
        setLocationRelativeTo(null);
    }

    // Transitions UI to the main workspace layout (Sidebar + CardLayout content area).
    private void showMainWindow() {
        setSize(1240, 760);
        menuButtons.clear();
        JPanel root = new JPanel(new BorderLayout());
        root.add(createSidebar(), BorderLayout.WEST);
        
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(BACKGROUND);
        root.add(contentPanel, BorderLayout.CENTER);
        
        setContentPane(root);
        showDashboard();
        revalidate();
        repaint();
        setLocationRelativeTo(null);
    }

    // Creates navigation sidebar using BoxLayout and event listeners.
    // Switches views and saves data on user logout.
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(225, 0));
        sidebar.setBorder(new EmptyBorder(22, 14, 18, 14));

        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setOpaque(false);
        brand.setMaximumSize(new Dimension(210, 70));
        brand.add(new JLabel(new ImageIcon("resources/logo-small.png")), BorderLayout.WEST);
        JLabel name = new JLabel("CampusStay");
        name.setForeground(Color.WHITE);
        name.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.add(name, BorderLayout.CENTER);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(25));

        String[] items = {"Dashboard", "Students", "Rooms", "Allocate",
                "Waiting List", "Checkout", "Search", "About"};
        for (String item : items) {
            JButton button = createMenuButton(item);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(5));
            menuButtons.put(item, button);
        }
        
        sidebar.add(Box.createVerticalGlue());
        JButton logout = createMenuButton("Logout");
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (askConfirm("Do you want to log out?", "Logout")) {
                    manager.saveData();
                    showLogin();
                }
            }
        });
        sidebar.add(logout);
        return sidebar;
    }

    // Instantiates and styles menu buttons using dynamic resource icon paths.
    private JButton createMenuButton(final String name) {
        JButton button = new JButton(name);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(new Color(203, 213, 225));
        button.setBackground(SIDEBAR);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 14, 11, 14));
        button.setMaximumSize(new Dimension(210, 43));
        button.setFont(NORMAL_FONT);
        
        String iconName = name.toLowerCase().replace(" ", "-") + ".png";
        button.setIcon(new ImageIcon("resources/" + iconName));
        button.setIconTextGap(12);
        
        if (!name.equals("Logout")) {
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) { openScreen(name); }
            });
        }
        return button;
    }

    // Triggers screen switching inside the CardLayout and highlights active menu selection.
    private void openScreen(String name) {
        switch (name) {
            case "Dashboard":    showDashboard(); break;
            case "Students":     showStudents(); break;
            case "Rooms":        showRooms(); break;
            case "Allocate":     showAllocation(); break;
            case "Waiting List": showWaitingList(); break;
            case "Checkout":     showCheckout(); break;
            case "Search":       showSearch(); break;
            case "About":        showAbout(); break;
        }
        highlightMenu(name);
    }

    // Replaces active panel in content CardLayout container and triggers repaint.
    private void setScreen(JPanel screen) {
        contentPanel.removeAll();
        contentPanel.add(screen);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Changes sidebar buttons colors to indicate active menu selection.
    private void highlightMenu(String selectedName) {
        for (String name : menuButtons.keySet()) {
            JButton button = menuButtons.get(name);
            button.setBackground(name.equals(selectedName) ? BLUE : SIDEBAR);
            button.setForeground(name.equals(selectedName) ? Color.WHITE : new Color(203, 213, 225));
        }
    }

    // Builds dashboard metrics banner using GridLayout and summary metric panels.
    private void showDashboard() {
        JPanel screen = createScreen("Dashboard", "Welcome back, Admin. Here is today's hostel overview.");
        JPanel center = new JPanel(new BorderLayout(0, 24));
        center.setOpaque(false);
        
        JPanel cards = new JPanel(new GridLayout(1, 4, 18, 0));
        cards.setOpaque(false);
        cards.add(summaryCard("Total Students", manager.getStudents().size(), "students.png", BLUE));
        cards.add(summaryCard("Total Rooms", manager.getRooms().size(), "rooms.png", new Color(14, 116, 144)));
        cards.add(summaryCard("Available Beds", manager.getAvailableBedCount(), "allocate.png", new Color(5, 150, 105)));
        cards.add(summaryCard("Waiting Students", manager.getWaitingQueue().size(), "waiting-list.png", new Color(217, 119, 6)));
        center.add(cards, BorderLayout.NORTH);

        JPanel welcomeCard = createCard();
        welcomeCard.setLayout(new BorderLayout(20, 0));
        welcomeCard.add(new JLabel(new ImageIcon("resources/dashboard-large.png")), BorderLayout.WEST);
        
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        
        text.add(Box.createVerticalGlue());
        text.add(heading("Simple hostel management, all in one place"));
        text.add(Box.createVerticalStrut(8));
        text.add(muted("Use the sidebar to manage students, rooms, beds, and checkouts."));
        text.add(Box.createVerticalGlue());
        
        welcomeCard.add(text, BorderLayout.CENTER);
        center.add(welcomeCard, BorderLayout.CENTER);
        screen.add(center, BorderLayout.CENTER);
        setScreen(screen);
        highlightMenu("Dashboard");
    }

    // Creates formatted summary card with subcomponents and colored text highlights.
    private JPanel summaryCard(String titleText, int number, String iconName, Color accent) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        JLabel title = new JLabel(titleText);
        title.setForeground(MUTED);
        
        JLabel value = new JLabel(String.valueOf(number));
        value.setFont(new Font("Segoe UI", Font.BOLD, 30));
        value.setForeground(accent);
        
        card.add(title, BorderLayout.NORTH);
        card.add(value, BorderLayout.WEST);
        card.add(new JLabel(new ImageIcon("resources/" + iconName)), BorderLayout.EAST);
        return card;
    }

    // Renders student records table with search filter toolbar.
    // Hooks action listeners for adding, editing, and deleting student records.
    private void showStudents() {
        JPanel card = borderCard(14);

        final JTextField searchField = new JTextField();
        styleInput(searchField);
        searchField.setPreferredSize(new Dimension(260, 38));
        JButton searchButton = secondaryButton("Search");
        JButton addButton = primaryButton("+  Add Student");
        JButton editButton = secondaryButton("Edit");
        JButton deleteButton = secondaryButton("Delete");
        
        JPanel tools = new JPanel(new BorderLayout());
        tools.setOpaque(false);
        tools.add(flowPanel(FlowLayout.LEFT, searchField, searchButton), BorderLayout.WEST);
        tools.add(flowPanel(FlowLayout.RIGHT, editButton, deleteButton, addButton), BorderLayout.EAST);
        card.add(tools, BorderLayout.NORTH);

        final DefaultTableModel model = readOnlyModel(new String[]{"Student ID", "Name", "Gender",
                "Department", "Year", "Phone", "Room"});
        final JTable table = new JTable(model);
        fillStudentTable(model, manager.getStudents());
        
        table.getColumnModel().getColumn(0).setPreferredWidth(90);  // Student ID
        table.getColumnModel().getColumn(1).setPreferredWidth(140); // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(70);  // Gender
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Department
        table.getColumnModel().getColumn(4).setPreferredWidth(50);  // Year
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Phone
        table.getColumnModel().getColumn(6).setPreferredWidth(180); // Room (Hostel/Bed details)

        card.add(styleAndWrapTable(table), BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                fillStudentTable(model, manager.searchStudents(searchField.getText()));
            }
        });
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) { showStudentForm(null); }
        });
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Student student = selectedStudent(table, model);
                if (student == null) showSelectMessage("student");
                else showStudentForm(student);
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Student student = selectedStudent(table, model);
                if (student == null) {
                    showSelectMessage("student");
                    return;
                }
                if (askConfirm("Delete " + student.getName() + "?", "Confirm Delete")) {
                    showInfo(manager.deleteStudent(student.getStudentId()));
                    showStudents();
                }
            }
        });
        
        setupBasePage("Student Management", "Add, edit, delete, and find student records", card);
    }

    // Renders Add/Edit Student modal dialog.
    // Validates inputs (non-empty, 10-digit phone) before calling HostelManager.
    private void showStudentForm(final Student existingStudent) {
        final JTextField idField = new JTextField();
        final JTextField nameField = new JTextField();
        final JComboBox<String> genderBox = new JComboBox<String>(new String[]{"Male", "Female"});
        final JTextField departmentField = new JTextField();
        final JComboBox<String> yearBox = new JComboBox<String>(new String[]{"1", "2", "3", "4"});
        final JTextField phoneField = new JTextField();
        
        if (existingStudent != null) {
            idField.setText(existingStudent.getStudentId());
            idField.setEnabled(false);
            nameField.setText(existingStudent.getName());
            genderBox.setSelectedItem(existingStudent.getGender());
            departmentField.setText(existingStudent.getDepartment());
            yearBox.setSelectedItem(String.valueOf(existingStudent.getYear()));
            phoneField.setText(existingStudent.getPhoneNumber());
        }
        
        JPanel form = createGridForm(
            new String[]{"Student ID", "Name", "Gender", "Department", "Year", "Phone Number"},
            new Component[]{idField, nameField, genderBox, departmentField, yearBox, phoneField}
        );
        
        String title = existingStudent == null ? "Add Student" : "Edit Student";
        int answer = JOptionPane.showConfirmDialog(this, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;

        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();
        String phone = phoneField.getText().trim();
        
        if (id.isEmpty() || name.isEmpty() || department.isEmpty() || phone.isEmpty()) {
            showWarning("Please fill in all fields.");
            return;
        }
        if (!phone.matches("[0-9]{10}")) {
            showWarning("Phone number must contain exactly 10 digits.");
            return;
        }
        
        String message;
        if (existingStudent == null) {
            Student student = new Student(id, name, String.valueOf(genderBox.getSelectedItem()),
                    department, Integer.parseInt(String.valueOf(yearBox.getSelectedItem())), phone);
            message = manager.addStudent(student);
        } else {
            message = manager.editStudent(id, name, String.valueOf(genderBox.getSelectedItem()),
                    department, Integer.parseInt(String.valueOf(yearBox.getSelectedItem())), phone);
        }
        showInfo(message);
        showStudents();
    }

    // Renders room details table with options to add new or delete vacant rooms.
    private void showRooms() {
        JPanel card = borderCard(14);
        
        JButton addButton = primaryButton("+  Add Room");
        JButton deleteButton = secondaryButton("Delete Selected Room");
        
        card.add(flowPanel(FlowLayout.RIGHT, deleteButton, addButton), BorderLayout.NORTH);

        final DefaultTableModel model = readOnlyModel(new String[]{"Room Number", "Hostel", "Block", "Type",
                "Capacity", "Occupancy", "Status"});
        final JTable table = new JTable(model);
        for (Room room : manager.getRooms()) {
            model.addRow(new Object[]{room.getRoomNumber(), room.getHostelType(), room.getBlock(), room.getRoomType(),
                    room.getCapacity(), room.getCurrentOccupancy(),
                    (room.getCapacity() - room.getCurrentOccupancy()) + " of " + room.getCapacity() + " available"});
        }
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0 && e.getClickCount() == 2) {
                    showRoomDetails(String.valueOf(model.getValueAt(row, 0)));
                }
            }
        });

        card.add(styleAndWrapTable(table), BorderLayout.CENTER);
        
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) { showRoomForm(); }
        });
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int row = table.getSelectedRow();
                if (row < 0) {
                    showSelectMessage("room");
                    return;
                }
                String roomNumber = String.valueOf(model.getValueAt(row, 0));
                if (askConfirm("Delete room " + roomNumber + "?", "Confirm Delete")) {
                    showInfo(manager.deleteRoom(roomNumber));
                    showRooms();
                }
            }
        });
        
        setupBasePage("Room Management", "Manage room capacity and availability", card);
    }

    // Renders Add Room modal dialog and builds formatted room number prefixes.
    private void showRoomForm() {
        JComboBox<String> hostelTypeBox = new JComboBox<String>(new String[]{"Boys", "Girls"});
        JComboBox<String> hostelNumberBox = new JComboBox<String>(new String[]{"01", "02", "03", "04"});
        JComboBox<String> blockBox = new JComboBox<String>(new String[]{"A", "B", "C", "D"});
        JTextField roomField = new JTextField("201");
        JComboBox<String> capacityBox = new JComboBox<String>(new String[]{"1", "2", "3", "4"});
        JComboBox<String> typeBox = new JComboBox<String>(new String[]{"Single", "Double", "Triple", "Four Sharing"});
        
        capacityBox.setSelectedItem("4");
        typeBox.setSelectedItem("Four Sharing");
        
        JPanel form = createGridForm(
            new String[]{"Hostel Type", "Hostel No.", "Block", "Room No.", "Capacity", "Room Type"},
            new Component[]{hostelTypeBox, hostelNumberBox, blockBox, roomField, capacityBox, typeBox}
        );
        
        int answer = JOptionPane.showConfirmDialog(this, form, "Add Room",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        
        String hostelType = String.valueOf(hostelTypeBox.getSelectedItem());
        String hostelNumber = String.valueOf(hostelNumberBox.getSelectedItem());
        String block = String.valueOf(blockBox.getSelectedItem());
        String roomCode = roomField.getText().trim();
        
        if (roomCode.isEmpty() || !roomCode.matches("[0-9]{3}")) {
            showWarning("Room number must be 3 digits, like 201.");
            return;
        }
        
        String roomNumber = manager.makeRoomNumber(hostelType, hostelNumber, block, roomCode);
        Room room = new Room(roomNumber, block,
                Integer.parseInt(String.valueOf(capacityBox.getSelectedItem())),
                String.valueOf(typeBox.getSelectedItem()), hostelType);
        
        showInfo(manager.addRoom(room));
        showRooms();
    }

    // Renders pop-up window showing occupants (Student ID, Name, Bed) for the clicked room.
    private void showRoomDetails(String roomNumber) {
        Room room = manager.findRoom(roomNumber);
        if (room == null) return;
        
        ArrayList<Student> residents = new ArrayList<Student>();
        for (Student s : manager.getStudents()) {
            if (s.getRoomNumber() != null && s.getRoomNumber().startsWith(roomNumber + "-")) {
                residents.add(s);
            }
        }
        
        if (residents.isEmpty()) {
            showInfo("No students are currently allocated to room " + roomNumber + ".");
            return;
        }
        
        DefaultTableModel detailModel = readOnlyModel(new String[]{"Student ID", "Name", "Bed"});
        for (Student s : residents) {
            String bed = s.getRoomNumber().substring(s.getRoomNumber().lastIndexOf("-") + 1);
            detailModel.addRow(new Object[]{s.getStudentId(), s.getName(), bed});
        }
        
        JTable detailTable = new JTable(detailModel);
        styleTable(detailTable);
        detailTable.setPreferredScrollableViewportSize(new Dimension(450, 150));
        
        JPanel popupPanel = new JPanel(new BorderLayout(0, 10));
        popupPanel.setOpaque(false);
        popupPanel.add(new JLabel("Residents for Room: " + roomNumber, SwingConstants.LEFT), BorderLayout.NORTH);
        popupPanel.add(createScrollPane(detailTable), BorderLayout.CENTER);
        
        JOptionPane.showMessageDialog(this, popupPanel, "Room Occupancy Details", JOptionPane.PLAIN_MESSAGE);
    }

    // Builds Bed Allocation screen using GridBagLayout and cascading dropdowns.
    // Resolves available beds dynamically based on selected student parameters.
    private void showAllocation() {
        JPanel card = createCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(680, 610));
        GridBagConstraints c = formConstraints();
        
        c.gridy = 0;
        card.add(heading("Manual Bed Allocation"), c);

        c.gridy = 1;
        c.insets = new Insets(16, 18, 5, 18);
        card.add(new JLabel("Select Student"), c);
        
        final JComboBox<Student> studentBox = new JComboBox<Student>();
        for (Student student : manager.getStudents()) studentBox.addItem(student);
        c.gridy = 2;
        c.insets = new Insets(3, 18, 8, 18);
        card.add(studentBox, c);

        c.gridy = 3;
        c.insets = new Insets(8, 18, 5, 18);
        card.add(new JLabel("Available Hostel"), c);
        
        final JComboBox<String> hostelBox = new JComboBox<String>();
        c.gridy = 4;
        c.insets = new Insets(3, 18, 8, 18);
        card.add(hostelBox, c);

        c.gridy = 5;
        c.insets = new Insets(8, 18, 5, 18);
        card.add(new JLabel("Available Block"), c);
        
        final JComboBox<String> blockBox = new JComboBox<String>();
        c.gridy = 6;
        c.insets = new Insets(3, 18, 8, 18);
        card.add(blockBox, c);

        c.gridy = 7;
        c.insets = new Insets(8, 18, 5, 18);
        card.add(new JLabel("Available Room"), c);
        
        final JComboBox<Room> roomBox = new JComboBox<Room>();
        c.gridy = 8;
        c.insets = new Insets(3, 18, 8, 18);
        card.add(roomBox, c);

        c.gridy = 9;
        c.insets = new Insets(8, 18, 5, 18);
        card.add(new JLabel("Available Bed"), c);
        
        final JComboBox<String> bedBox = new JComboBox<String>();
        c.gridy = 10;
        c.insets = new Insets(3, 18, 8, 18);
        card.add(bedBox, c);

        final JLabel currentBedLabel = muted("Current bed: -");
        c.gridy = 11;
        c.insets = new Insets(8, 18, 3, 18);
        card.add(currentBedLabel, c);
        
        final JLabel status = muted("Choose a student to view available beds.");
        c.gridy = 12;
        c.insets = new Insets(8, 18, 8, 18);
        card.add(status, c);
        
        ActionListener studentChanged = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Student student = (Student) studentBox.getSelectedItem();
                currentBedLabel.setText("Current bed: " + (manager.hasRoom(student) ? student.getRoomNumber() : "Not allocated / waiting"));
                refreshCascade(student, hostelBox, blockBox, roomBox, bedBox, status, 1);
            }
        };
        ActionListener hostelChanged = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                refreshCascade((Student) studentBox.getSelectedItem(), hostelBox, blockBox, roomBox, bedBox, status, 2);
            }
        };
        ActionListener blockChanged = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                refreshCascade((Student) studentBox.getSelectedItem(), hostelBox, blockBox, roomBox, bedBox, status, 3);
            }
        };
        ActionListener roomChanged = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                refreshCascade((Student) studentBox.getSelectedItem(), hostelBox, blockBox, roomBox, bedBox, status, 4);
            }
        };
        ActionListener bedChanged = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                refreshCascade((Student) studentBox.getSelectedItem(), hostelBox, blockBox, roomBox, bedBox, status, 5);
            }
        };

        studentBox.addActionListener(studentChanged);
        hostelBox.addActionListener(hostelChanged);
        blockBox.addActionListener(blockChanged);
        roomBox.addActionListener(roomChanged);
        bedBox.addActionListener(bedChanged);
        
        studentChanged.actionPerformed(null);

        JButton allocateButton = primaryButton("Allocate / Change Bed");
        c.gridy = 13;
        c.insets = new Insets(18, 18, 8, 18);
        card.add(allocateButton, c);
        
        allocateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Student student = (Student) studentBox.getSelectedItem();
                Room room = (Room) roomBox.getSelectedItem();
                String bedLabel = String.valueOf(bedBox.getSelectedItem());
                showInfo(manager.allocateBed(student, room, bedLabel));
                showAllocation();
            }
        });
        
        setupCenteredPage("Allocate Room", "Select hostel, block, room, and bed manually", card);
    }

    // Renders the first-in, first-out (FIFO) queued students table.
    private void showWaitingList() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        
        DefaultTableModel model = readOnlyModel(new String[]{"Queue Position", "Student ID", "Name",
                "Gender", "Department", "Year", "Required Hostel"});
        
        int position = 1;
        for (String studentId : manager.getWaitingQueue()) {
            Student student = manager.findStudent(studentId);
            if (student != null) {
                String hostel = student.getGender().equalsIgnoreCase("Female") ? "Girls" : "Boys";
                model.addRow(new Object[]{position, student.getStudentId(), student.getName(),
                        student.getGender(), student.getDepartment(), student.getYear(), hostel});
                position++;
            }
        }
        
        JTable table = new JTable(model);
        card.add(styleAndWrapTable(table), BorderLayout.CENTER);
        setupBasePage("Waiting List", "Students are allocated in first-in, first-out order", card);
    }

    // Renders Checkout screen using GridBagLayout to release a resident student's bed.
    // Automatically triggers matching allocations for waiting queue students.
    private void showCheckout() {
        JPanel card = createCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(560, 320));
        GridBagConstraints c = formConstraints();
        
        c.gridy = 0;
        card.add(heading("Checkout Details"), c);
        
        c.gridy = 1;
        c.insets = new Insets(22, 18, 5, 18);
        card.add(new JLabel("Select Resident Student"), c);
        
        final JComboBox<Student> studentBox = new JComboBox<Student>();
        for (Student student : manager.getStudents()) {
            if (manager.hasRoom(student)) studentBox.addItem(student);
        }
        c.gridy = 2;
        c.insets = new Insets(5, 18, 12, 18);
        card.add(studentBox, c);
        
        final JLabel roomLabel = muted("Allocated room: -");
        c.gridy = 3;
        c.insets = new Insets(8, 18, 8, 18);
        card.add(roomLabel, c);
        
        ActionListener updateRoom = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Student student = (Student) studentBox.getSelectedItem();
                roomLabel.setText(student == null ? "Allocated room: -"
                        : "Allocated room: " + student.getRoomNumber());
            }
        };
        studentBox.addActionListener(updateRoom);
        updateRoom.actionPerformed(null);
        
        JButton checkoutButton = primaryButton("Complete Checkout");
        c.gridy = 4;
        c.insets = new Insets(22, 18, 8, 18);
        card.add(checkoutButton, c);
        
        checkoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Student student = (Student) studentBox.getSelectedItem();
                if (student == null) {
                    showWarning("There is no resident student to check out.");
                    return;
                }
                if (askConfirm("Check out " + student.getName() + " from room " + student.getRoomNumber() + "?", "Confirm Checkout")) {
                    showInfo(manager.checkoutStudent(student));
                    showCheckout();
                }
            }
        });
        
        setupCenteredPage("Student Checkout", "Release a room and process the next waiting student", card);
    }

    // Renders search screen toolbar and filters results using database query.
    private void showSearch() {
        JPanel card = borderCard(16);
        
        final JTextField searchField = new JTextField();
        styleInput(searchField);
        JButton searchButton = primaryButton("Search Records");
        
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);
        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchButton, BorderLayout.EAST);
        card.add(searchBar, BorderLayout.NORTH);
        
        final DefaultTableModel model = readOnlyModel(new String[]{"Student ID", "Name", "Gender", "Department",
                "Year", "Phone", "Bed", "Hostel", "Block"});
        JTable table = new JTable(model);
        fillSearchTable(model, "");
        
        table.getColumnModel().getColumn(0).setPreferredWidth(90);  // Student ID
        table.getColumnModel().getColumn(1).setPreferredWidth(130); // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(70);  // Gender
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Department
        table.getColumnModel().getColumn(4).setPreferredWidth(50);  // Year
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Phone
        table.getColumnModel().getColumn(6).setPreferredWidth(180); // Bed (Hostel/Bed details)
        table.getColumnModel().getColumn(7).setPreferredWidth(90);  // Hostel
        table.getColumnModel().getColumn(8).setPreferredWidth(60);  // Block
        
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) { fillSearchTable(model, searchField.getText()); }
        });
        
        card.add(styleAndWrapTable(table), BorderLayout.CENTER);
        setupBasePage("Search", "Find a student using ID, name, or room number", card);
    }

    // Displays academic metadata and project specifications.
    private void showAbout() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(650, 470));
        
        JLabel title = heading("CampusStay");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(BLUE);
        title.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel version = muted("Hostel Room Allocation System  •  Version 1.0");
        version.setAlignmentX(CENTER_ALIGNMENT);
        
        card.add(Box.createVerticalGlue());
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(version);
        card.add(Box.createVerticalStrut(30));
        card.add(infoLine("Technology", "Core Java, Swing, AWT and Java IO"));
        card.add(Box.createVerticalStrut(12));
        card.add(infoLine("Storage", "Local serialized .dat files"));
        card.add(Box.createVerticalStrut(12));
        card.add(infoLine("Purpose", "Second-year B.Tech college project"));
        card.add(Box.createVerticalStrut(28));
        
        JLabel note = new JLabel("Designed for simple, reliable hostel administration.");
        note.setForeground(TEXT);
        note.setAlignmentX(CENTER_ALIGNMENT);
        
        card.add(note);
        card.add(Box.createVerticalGlue());
        setupCenteredPage("About", "Project information", card);
    }

    // Builds header banner panel with title and subtitle labels.
    private JPanel createScreen(String titleText, String subtitleText) {
        JPanel screen = new JPanel(new BorderLayout(0, 20));
        screen.setBackground(BACKGROUND);
        screen.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel(titleText);
        title.setFont(HEADING_FONT);
        title.setForeground(TEXT);
        
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setForeground(MUTED);
        
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        screen.add(header, BorderLayout.NORTH);
        return screen;
    }

    // Creates reusable rounded white container panel using custom painting.
    private JPanel createCard() {
        RoundedPanel panel = new RoundedPanel();
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    // Creates rounded Blue primary action button.
    private JButton primaryButton(String text) {
        RoundedButton button = new RoundedButton(text, BLUE);
        button.setForeground(Color.WHITE);
        return button;
    }

    // Creates rounded Grey secondary control button.
    private JButton secondaryButton(String text) {
        RoundedButton button = new RoundedButton(text, new Color(226, 232, 240));
        button.setForeground(TEXT);
        return button;
    }

    // Applies uniform border, padding, and font styles to input text fields.
    private void styleInput(JTextField field) {
        field.setFont(NORMAL_FONT);
        field.setPreferredSize(new Dimension(0, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 10, 7, 10)));
    }

    // Applies corporate styling constraints (font, heights, cell margins) to JTable components.
    private void styleTable(JTable table) {
        table.setFont(NORMAL_FONT);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setBackground(new Color(239, 244, 250));
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        table.setDefaultRenderer(Object.class, renderer);
    }

    // Wraps a JTable into JScrollPane to support layout scrollbars.
    private JScrollPane createScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        return scrollPane;
    }

    // Unifies creation and display of standard dashboard page views.
    private void setupBasePage(String title, String subtitle, JPanel card) {
        JPanel screen = createScreen(title, subtitle);
        screen.add(card, BorderLayout.CENTER);
        setScreen(screen);
    }

    // Unifies creation and display of centered modal-like screen views.
    private void setupCenteredPage(String title, String subtitle, JPanel card) {
        JPanel screen = createScreen(title, subtitle);
        JPanel wrapper = centeredWrapper();
        wrapper.add(card);
        screen.add(wrapper, BorderLayout.CENTER);
        setScreen(screen);
    }

    // Triggers cascading database dropdown updates on choice modifications.
    // Syncs choices (Hostel -> Block -> Room -> Bed) using dynamic database parameters.
    private void refreshCascade(Student student, JComboBox<String> hostelBox, JComboBox<String> blockBox,
                                JComboBox<Room> roomBox, JComboBox<String> bedBox, JLabel status, int level) {
        if (level <= 1) {
            fillAvailableHostelBox(hostelBox, student);
        }
        if (level <= 2) {
            fillAvailableBlockBox(blockBox, student, String.valueOf(hostelBox.getSelectedItem()));
        }
        if (level <= 3) {
            fillAvailableRoomBox(roomBox, student, String.valueOf(hostelBox.getSelectedItem()), String.valueOf(blockBox.getSelectedItem()));
        }
        if (level <= 4) {
            fillAvailableBedBox(bedBox, student, (Room) roomBox.getSelectedItem());
        }
        updateAllocationStatus(status, student, hostelBox, blockBox, roomBox, bedBox);
    }

    // Shared helper to build two-column GridBag layout forms.
    private JPanel createGridForm(String[] labels, Component[] fields) {
        JPanel form = new JPanel(new GridBagLayout());
        for (int i = 0; i < labels.length; i++) {
            addFormRow(form, i, labels[i], fields[i]);
        }
        return form;
    }

    // Combines table styling and scroll pane wrapping into one step.
    private JScrollPane styleAndWrapTable(JTable table) {
        styleTable(table);
        return createScrollPane(table);
    }

    // Instantiates table model disabling cell double-click editing.
    private DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    // Populates available hostels compatible with student gender constraints.
    private void fillAvailableHostelBox(JComboBox<String> hostelBox, Student student) {
        hostelBox.removeAllItems();
        if (student == null) return;
        for (Room room : manager.getRooms()) {
            if (manager.canRoomAcceptStudentForManualAllocation(student, room)) {
                String hostelName = getHostelName(room);
                if (!comboHasItem(hostelBox, hostelName)) hostelBox.addItem(hostelName);
            }
        }
    }

    // Populates compatible blocks for the chosen hostel.
    private void fillAvailableBlockBox(JComboBox<String> blockBox, Student student, String hostelName) {
        blockBox.removeAllItems();
        if (student == null || hostelName == null || hostelName.equals("null")) return;
        for (Room room : manager.getRooms()) {
            if (manager.canRoomAcceptStudentForManualAllocation(student, room)
                    && getHostelName(room).equals(hostelName)) {
                if (!comboHasItem(blockBox, room.getBlock())) blockBox.addItem(room.getBlock());
            }
        }
    }

    // Populates compatible rooms based on hostel and block.
    private void fillAvailableRoomBox(JComboBox<Room> roomBox, Student student, String hostelName, String block) {
        roomBox.removeAllItems();
        if (student == null || hostelName == null || hostelName.equals("null")
                || block == null || block.equals("null")) return;
        for (Room room : manager.getRooms()) {
            if (manager.canRoomAcceptStudentForManualAllocation(student, room)
                    && getHostelName(room).equals(hostelName)
                    && room.getBlock().equalsIgnoreCase(block)) {
                roomBox.addItem(room);
            }
        }
    }

    // Populates unoccupied beds inside the selected room.
    private void fillAvailableBedBox(JComboBox<String> bedBox, Student student, Room room) {
        bedBox.removeAllItems();
        if (student == null || room == null) return;
        for (int i = 0; i < room.getCapacity(); i++) {
            String bedLabel = "BED-" + (char)('A' + i);
            if (manager.canBedAcceptStudentForManualAllocation(student, room, bedLabel)) {
                bedBox.addItem(bedLabel);
            }
        }
    }

    // Updates allocation status label to show selection readiness.
    private void updateAllocationStatus(JLabel status, Student student, JComboBox<String> hostelBox,
                                        JComboBox<String> blockBox, JComboBox<Room> roomBox,
                                        JComboBox<String> bedBox) {
        if (student == null) {
            status.setText("No student record is available.");
        } else if (hostelBox.getItemCount() == 0) {
            status.setText("No compatible hostel has a free bed for this " + student.getGender() + " student.");
        } else if (blockBox.getItemCount() == 0) {
            status.setText("No available block in the selected hostel.");
        } else if (roomBox.getItemCount() == 0) {
            status.setText("No available room in the selected block.");
        } else if (bedBox.getItemCount() == 0) {
            status.setText("No available bed in the selected room.");
        } else {
            Room room = (Room) roomBox.getSelectedItem();
            String bed = String.valueOf(bedBox.getSelectedItem());
            status.setText("Ready to allocate: " + room.getRoomNumber() + "-" + bed);
        }
    }

    // Extracts structured hostel prefix name from room ID string (e.g. GH-01).
    private String getHostelName(Room room) {
        String roomNumber = room.getRoomNumber();
        String[] parts = roomNumber.split("-");
        if (parts.length >= 2) return parts[0] + "-" + parts[1];
        return room.getHostelType();
    }

    // Checks if combobox already contains a selection value query.
    private boolean comboHasItem(JComboBox<String> comboBox, String value) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (String.valueOf(comboBox.getItemAt(i)).equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    // Populates Student table model from records.
    private void fillStudentTable(DefaultTableModel model, ArrayList<Student> students) {
        model.setRowCount(0);
        for (Student student : students) {
            String room = manager.hasRoom(student) ? student.getRoomNumber() : "Not allocated";
            model.addRow(new Object[]{student.getStudentId(), student.getName(), student.getGender(),
                    student.getDepartment(), student.getYear(), student.getPhoneNumber(), room});
        }
    }

    // Populates Search table model matching text query.
    private void fillSearchTable(DefaultTableModel model, String text) {
        model.setRowCount(0);
        for (Student student : manager.searchStudents(text)) {
            String roomNumber = manager.hasRoom(student) ? student.getRoomNumber() : "Not allocated";
            Room room = manager.findRoom(student.getRoomNumber());
            model.addRow(new Object[]{student.getStudentId(), student.getName(), student.getGender(),
                    student.getDepartment(), student.getYear(), student.getPhoneNumber(), roomNumber,
                    room == null ? "-" : room.getHostelType(), room == null ? "-" : room.getBlock()});
        }
    }

    // Maps JTable row index back to underlying Student model object.
    private Student selectedStudent(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int modelRow = table.convertRowIndexToModel(row);
        return manager.findStudent(String.valueOf(model.getValueAt(modelRow, 0)));
    }

    // Shared GridBagConstraints configuration to add two-column form rows.
    private void addFormRow(JPanel form, int row, String label, Component field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(5, 8, 5, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 0.35;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.weightx = 0.65;
        field.setPreferredSize(new Dimension(230, 34));
        form.add(field, c);
    }

    // Returns template GridBagConstraints configuration for dialog layouts.
    private GridBagConstraints formConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(8, 18, 8, 18);
        return c;
    }

    // Creates transparent GridBagLayout panel wrapper to center contents.
    private JPanel centeredWrapper() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        return panel;
    }

    // Renders styled two-column horizontal row representing metadata values.
    private JPanel infoLine(String labelText, String valueText) {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(500, 30));
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel value = new JLabel(valueText);
        value.setForeground(MUTED);
        panel.add(label);
        panel.add(value);
        return panel;
    }

    // Triggers warning message if no table selection is active.
    private void showSelectMessage(String item) {
        showWarning("Please select a " + item + " from the table.");
    }

    // Shared dialog wrapper around JOptionPane.
    private void showMessage(String message, String title, int type) {
        JOptionPane.showMessageDialog(this, message, title, type);
    }

    // Display dialog boxes using the shared wrapper.
    private void showInfo(String message) {
        showMessage(message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        showMessage(message, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message, String title) {
        showMessage(message, title, JOptionPane.ERROR_MESSAGE);
    }

    // Shared confirmation dialog helper returning user selection.
    private boolean askConfirm(String message, String title) {
        int answer = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
        return answer == JOptionPane.YES_OPTION;
    }

    // Reusable BorderLayout card container.
    private JPanel borderCard(int gap) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, gap));
        return card;
    }

    // Creates transparent FlowLayout panel.
    private JPanel flowPanel(int align, Component... components) {
        JPanel panel = new JPanel(new FlowLayout(align, 8, 0));
        panel.setOpaque(false);
        for (Component c : components) panel.add(c);
        return panel;
    }

    // Creates bold heading label.
    private JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 21));
        label.setForeground(TEXT);
        return label;
    }

    // Creates muted gray description label.
    private JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    // Overrides paintComponent to enable smooth, anti-aliased rounded borders.
    private static class RoundedPanel extends JPanel {
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(Color.WHITE);
            copy.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            copy.dispose();
            super.paintComponent(graphics);
        }

        RoundedPanel() { setOpaque(false); }
    }

    // Overrides paintComponent to render styled rounded edges.
    private static class RoundedButton extends JButton {
        private Color buttonColor;

        RoundedButton(String text, Color buttonColor) {
            super(text);
            this.buttonColor = buttonColor;
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setBorder(new EmptyBorder(10, 18, 10, 18));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.setColor(buttonColor);
            copy.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            copy.dispose();
            super.paintComponent(graphics);
        }
    }
}
