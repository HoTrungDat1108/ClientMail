/*
 * Mail Client - hMailServer POP3
 * Entry point của ứng dụng
 */
package client;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * @author htd11
 */
public class Client {

    public static void main(String[] args) {
        // Dùng system look and feel cho đẹp hơn
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Dùng mặc định nếu lỗi
        }

        // Chạy trên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
