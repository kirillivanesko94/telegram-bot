package pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
@Service
public class Scheduler {
    private final NotificationTaskService notificationTaskService;
    private final TelegramBot telegramBot;

    public Scheduler(NotificationTaskService notificationTaskService, TelegramBot telegramBot) {
        this.notificationTaskService = notificationTaskService;
        this.telegramBot = telegramBot;
    }


    @Scheduled(cron = "0 * * * * *")
    public void sendingNotification() {
        Collection<NotificationTask> exists = notificationTaskService
                .getCollectionNotificationTask(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        if (!exists.isEmpty()) {
            exists
                    .forEach(notificationTask -> {
                        SendMessage message = new SendMessage(notificationTask.getIdChat(), notificationTask.getText());
                        telegramBot.execute(message);
                    });
        }
    }
}

