package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class MailService {

    private final boolean enabled;
    private final Properties smtp = new Properties();
    private final String username;
    private final String password;
    private final String from;

    public MailService() {
        Properties file = new Properties();
        try (InputStream in = MailService.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (in != null) {
                file.load(in);
            }
        } catch (IOException ex) {
            System.out.println("InBank could not read mail.properties: " + ex.getMessage());
        }
        enabled = Boolean.parseBoolean(file.getProperty("enabled", "false").trim());
        username = file.getProperty("username", "").trim();
        password = file.getProperty("password", "");
        from = file.getProperty("from", "noreply@inbank.local").trim();
        smtp.put("mail.smtp.host", file.getProperty("host", "smtp.gmail.com").trim());
        smtp.put("mail.smtp.port", file.getProperty("port", "587").trim());
        smtp.put("mail.smtp.auth", "true");
        smtp.put("mail.smtp.starttls.enable", file.getProperty("starttls", "true").trim());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void send(String to, String subject, String body) {
        if (!enabled || to == null || to.isBlank()) {
            return;
        }
        try {
            Session session = Session.getInstance(smtp, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            MimeMessage message = new MimeMessage(session);
            message.setFrom(InternetAddress.parse(from)[0]);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (Exception ex) {
            System.out.println("InBank email was not sent: " + ex.getMessage());
        }
    }
}
