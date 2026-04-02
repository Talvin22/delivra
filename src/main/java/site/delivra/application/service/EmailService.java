package site.delivra.application.service;

import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.entities.User;

public interface EmailService {
    void sendChatNotification(DeliveryTask task, User sender, String messageText);
    void sendTaskAssignedNotification(DeliveryTask task, User driver);
    void sendTaskStatusChangedNotification(DeliveryTask task, User driver);
}
