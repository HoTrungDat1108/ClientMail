/*
 * MailMessage.java - Đại diện cho một email
 */
package client;

import java.util.Date;

/**
 * Model chứa thông tin của một email
 * @author htd11
 */
public class MailMessage {

    private int    number;      // Số thứ tự trên server
    private String messageId;   // Message-ID header
    private String from;        // Người gửi
    private String subject;     // Tiêu đề
    private String date;        // Ngày gửi (dạng string từ header)
    private String body;        // Nội dung full
    private boolean isRead;

    public MailMessage() {
        this.isRead = false;
    }

    public MailMessage(int number, String from, String subject, String date, String body) {
        this.number  = number;
        this.from    = from;
        this.subject = subject;
        this.date    = date;
        this.body    = body;
        this.isRead  = false;
    }

    // ===== Getters & Setters =====
    public int getNumber()              { return number; }
    public void setNumber(int n)        { this.number = n; }

    public String getMessageId()        { return messageId; }
    public void setMessageId(String id) { this.messageId = id; }

    public String getFrom()             { return from; }
    public void setFrom(String from)    { this.from = from; }

    public String getSubject()          { return subject; }
    public void setSubject(String s)    { this.subject = s; }

    public String getDate()             { return date; }
    public void setDate(String d)       { this.date = d; }

    public String getBody()             { return body; }
    public void setBody(String body)    { this.body = body; }

    public boolean isRead()             { return isRead; }
    public void setRead(boolean r)      { this.isRead = r; }

    /**
     * Lấy preview ngắn của nội dung (50 ký tự đầu)
     */
    public String getPreview() {
        if (body == null || body.isEmpty()) return "(Không có nội dung)";
        String clean = body.trim().replaceAll("\\s+", " ");
        return clean.length() > 80 ? clean.substring(0, 80) + "..." : clean;
    }

    /**
     * Hiển thị tên người gửi đẹp hơn (bỏ phần email nếu có tên)
     * VD: "Nguyen Van A <a@mail.com>" → "Nguyen Van A"
     */
    public String getFromName() {
        if (from == null) return "Unknown";
        int idx = from.indexOf('<');
        if (idx > 0) return from.substring(0, idx).trim().replaceAll("\"", "");
        return from;
    }

    @Override
    public String toString() {
        return "[" + number + "] " + subject + " - " + from;
    }
}
