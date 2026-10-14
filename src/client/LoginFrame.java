/*
 * LoginFrame.java - Form đăng nhập kết nối hMailServer qua POP3
 */
package client;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Giao diện đăng nhập - nhập email và mật khẩu
 * @author htd11
 */
public class LoginFrame extends JFrame {

    // ===== Màu sắc chủ đạo =====
    private static final Color BG_DARK       = new Color(18, 18, 30);
    private static final Color BG_CARD       = new Color(28, 28, 45);
    private static final Color ACCENT_BLUE   = new Color(99, 102, 241);
    private static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    private static final Color TEXT_WHITE    = new Color(240, 240, 255);
    private static final Color TEXT_GRAY     = new Color(148, 163, 184);
    private static final Color FIELD_BG      = new Color(38, 38, 60);
    private static final Color BORDER_COLOR  = new Color(55, 55, 80);
    private static final Color ERROR_COLOR   = new Color(239, 68, 68);

    // ===== Components =====
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JTextField txtServer;
    private JTextField txtPort;
    private JButton btnLogin;
    private JLabel lblStatus;
    private JProgressBar progressBar;

    public LoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Mail Client - Đăng Nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true); // Bỏ thanh title mặc định

        // Panel nền gradient
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient nền
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK, getWidth(), getHeight(), new Color(25, 20, 50));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new LineBorder(new Color(60, 60, 100), 1, true));

        // ===== Header =====
        JPanel headerPanel = createHeaderPanel();

        // ===== Form Panel =====
        JPanel formPanel = createFormPanel();

        // ===== Footer (nút đóng) =====
        JPanel footerPanel = createFooterPanel();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

        // Cho phép kéo cửa sổ
        addWindowDragListener(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient header
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE, getWidth(), getHeight(), ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Hiệu ứng vòng tròn mờ
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                g2.setColor(Color.WHITE);
                g2.fillOval(-30, -60, 200, 200);
                g2.fillOval(getWidth() - 100, -20, 180, 180);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(480, 150));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Icon mail
        JLabel iconLabel = new JLabel("✉");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setForeground(Color.WHITE);
        panel.add(iconLabel, gbc);

        // Tiêu đề
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 2, 0);
        JLabel titleLabel = new JLabel("Mail Client");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, gbc);

        // Subtitle
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel subLabel = new JLabel("Đăng nhập vào tài khoản email của bạn");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(200, 210, 255));
        panel.add(subLabel, gbc);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 40, 10, 40));

        // ===== Email =====
        panel.add(createFieldLabel("Email"));
        panel.add(Box.createVerticalStrut(6));
        txtEmail = createTextField("user@domain.local", false);
        panel.add(txtEmail);

        panel.add(Box.createVerticalStrut(16));

        // ===== Mật khẩu =====
        panel.add(createFieldLabel("Mật khẩu"));
        panel.add(Box.createVerticalStrut(6));
        txtPassword = (JPasswordField) createTextField("••••••••", true);
        panel.add(txtPassword);

        panel.add(Box.createVerticalStrut(16));

        // ===== Cài đặt server (collapsible row) =====
        JPanel serverRow = new JPanel(new GridLayout(1, 2, 10, 0));
        serverRow.setOpaque(false);
        serverRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel serverCol = new JPanel();
        serverCol.setOpaque(false);
        serverCol.setLayout(new BoxLayout(serverCol, BoxLayout.Y_AXIS));
        serverCol.add(createFieldLabel("POP3 Server"));
        serverCol.add(Box.createVerticalStrut(6));
        txtServer = (JTextField) createTextField("localhost", false);
        serverCol.add(txtServer);

        JPanel portCol = new JPanel();
        portCol.setOpaque(false);
        portCol.setLayout(new BoxLayout(portCol, BoxLayout.Y_AXIS));
        portCol.add(createFieldLabel("Port"));
        portCol.add(Box.createVerticalStrut(6));
        txtPort = (JTextField) createTextField("110", false);
        portCol.add(txtPort);

        serverRow.add(serverCol);
        serverRow.add(portCol);
        panel.add(serverRow);

        panel.add(Box.createVerticalStrut(22));

        // ===== Nút đăng nhập =====
        btnLogin = createLoginButton();
        panel.add(btnLogin);

        panel.add(Box.createVerticalStrut(14));

        // ===== Status / loading =====
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        progressBar.setForeground(ACCENT_BLUE);
        progressBar.setBackground(BORDER_COLOR);
        progressBar.setBorderPainted(false);
        panel.add(progressBar);

        panel.add(Box.createVerticalStrut(8));

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(TEXT_GRAY);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblStatus);

        outer.add(panel, BorderLayout.CENTER);
        return outer;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 12, 16));

        JButton btnClose = new JButton("✕  Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.setForeground(TEXT_GRAY);
        btnClose.setBackground(new Color(50, 50, 70));
        btnClose.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> System.exit(0));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(ERROR_COLOR); }
            public void mouseExited(MouseEvent e)  { btnClose.setForeground(TEXT_GRAY); }
        });
        panel.add(btnClose);
        return panel;
    }

    // ===== Helper: tạo label =====
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // ===== Helper: tạo text field =====
    private JTextField createTextField(String placeholder, boolean isPassword) {
        JTextField field = isPassword ? new JPasswordField(20) : new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_WHITE);
        field.setBackground(FIELD_BG);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Placeholder
        if (!isPassword) {
            field.setText(placeholder);
            field.setForeground(TEXT_GRAY);
            field.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (field.getText().equals(placeholder)) {
                        field.setText("");
                        field.setForeground(TEXT_WHITE);
                    }
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACCENT_BLUE, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                    ));
                }
                public void focusLost(FocusEvent e) {
                    if (field.getText().isEmpty()) {
                        field.setText(placeholder);
                        field.setForeground(TEXT_GRAY);
                    }
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_COLOR, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                    ));
                }
            });
        } else {
            ((JPasswordField) field).setEchoChar((char) 0);
            field.setText(placeholder);
            field.setForeground(TEXT_GRAY);
            field.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (String.valueOf(((JPasswordField) field).getPassword()).equals(placeholder)) {
                        field.setText("");
                        field.setForeground(TEXT_WHITE);
                        ((JPasswordField) field).setEchoChar('•');
                    }
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(ACCENT_BLUE, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                    ));
                }
                public void focusLost(FocusEvent e) {
                    if (String.valueOf(((JPasswordField) field).getPassword()).isEmpty()) {
                        ((JPasswordField) field).setEchoChar((char) 0);
                        field.setText(placeholder);
                        field.setForeground(TEXT_GRAY);
                    }
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(BORDER_COLOR, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                    ));
                }
            });
        }
        return field;
    }

    // ===== Helper: nút đăng nhập gradient =====
    private JButton createLoginButton() {
        JButton btn = new JButton("  Đăng Nhập") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp;
                if (getModel().isPressed()) {
                    gp = new GradientPaint(0, 0, ACCENT_PURPLE, getWidth(), 0, ACCENT_BLUE);
                } else if (getModel().isRollover()) {
                    gp = new GradientPaint(0, 0, new Color(120, 123, 255), getWidth(), 0, new Color(160, 110, 255));
                } else {
                    gp = new GradientPaint(0, 0, ACCENT_BLUE, getWidth(), 0, ACCENT_PURPLE);
                }
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> doLogin());

        // Enter để login
        getRootPane().setDefaultButton(btn);
        return btn;
    }

    // ===== Xử lý đăng nhập =====
    private void doLogin() {
        String email    = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String server   = txtServer.getText().trim();
        String portStr  = txtPort.getText().trim();

        // Validation
        if (email.isEmpty() || email.equals("user@domain.local")) {
            showError("Vui lòng nhập địa chỉ email!");
            txtEmail.requestFocus();
            return;
        }
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            showError("Email phải có dạng: tenuser@domain (vd: htd11082005@deathheart)");
            txtEmail.requestFocus();
            return;
        }
        if (password.isEmpty() || password.equals("••••••••")) {
            showError("Vui lòng nhập mật khẩu!");
            txtPassword.requestFocus();
            return;
        }
        if (server.isEmpty()) {
            showError("Vui lòng nhập địa chỉ server!");
            txtServer.requestFocus();
            return;
        }

        int port = 110;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            showError("Port không hợp lệ!");
            return;
        }

        final int finalPort = port;
        final String finalServer = server;
        final String finalEmail = email;
        final String finalPassword = password;

        // Hiện loading
        setLoading(true, "Đang kết nối tới " + server + "...");

        // Kết nối trong background thread
        new Thread(() -> {
            try {
                POP3Client pop3 = new POP3Client(finalServer, finalPort, finalEmail, finalPassword);
                java.util.List<MailMessage> messages = pop3.fetchMessages();

                SwingUtilities.invokeLater(() -> {
                    setLoading(false, "");
                    // Mở màn hình inbox
                    InboxFrame inbox = new InboxFrame(finalEmail, messages, pop3);
                    inbox.setVisible(true);
                    dispose(); // Đóng login
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setLoading(false, "");
                    showError("Lỗi kết nối: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void setLoading(boolean loading, String message) {
        btnLogin.setEnabled(!loading);
        progressBar.setVisible(loading);
        progressBar.setIndeterminate(loading);
        lblStatus.setForeground(TEXT_GRAY);
        lblStatus.setText(loading ? message : " ");
    }

    private void showError(String msg) {
        lblStatus.setForeground(ERROR_COLOR);
        lblStatus.setText("⚠  " + msg);
    }

    // ===== Cho phép kéo cửa sổ =====
    private int startX, startY;
    private void addWindowDragListener(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
            }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getX() + e.getX() - startX, getY() + e.getY() - startY);
            }
        });
    }
}
