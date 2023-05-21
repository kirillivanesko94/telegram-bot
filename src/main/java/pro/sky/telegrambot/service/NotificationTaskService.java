package pro.sky.telegrambot.service;

import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repositories.NotificationTaskRepository;

@Service
public class NotificationTaskService {
private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    public NotificationTask createNotificationTask(NotificationTask notificationTask){
        if (notificationTask == null){
            throw new IllegalArgumentException();
        }
        return notificationTaskRepository.save(notificationTask);
    }
}
