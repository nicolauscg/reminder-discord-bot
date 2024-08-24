package reminder.discord.bot.java.model;

import java.security.Timestamp;

public class Reminder {
    private Integer id;
    private String userId;
    private String guildId;
    private String title;
    private String description;
    private Boolean isNotifiedAfterComplete;
    private Timestamp createdAt;

    public Reminder() {
    }

    public static Reminder fromDraft(DraftReminder draftReminder) {
        Reminder reminder = new Reminder();
        reminder.setUserId(draftReminder.getUserId());
        reminder.setGuildId(draftReminder.getGuildId());
        reminder.setTitle(draftReminder.getTitle());
        reminder.setDescription(draftReminder.getDescription());
        reminder.setIsNotifiedAfterComplete(false);
        return reminder;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsNotifiedAfterComplete() {
        return isNotifiedAfterComplete;
    }

    public void setIsNotifiedAfterComplete(Boolean isNotifiedAfterComplete) {
        this.isNotifiedAfterComplete = isNotifiedAfterComplete;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
