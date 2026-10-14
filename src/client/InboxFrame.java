/*
 * InboxFrame.java - Giao diện hộp thư đến sau khi đăng nhập
 */
package client;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Màn hình Inbox hiển thị danh sách email lấy từ POP3
 * @author htd11
 */
public class InboxFrame extends JFrame {

    // ===== Màu sắc =====
    private static final Color BG_DARK      = new Color(15, 15, 25);
    private static final Color BG_SIDEBAR   = new Color(22, 22, 38);
    private static final Color BG_LIST      = new Color(18, 18, 32);
    private static final Color BG_CONTENT   = new Color(24, 24, 40);
    private static final Color BG_HOVER     = new Color(35, 35, 60);
    private static final Color BG_SELECTED  = new Color(45, 45, 80);
    private static final Color ACCENT_BLUE  = new Color(99, 102, 241);
    private static final Color ACCENT_PURPLE= new Color(139, 92, 246);
    private static final Color TEXT_WHITE   = new Color(240, 240, 255);
    private static final Color TEXT_GRAY    = new Color(148, 163, 184);
    private static final Color TEXT_DIMMED  = new Color(90, 100, 130);
    private static final Color BORDER_COLOR = new Color(35, 35, 58);
    private static final Color UNREAD_DOT   = new Color(99, 102, 241);

    private final String         userEmail;
    private final List<MailMessage> messages;
    private final POP3Client     pop3;

    // ===== Components =====
    private JPanel    mailListPanel;
    private JTextArea txtBody;
    private JLabel    lblSubject;
    private JLabel    lblFrom;
    private JLabel    lblDate;
    private JLabel    lblMailCount;
    private JPanel    selectedCard = null;

    // ===== Navigation =====
    private JPanel        centerWrapper;   // wrapper chứa list + content
    private JSplitPane    splitRight;
    private String        currentView = "inbox";
    private JPanel        activeSidebarItem = null;
    private java.util.List<JPanel> sidebarItems = new java.util.ArrayList<>();

    public InboxFrame(String userEmail, List<MailMessage> messages, POP3Client pop3) {
        this.userEmail = userEmail;
        this.messages  = messages;
        this.pop3      = pop3;
        initUI();
    }

    private void initUI() {
        setTitle("Mail Client - Hộp Thư Đến");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);

        // ===== SIDEBAR trái =====
        JPanel sidebar = buildSidebar();

        // ===== DANH SÁCH MAIL (giữa) =====
        JPanel listPanel = buildMailList();

        // ===== NỘI DUNG MAIL (phải) =====
        JPanel contentPanel = buildContentPanel();

