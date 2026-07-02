package hostel;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            // Java's default look and feel is used if the system theme is unavailable.
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                HostelManager manager = new HostelManager();
                new MainFrame(manager).setVisible(true);
            }
        });
    }
}
