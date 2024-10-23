package com.nicolauscg.reminder.discord.bot.dto;

public class ReminderCreate {
    private String userId;
    private String guildId;
    private String title;
    private String description;
    private Boolean isNotifiedAfterComplete;
    
    public ReminderCreate(String userId, String guildId, String title, String description) {
        this.userId = userId;
        this.guildId = guildId;
        this.title = title;
        this.description = description;
        this.isNotifiedAfterComplete = false;
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
}
