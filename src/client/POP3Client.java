/*
 * POP3Client.java - Xử lý kết nối POP3 thuần Java (không dùng thư viện ngoài)
 */
package client;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Kết nối tới hMailServer qua giao thức POP3 (port 110)
 * Các lệnh POP3 cơ bản: USER, PASS, STAT, LIST, RETR, QUIT
 * @author htd11
 */
public class POP3Client {

    private final String host;
    private final int    port;
    private final String username;
    private final String password;

    private Socket         socket;
    private BufferedReader reader;
    private PrintWriter    writer;

    public POP3Client(String host, int port, String username, String password) {
        this.host     = host;
        this.port     = port;
        this.username = username;
        this.password = password;
    }

    // ===== Kết nối và xác thực =====
    public void connect() throws IOException {
        System.err.println("[POP3] Đang kết nối tới " + host + ":" + port);
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 5000); // timeout 5s
        socket.setSoTimeout(15000);

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

        // Đọc greeting từ server
        String greeting = readLine();
        System.err.println("[POP3] Greeting: " + greeting);
        if (!greeting.startsWith("+OK")) {
            throw new IOException("Server không phản hồi đúng: " + greeting);
        }

        // Gửi USER
        System.err.println("[POP3] Gửi: USER " + username);
        sendCommand("USER " + username);
        String userResp = readLine();
        System.err.println("[POP3] Phản hồi USER: " + userResp);
        if (!userResp.startsWith("+OK")) {
            throw new IOException("Tài khoản không tồn tại: " + userResp);
        }

