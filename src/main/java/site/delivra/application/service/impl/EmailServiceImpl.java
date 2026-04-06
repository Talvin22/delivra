package site.delivra.application.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.entities.User;
import site.delivra.application.repository.UserRepository;
import site.delivra.application.service.EmailService;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from.address:${spring.mail.username:}}")
    private String mailFromAddress;

    @Value("${app.mail.from.name:Delivra Notifications}")
    private String mailFromName;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    @Async
    @Override
    public void sendChatNotification(DeliveryTask task, User sender, String messageText) {
        if (!mailEnabled) {
            log.debug("Mail disabled, skipping chat notification for task #{}", task.getId());
            return;
        }

        Optional<User> recipientOpt = resolveRecipient(task, sender);
        if (recipientOpt.isEmpty()) {
            log.debug("No recipient found for chat notification, task #{}", task.getId());
            return;
        }

        User recipient = recipientOpt.get();
        if (recipient.getEmail() == null) {
            return;
        }

        String preview = messageText.length() > 100
                ? messageText.substring(0, 100) + "..."
                : messageText;

        String subject = "New message in task #" + task.getId() + " — Delivra";
        String body = "<h2>New message in task #" + task.getId() + "</h2>"
                + "<p><b>From:</b> " + sender.getUsername() + "</p>"
                + "<p><b>Address:</b> " + task.getAddress() + "</p>"
                + "<p><b>Message:</b> " + preview + "</p>";

        send(recipient.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendTaskAssignedNotification(DeliveryTask task, User driver) {
        if (!mailEnabled) {
            log.debug("Mail disabled, skipping task assigned notification for task #{}", task.getId());
            return;
        }

        if (driver.getEmail() == null) {
            return;
        }

        String subject = "You have been assigned a new task — Delivra";
        String body = "<h2>New task assigned to you</h2>"
                + "<p><b>Task #:</b> " + task.getId() + "</p>"
                + "<p><b>Delivery address:</b> " + task.getAddress() + "</p>"
                + "<p><b>Status:</b> " + task.getStatus() + "</p>";

        send(driver.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendTaskStatusChangedNotification(DeliveryTask task, User driver) {
        if (!mailEnabled) {
            log.debug("Mail disabled, skipping status change notification for task #{}", task.getId());
            return;
        }

        if (driver.getEmail() == null) {
            return;
        }

        String subject = "Task #" + task.getId() + " status updated to " + task.getStatus() + " — Delivra";
        String body = "<h2>Task status updated</h2>"
                + "<p><b>Task #:</b> " + task.getId() + "</p>"
                + "<p><b>Delivery address:</b> " + task.getAddress() + "</p>"
                + "<p><b>New status:</b> " + task.getStatus() + "</p>";

        send(driver.getEmail(), subject, body);
    }

    private Optional<User> resolveRecipient(DeliveryTask task, User sender) {
        User driver = task.getUser();
        if (driver == null) {
            return Optional.empty();
        }

        boolean senderIsDriver = sender.getId().equals(driver.getId());
        if (senderIsDriver) {
            return userRepository.findByUsername(task.getCreatedBy());
        } else {
            return Optional.of(driver);
        }
    }

    @Async
    @Override
    public void sendPasswordResetEmail(User user, String token) {
        if (!mailEnabled) return;
        String link = baseUrl + "/reset-password?token=" + token;
        String subject = "Password reset — Delivra";
        String body = "<h2>Password reset request</h2>"
                + "<p>Click the link below to set a new password. The link is valid for <b>1 hour</b>.</p>"
                + "<p><a href=\"" + link + "\">Reset password</a></p>"
                + "<p>If you did not request this, ignore this email.</p>";
        send(user.getEmail(), subject, body);
    }

    @Async
    @Override
    public void sendEmailVerificationEmail(User user, String token) {
        if (!mailEnabled) return;
        String link = baseUrl + "/verify-email?token=" + token;
        String subject = "Confirm your email — Delivra";
        String body = "<h2>Welcome to Delivra, " + user.getUsername() + "!</h2>"
                + "<p>Please confirm your email address by clicking the link below:</p>"
                + "<p><a href=\"" + link + "\">Verify email</a></p>";
        send(user.getEmail(), subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(new InternetAddress(mailFromAddress, mailFromName));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.debug("Email sent to {}: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
