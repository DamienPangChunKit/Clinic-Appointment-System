package com.example.clinicappointmentsystems;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendEmailOnUpdate extends AsyncTask<Void, Void, Void> {

    private Context context;
    private final String toRecipentEmail = EditAdminProfile.toRecipentEmail;
    private final String bySenderUsername = EditAdminProfile.bySenderUsername;
    private final String bySenderRole = EditAdminProfile.bySenderRole;

    private Session session;
    private String email = "clinicapps123@gmail.com";
    private String password = "clinicapps123-123";

    public SendEmailOnUpdate(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "587");

        session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            mimeMessage.setFrom(new InternetAddress(email));
            mimeMessage.addRecipients(Message.RecipientType.TO, String.valueOf(new InternetAddress(toRecipentEmail)));
            mimeMessage.setSubject("Profile information changed!");
            mimeMessage.setText("Your profile information was changed by " + bySenderUsername + "\n"
                                + "Role: " + bySenderRole);
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return null;

    }
}
