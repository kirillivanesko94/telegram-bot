package pro.sky.telegrambot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class NotificationTask {
    @Id
    @GeneratedValue
    Long id;
    String text;
    LocalDateTime date;
    Long idChat;

    public NotificationTask() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getIdChat() {
        return idChat;
    }

    public void setIdChat(Long idChat) {
        this.idChat = idChat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTask that = (NotificationTask) o;
        return Objects.equals(id, that.id) && Objects.equals(text, that.text) && Objects.equals(date, that.date) && Objects.equals(idChat, that.idChat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, date, idChat);
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", date=" + date +
                ", idChat=" + idChat +
                '}';
    }
}
