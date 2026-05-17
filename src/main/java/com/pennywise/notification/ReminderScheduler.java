package com.pennywise.notification;

import com.pennywise.user.User;
import com.pennywise.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReminderScheduler.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    // Run at 10 AM on the last day of every month
    @Scheduled(cron = "0 0 10 L * ?")
    public void sendMonthlyStatementReminder() {
        logger.info("Starting monthly statement reminder job...");
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                String subject = "Action Required: Upload your monthly statements!";
                String text = "Hi " + user.getName() + ",\n\n" +
                        "This is a reminder to upload your monthly statements (CSV or Screenshots) to PennyWise.\n" +
                        "Our AI will automatically process them and update your budgets!\n\n" +
                        "Best,\nPennyWise Team";
                
                try {
                    emailService.sendReminderEmail(user.getEmail(), subject, text);
                    logger.info("Sent reminder email to: {}", user.getEmail());
                } catch (Exception e) {
                    logger.error("Failed to send reminder to: {}", user.getEmail(), e);
                }
            }
        }
        logger.info("Completed monthly statement reminder job.");
    }
}
