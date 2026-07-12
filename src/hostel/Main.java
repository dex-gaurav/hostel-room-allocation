package hostel;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main is the entry point class for the Hostel Room Allocation System (CampusStay).
 * It initializes the application, applies the system-native look and feel theme,
 * and launches the GUI on the Event Dispatch Thread (EDT).
 */
public class Main {
    
    /**
     * The main method executes when the program starts.
     * It sets up the user interface theme and instantiates the main application frame.
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Attempt to apply the system's native look and feel to make the GUI match the OS style.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            // If the system theme is unavailable, Java falls back to its default look and feel (Metal/Nimbus).
        }

        // Run the GUI creation code on the Event Dispatch Thread (EDT) to ensure thread-safety in Swing.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Initialize the central business logic controller.
                HostelManager manager = new HostelManager();
                // Create and display the main application window.
                new MainFrame(manager).setVisible(true);
            }
        });
    }
}

