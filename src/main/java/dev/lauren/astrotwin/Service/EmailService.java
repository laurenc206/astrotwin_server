package dev.lauren.astrotwin.Service;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import dev.lauren.astrotwin.Model.ContactForm;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private Environment env;
    
    public String sendFeedback(ContactForm form) {
        String to = env.getProperty("MAIL_TO");
        String from = env.getProperty("MAIL_FROM");

        final String username = env.getProperty("MAIL_USERNAME");
        final String password = env.getProperty("MAIL_PASSWORD");
        String host = env.getProperty("MAIL_HOST");
        String port = env.getProperty("MAIL_PORT");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
            }
        };

        Session session = Session.getInstance(props, authenticator);
        StringBuilder response = new StringBuilder();
        
        try {
            //create a MimeMessage object
            Message message = new MimeMessage(session);
            //set From email field
            message.setFrom(new InternetAddress(from));
            //set To email field
            message.setRecipients(Message.RecipientType.TO,
                       InternetAddress.parse(to));
            //set email subject field
            message.setSubject("AstroTwin Feedback from " + form.getFirstName() + " " + form.getLastName() + " at " + form.getEmail());
            //set the content of the email message
            message.setText(form.getMessage());
            //send the email message
            Transport.send(message);
            System.out.println("Email Message Sent Successfully");
            response.append("Success");
        } catch (MessagingException e) {
            response.append("Fail");
            System.out.println("Could not send");
            System.out.println(e);
            throw new RuntimeException(e); 
        }
        
        if (form.getFirstName().equals("Test")) return "Fail";
        return response.toString();
    }

}
