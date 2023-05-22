package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private static final Pattern PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2})\\s(.+)");
    private final NotificationTaskService notificationTaskService;

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }
    public TelegramBotUpdatesListener(NotificationTaskService notificationTaskService) {
        this.notificationTaskService = notificationTaskService;
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            processMessage(updates);
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public void processMessage(List<Update> updates) {
        List<Update> updatesForCheck = new ArrayList<>();
        updates.forEach(update -> {
            if (update.message() != null && "/start".equals(update.message().text())) {
                String name = update.message().chat().firstName();
                long id = update.message().chat().id();
                SendMessage message = new SendMessage(id, "Привет, " + name + "! Отправь сообшение следующего вида " +
                        "<01.01.2022 20:00 Сделать домашнюю работу> и я обязательно напомню тебе о твоей задаче!");
                telegramBot.execute(message);
            } else {
                updatesForCheck.add(update);
            }
        });

        processNotStartMessages(updatesForCheck);
    }

    private void processNotStartMessages(List<Update> updates) {
        updates.forEach(update -> {
            Long chatId = update.message().chat().id();
            //  Проверяем на валидность, если не валидно -- шлем сообщение
            Matcher matcher = PATTERN.matcher(update.message().text());
            if (!matcher.matches()) {
                sendMessage(chatId, "Извините, но введенное вами сообщение не соответсвует шаблону!");
                return;
            }
            //  Парсим, если дата меньше текущей -- шлем сообщение
            String date = matcher.group(1);
            String task = matcher.group(2);
            LocalDateTime taskDt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            if (taskDt.isBefore(LocalDateTime.now())) {
                sendMessage(chatId, "Извините, но введенная вами дата раньше текущей!");
                return;
            }
            // Если прошли все проверки то сохраняем в БД, те вызываем createNotificationTaskEntity
            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setIdChat(chatId);
            notificationTask.setDate(taskDt);
            notificationTask.setText(task);

            notificationTaskService.createNotificationTask(notificationTask);
        });
    }

    private void sendMessage(Long chatId, String msg) {
        SendMessage tlgMsg = new SendMessage(chatId, msg);
        telegramBot.execute(tlgMsg);
    }


}