        // JSplitPane để kéo thay đổi kích thước
        splitRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, contentPanel);
        splitRight.setDividerLocation(360);
        splitRight.setDividerSize(1);
        splitRight.setBorder(null);
        splitRight.setBackground(BORDER_COLOR);

        // Wrapper để swap view
        centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(splitRight, BorderLayout.CENTER);

        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, centerWrapper);
        splitMain.setDividerLocation(200);
        splitMain.setDividerSize(1);
        splitMain.setBorder(null);
        splitMain.setBackground(BORDER_COLOR);

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(splitMain, BorderLayout.CENTER);

        setContentPane(root);

        // Hiển thị mail đầu tiên nếu có
        if (!messages.isEmpty()) {
            showMailContent(messages.get(0));
        }
    }

    // ===== TOP BAR =====
    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE, getWidth(), 0, ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(0, 52));
        panel.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Trái: Logo + tiêu đề
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("✉");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        icon.setForeground(Color.WHITE);
        JLabel title = new JLabel("Mail Client");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        left.add(icon);
        left.add(title);

        // Phải: Email đang đăng nhập + nút logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        JLabel lblUser = new JLabel("● " + userEmail);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(new Color(200, 210, 255));

        JButton btnLogout = new JButton("Dang xuat") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(220, 50, 50, 200));
                } else {
                    g2.setColor(new Color(255, 255, 255, 60));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        try {
            btnLogout.setText("\u0110\u0103ng xu\u1ea5t");
        } catch (Exception ignored) { btnLogout.setText("Dang xuat"); }
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setOpaque(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 1, true),
            new EmptyBorder(5, 14, 5, 14)
        ));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            pop3.disconnect();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        });

        right.add(lblUser);
        right.add(btnLogout);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // ===== SIDEBAR =====
    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_SIDEBAR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_COLOR));

        panel.add(Box.createVerticalStrut(20));

        // Avatar
        JLabel avatar = new JLabel(String.valueOf(userEmail.charAt(0)).toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE, getWidth(), getHeight(), ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 22));
        avatar.setForeground(Color.WHITE);
        avatar.setPreferredSize(new Dimension(56, 56));
        avatar.setMaximumSize(new Dimension(56, 56));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(avatar);

        panel.add(Box.createVerticalStrut(10));

        // Tên ngắn
        String shortName = userEmail.contains("@") ? userEmail.split("@")[0] : userEmail;
        JLabel lblName = new JLabel(shortName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(TEXT_WHITE);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblName);

        panel.add(Box.createVerticalStrut(4));

        lblMailCount = new JLabel(messages.size() + " thư");
        lblMailCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMailCount.setForeground(TEXT_GRAY);
        lblMailCount.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblMailCount);

        panel.add(Box.createVerticalStrut(24));

        // Menu items
        JPanel itemInbox = createSidebarItem("inbox",  "[Hộp thư đến]", true);
        JPanel itemSent  = createSidebarItem("sent",   "[Đã gửi]",      false);
        JPanel itemDraft = createSidebarItem("draft",  "[Nháp]",        false);
        JPanel itemTrash = createSidebarItem("trash",  "[Thùng rác]",   false);
        sidebarItems.add(itemInbox);
        sidebarItems.add(itemSent);
        sidebarItems.add(itemDraft);
        sidebarItems.add(itemTrash);
        activeSidebarItem = itemInbox;
        panel.add(itemInbox);
        panel.add(itemSent);
        panel.add(itemDraft);
        panel.add(itemTrash);

        panel.add(Box.createVerticalGlue());

        // Thông tin server
        JLabel lblServer = new JLabel(pop3.getHost());
        lblServer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblServer.setForeground(TEXT_DIMMED);
        lblServer.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblServer);
        panel.add(Box.createVerticalStrut(16));

        return panel;
    }

    private JPanel createSidebarItem(String viewId, String text, boolean active) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        item.setBackground(active ? BG_SELECTED : BG_SIDEBAR);
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setBorder(active
            ? new MatteBorder(0, 3, 0, 0, ACCENT_BLUE)
            : new EmptyBorder(0, 3, 0, 0));

        // Dùng icon text thay vì emoji để tránh lỗi font
        String icon;
        String label;
        switch (viewId) {
            case "inbox":  icon = ">"; label = "Hộp thư đến"; break;
            case "sent":   icon = ">"; label = "Đã gửi";      break;
            case "draft":  icon = ">"; label = "Nháp";        break;
            case "trash":  icon = ">"; label = "Thùng rác";   break;
            default:       icon = ">"; label = text;          break;
        }

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(active ? TEXT_WHITE : TEXT_GRAY);
        item.add(lbl);
        // Lưu label vào item để update màu sau
        item.putClientProperty("label", lbl);
        item.putClientProperty("viewId", viewId);

        item.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (item != activeSidebarItem) item.setBackground(BG_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (item != activeSidebarItem) item.setBackground(BG_SIDEBAR);
            }
            public void mouseClicked(MouseEvent e) {
                setActiveSidebarItem(item);
                switchView(viewId);
            }
        });

        return item;
    }

    // ===== Cập nhật sidebar item active =====
    private void setActiveSidebarItem(JPanel item) {
        if (activeSidebarItem != null) {
            activeSidebarItem.setBackground(BG_SIDEBAR);
            activeSidebarItem.setBorder(new EmptyBorder(0, 3, 0, 0));
            JLabel lbl = (JLabel) activeSidebarItem.getClientProperty("label");
            if (lbl != null) lbl.setForeground(TEXT_GRAY);
        }
        activeSidebarItem = item;
        item.setBackground(BG_SELECTED);
        item.setBorder(new MatteBorder(0, 3, 0, 0, ACCENT_BLUE));
        JLabel lbl = (JLabel) item.getClientProperty("label");
        if (lbl != null) lbl.setForeground(TEXT_WHITE);
    }

    // ===== Chuyển view =====
    private void switchView(String viewId) {
        currentView = viewId;
        centerWrapper.removeAll();
        if ("inbox".equals(viewId)) {
            centerWrapper.add(splitRight, BorderLayout.CENTER);
        } else {
            centerWrapper.add(buildEmptyStatePanel(viewId), BorderLayout.CENTER);
        }
        centerWrapper.revalidate();
        centerWrapper.repaint();
    }

    // ===== Panel empty state cho các view chưa có dữ liệu =====
    private JPanel buildEmptyStatePanel(String viewId) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_DARK);

        JPanel card = new JPanel();
        card.setBackground(new Color(28, 28, 48));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 60, 40, 60));

        String emoji, title, desc;
        switch (viewId) {
            case "sent":
                emoji = "Đã Gửi"; title = "Chưa có thư đã gửi";
                desc  = "Các email bạn gửi sẽ xuất hiện ở đây.";
                break;
            case "draft":
                emoji = "Nháp"; title = "Không có thư nháp";
                desc  = "Các email bạn lưu nháp sẽ xuất hiện ở đây.";
                break;
            case "trash":
                emoji = "Thùng Rác"; title = "Thùng rác trống";
                desc  = "Các email đã xoá sẽ xuất hiện ở đây.";
                break;
            default:
                emoji = "?"; title = "Trống"; desc = "";
        }

        JLabel lblEmoji = new JLabel(emoji);
        lblEmoji.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblEmoji.setForeground(ACCENT_PURPLE);
        lblEmoji.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(TEXT_WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(TEXT_GRAY);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblEmoji);
        card.add(Box.createVerticalStrut(16));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(lblDesc);

        panel.add(card);
        return panel;
    }

    // ===== DANH SÁCH MAIL =====
    private JPanel buildMailList() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_LIST);

        // Header danh sách
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_LIST);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("Hộp thư đến");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_WHITE);

        JLabel count = new JLabel(messages.size() + " thư");
        count.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        count.setForeground(TEXT_GRAY);

        header.add(title, BorderLayout.WEST);
        header.add(count, BorderLayout.EAST);

        // Danh sách mail (scrollable)
        mailListPanel = new JPanel();
        mailListPanel.setLayout(new BoxLayout(mailListPanel, BoxLayout.Y_AXIS));
        mailListPanel.setBackground(BG_LIST);

        if (messages.isEmpty()) {
            JLabel empty = new JLabel("Hộp thư rỗng");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            empty.setForeground(TEXT_DIMMED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            mailListPanel.add(Box.createVerticalStrut(40));
            mailListPanel.add(empty);
        } else {
            for (MailMessage msg : messages) {
                mailListPanel.add(createMailCard(msg));
            }
        }

        JScrollPane scroll = new JScrollPane(mailListPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setBackground(BG_LIST);

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    // ===== Card một mail trong danh sách =====
    private JPanel createMailCard(MailMessage msg) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(BG_LIST);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        card.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(12, 16, 12, 16)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Cột trái: avatar chữ cái
        JLabel avatar = new JLabel(msg.getFromName().isEmpty() ? "?" :
                String.valueOf(msg.getFromName().charAt(0)).toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Màu random theo chữ cái
                int hash = getFromNameHash(msg.getFromName());
                Color[] colors = {
                    new Color(239,68,68), new Color(245,158,11), new Color(16,185,129),
                    new Color(59,130,246), new Color(139,92,246), new Color(236,72,153)
                };
                g2.setColor(colors[Math.abs(hash) % colors.length]);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatar.setForeground(Color.WHITE);
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);

        // Cột phải: thông tin
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        // Dòng 1: tên người gửi + ngày
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        JLabel lblFrom = new JLabel(msg.getFromName());
        lblFrom.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFrom.setForeground(msg.isRead() ? TEXT_GRAY : TEXT_WHITE);
        JLabel lblDate = new JLabel(formatDate(msg.getDate()));
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(TEXT_DIMMED);
        row1.add(lblFrom, BorderLayout.WEST);
        row1.add(lblDate, BorderLayout.EAST);

        // Dòng 2: tiêu đề
        JLabel lblSubj = new JLabel(msg.getSubject());
        lblSubj.setFont(new Font("Segoe UI", msg.isRead() ? Font.PLAIN : Font.BOLD, 12));
        lblSubj.setForeground(msg.isRead() ? TEXT_GRAY : TEXT_WHITE);

        // Dòng 3: preview
        JLabel lblPreview = new JLabel(msg.getPreview());
        lblPreview.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPreview.setForeground(TEXT_DIMMED);

        info.add(row1);
        info.add(Box.createVerticalStrut(2));
        info.add(lblSubj);
        info.add(Box.createVerticalStrut(2));
        info.add(lblPreview);

        card.add(avatar, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);

        // Hover & click
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (card != selectedCard) card.setBackground(BG_HOVER);
            }
            public void mouseExited(MouseEvent e) {
                if (card != selectedCard) card.setBackground(BG_LIST);
            }
            public void mouseClicked(MouseEvent e) {
                if (selectedCard != null) selectedCard.setBackground(BG_LIST);
                selectedCard = card;
                card.setBackground(BG_SELECTED);
                msg.setRead(true);
                lblFrom.setForeground(TEXT_GRAY);
                lblFrom.setFont(lblFrom.getFont().deriveFont(Font.PLAIN));
                lblSubj.setFont(lblSubj.getFont().deriveFont(Font.PLAIN));
                showMailContent(msg);
            }
        });

        return card;
    }

    // ===== PANEL NỘI DUNG MAIL =====
    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CONTENT);

        // Header nội dung
        JPanel header = new JPanel();
        header.setBackground(BG_CONTENT);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(20, 24, 16, 24)
        ));

        lblSubject = new JLabel("Chọn một email để đọc");
        lblSubject.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSubject.setForeground(TEXT_WHITE);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        metaRow.setOpaque(false);
        lblFrom = new JLabel("");
        lblFrom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFrom.setForeground(TEXT_GRAY);

        lblDate = new JLabel("");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDate.setForeground(TEXT_DIMMED);

        metaRow.add(lblFrom);
        metaRow.add(new JLabel("  ·  ") {{ setForeground(TEXT_DIMMED); }});
        metaRow.add(lblDate);

        header.add(lblSubject);
        header.add(Box.createVerticalStrut(6));
        header.add(metaRow);

        // Nội dung body
        txtBody = new JTextArea();
        txtBody.setEditable(false);
        txtBody.setLineWrap(true);
        txtBody.setWrapStyleWord(true);
        txtBody.setBackground(BG_CONTENT);
        txtBody.setForeground(TEXT_WHITE);
        txtBody.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBody.setBorder(new EmptyBorder(20, 24, 20, 24));
        txtBody.setCaretColor(ACCENT_BLUE);
        txtBody.setText("← Chọn một email từ danh sách bên trái");
        txtBody.setForeground(TEXT_DIMMED);

        JScrollPane bodyScroll = new JScrollPane(txtBody);
        bodyScroll.setBorder(null);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(header, BorderLayout.NORTH);
        panel.add(bodyScroll, BorderLayout.CENTER);

        return panel;
    }

    // ===== Hiển thị nội dung mail được chọn =====
    private void showMailContent(MailMessage msg) {
        lblSubject.setText(msg.getSubject());
        lblFrom.setText("Từ: " + msg.getFrom());
        lblDate.setText(msg.getDate());
        txtBody.setForeground(TEXT_WHITE);
        txtBody.setText(msg.getBody());
        txtBody.setCaretPosition(0); // Cuộn lên đầu
    }

    // ===== Helpers =====
    private int getFromNameHash(String name) {
        if (name == null || name.isEmpty()) return 0;
        return name.charAt(0);
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.equals("N/A")) return "";
        // Rút gọn ngày nếu quá dài
        if (rawDate.length() > 20) {
            // Ví dụ: "Tue, 14 Oct 2026 06:30:00 +0700" → "14 Oct 2026"
            try {
                String[] parts = rawDate.replaceAll("^[A-Za-z]+,\\s*", "").split("\\s+");
                if (parts.length >= 3) return parts[0] + " " + parts[1] + " " + parts[2];
            } catch (Exception ignored) {}
        }
        return rawDate;
    }
}