        // Gửi PASS
        System.err.println("[POP3] Gửi: PASS ****");
        sendCommand("PASS " + password);
        String passResp = readLine();
        System.err.println("[POP3] Phản hồi PASS: " + passResp);
        if (!passResp.startsWith("+OK")) {
            throw new IOException("Sai mật khẩu hoặc tài khoản bị khóa: " + passResp);
        }
        System.err.println("[POP3] Đăng nhập thành công!");
    }

    // ===== Lấy số lượng mail =====
    public int getMessageCount() throws IOException {
        sendCommand("STAT");
        String resp = readLine();
        // +OK <count> <size>
        if (resp.startsWith("+OK")) {
            String[] parts = resp.split("\\s+");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        }
        return 0;
    }

    // ===== Lấy danh sách và nội dung tất cả mail =====
    public List<MailMessage> fetchMessages() throws IOException {
        connect();

        List<MailMessage> messages = new ArrayList<>();
        int count = getMessageCount();

        // Giới hạn lấy tối đa 50 mail gần nhất
        int start = Math.max(1, count - 49);

        for (int i = count; i >= start; i--) {
            try {
                MailMessage msg = retrieveMessage(i);
                messages.add(msg);
            } catch (Exception e) {
                // Bỏ qua mail lỗi, tiếp tục
                System.err.println("Lỗi lấy mail #" + i + ": " + e.getMessage());
            }
        }

        // Đóng kết nối sạch sau khi fetch xong
        disconnect();

        return messages;
    }

    // ===== Lấy nội dung một mail theo số thứ tự =====
    public MailMessage retrieveMessage(int msgNumber) throws IOException {
        sendCommand("RETR " + msgNumber);
        String firstLine = readLine();
        if (!firstLine.startsWith("+OK")) {
            throw new IOException("Không lấy được mail #" + msgNumber + ": " + firstLine);
        }

        // Đọc toàn bộ nội dung cho đến khi gặp dấu "." trên một dòng riêng
        StringBuilder rawContent = new StringBuilder();
        String line;
        while ((line = readLine()) != null) {
            if (line.equals(".")) break;
            // POP3: dòng bắt đầu bằng ".." thì bỏ một dấu "."
            if (line.startsWith("..")) line = line.substring(1);
            rawContent.append(line).append("\n");
        }

        return parseMail(msgNumber, rawContent.toString());
    }

    // ===== Parse raw email thành MailMessage =====
    private MailMessage parseMail(int number, String raw) {
        MailMessage msg = new MailMessage();
        msg.setNumber(number);

        String[] lines = raw.split("\n");
        boolean inHeader = true;
        StringBuilder bodyBuilder = new StringBuilder();
        StringBuilder currentHeaderValue = new StringBuilder();
        String currentHeaderName = "";

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (inHeader) {
                if (line.trim().isEmpty()) {
                    // Hết header
                    saveHeader(msg, currentHeaderName, currentHeaderValue.toString().trim());
                    inHeader = false;
                    continue;
                }

                // Header tiếp theo (folded header bắt đầu bằng khoảng trắng)
                if ((line.startsWith(" ") || line.startsWith("\t")) && !currentHeaderName.isEmpty()) {
                    currentHeaderValue.append(" ").append(line.trim());
                } else {
                    // Lưu header trước
                    if (!currentHeaderName.isEmpty()) {
                        saveHeader(msg, currentHeaderName, currentHeaderValue.toString().trim());
                    }
                    // Parse header mới
                    int colonIdx = line.indexOf(':');
                    if (colonIdx > 0) {
                        currentHeaderName  = line.substring(0, colonIdx).trim().toLowerCase();
                        currentHeaderValue = new StringBuilder(line.substring(colonIdx + 1).trim());
                    }
                }
            } else {
                bodyBuilder.append(line).append("\n");
            }
        }

        // Nếu body rỗng hoặc được encode (base64/quoted-printable), hiển thị thô
        String body = bodyBuilder.toString().trim();
        if (body.isEmpty()) body = "(Không có nội dung)";
        msg.setBody(body);

        // Giá trị mặc định nếu thiếu
        if (msg.getFrom()    == null) msg.setFrom("unknown@unknown");
        if (msg.getSubject() == null) msg.setSubject("(Không có tiêu đề)");
        if (msg.getDate()    == null) msg.setDate("N/A");

        return msg;
    }

    // ===== Gán header vào MailMessage =====
    private void saveHeader(MailMessage msg, String name, String value) {
        switch (name) {
            case "from":       msg.setFrom(decodeHeader(value));    break;
            case "subject":    msg.setSubject(decodeHeader(value)); break;
            case "date":       msg.setDate(value);                  break;
            case "message-id": msg.setMessageId(value);             break;
        }
    }

    // ===== Decode encoded header (=?UTF-8?B?...?= hoặc =?UTF-8?Q?...?=) =====
    private String decodeHeader(String value) {
        if (value == null) return "";
        try {
            // Xử lý UTF-8 Base64: =?UTF-8?B?...?=
            if (value.contains("=?") && value.contains("?=")) {
                StringBuilder result = new StringBuilder();
                String remaining = value;
                while (remaining.contains("=?")) {
                    int start = remaining.indexOf("=?");
                    result.append(remaining, 0, start);
                    int end = remaining.indexOf("?=", start + 2);
                    if (end < 0) { result.append(remaining.substring(start)); break; }
                    String encoded = remaining.substring(start + 2, end);
                    String[] parts = encoded.split("\\?", 3);
                    if (parts.length == 3) {
                        String charset  = parts[0];
                        String encoding = parts[1].toUpperCase();
                        String data     = parts[2];
                        if ("B".equals(encoding)) {
                            byte[] bytes = Base64.getDecoder().decode(data);
                            result.append(new String(bytes, charset));
                        } else if ("Q".equals(encoding)) {
                            String qp = data.replace('_', ' ');
                            result.append(qp); // Simplified
                        } else {
                            result.append(data);
                        }
                    }
                    remaining = remaining.substring(end + 2);
                }
                result.append(remaining);
                return result.toString().trim();
            }
        } catch (Exception e) {
            // Trả về nguyên gốc nếu lỗi decode
        }
        return value.trim();
    }

    // ===== Gửi lệnh POP3 =====
    private void sendCommand(String command) {
        writer.println(command);
        writer.flush();
    }

    // ===== Đọc một dòng từ server =====
    private String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) throw new IOException("Kết nối bị đóng bởi server");
        return line;
    }

    // ===== Ngắt kết nối =====
    public void disconnect() {
        try {
            if (writer != null) {
                writer.println("QUIT");
                writer.flush();
            }
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {}
    }

    public String getUsername() { return username; }
    public String getHost()     { return host; }
}
