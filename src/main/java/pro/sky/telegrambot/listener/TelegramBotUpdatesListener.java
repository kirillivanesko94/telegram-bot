package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
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

            generateWelcomeMessage(updates);

            createNotificationTaskInDataBase(updates);

            sendingNotification();
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public void generateWelcomeMessage(List<Update> updates) {
        updates.forEach(update -> {
            if (update.message() != null && "/start".equals(update.message().text())) {
                String name = update.message().chat().firstName();
                long id = update.message().chat().id();
                SendMessage message = new SendMessage(id, "Привет, " + name + "! Отправь сообшение следующего вида " +
                        "<01.01.2022 20:00 Сделать домашнюю работу> и я обязательно напомню тебе о твоей задаче!");
                SendResponse response = telegramBot.execute(message);
            }
        });
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
        if (localDateTime.isBefore(LocalDateTime.now())) {
            Long chatId = message.chat().id();
            SendMessage message1 = new SendMessage(chatId, "Извините, но введенная вами дата раньше текущей!");
            SendResponse response = telegramBot.execute(message1);
        } else {
            notificationTask.setDate(localDateTime);
        }
        notificationTask.setText(task);
        notificationTask.setIdChat(message.chat().id());
        return notificationTask;
    }

    public void createNotificationTaskInDataBase(List<Update> updates) {
        List<Message> collect = updates
                .stream()
                .map(Update::message)
                .filter(message -> checkMessageToValidPattern(message.text()))
                .collect(Collectors.toList());
        logger.info("Collect contains {}", collect.size());
        if (collect.isEmpty()) {
            Long chatId = updates
                    .stream()
                    .findFirst()
                    .map(Update::message)
                    .map(Message::chat)
                    .map(Chat::id)
                    .orElse(null);
            SendMessage message1 = new SendMessage(chatId, "Извините, но введенное вами сообщение не соответсвует шаблону!");
            SendResponse response = telegramBot.execute(message1);
        }
        collect.forEach(message -> {
            logger.info("Send respond {}", message.text());
            notificationTaskService.createNotificationTask(createNotificationTaskEntity(message));
        });
    }

    @Scheduled(cron = "0 * * * * *")
    public void sendingNotification() {
        Collection<NotificationTask> exists = notificationTaskService
                .getCollectionNotificationTask(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        if (!exists.isEmpty()) {
            exists
                    .forEach(notificationTask -> {
                        SendMessage message = new SendMessage(notificationTask.getIdChat(), notificationTask.getText());
                        SendResponse response = telegramBot.execute(message);
                    });
        }
    }


}
