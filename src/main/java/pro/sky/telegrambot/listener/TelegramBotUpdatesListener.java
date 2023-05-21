package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private static final Pattern PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2})\\s(.+)");

    public TelegramBotUpdatesListener(NotificationTaskService notificationTaskService) {
        this.notificationTaskService = notificationTaskService;
    }

    private final NotificationTaskService notificationTaskService;

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message() != null && "/start".equals(update.message().text())) {
                TelegramBot bot = new TelegramBot(telegramBot.getToken());
                long id = update.message().chat().id();
                SendMessage message = new SendMessage(id, "Hello!");
                SendResponse response = telegramBot.execute(message);
            }
            createNotificationTaskInDataBase(updates);
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public boolean checkMessageToValidPattern(String text) {
        Matcher matcher = PATTERN.matcher(text);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public NotificationTask createNotificationTaskEntity(Message message) {
        String text = message.text();
        Matcher matcher = PATTERN.matcher(text);
        String date = "";
        String task = "";
        if (matcher.matches()) {
            date = matcher.group(1);
            task = matcher.group(2);
        }
        LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setDate(localDateTime);
        notificationTask.setText(task);
        notificationTask.setIdChat(message.chat().id());
        return notificationTask;
    }
    public void createNotificationTaskInDataBase(List<Update> updates) {
        updates
                .stream()
                .map(Update::message)
                .filter(message -> checkMessageToValidPattern(message.text()))
                .forEach(message -> notificationTaskService.createNotificationTask(createNotificationTaskEntity(message)));
    }


}
