package reminder.discord.bot.java.dto;

public class DraftReminderCreate {
    private String firstInteractionId;
    private String userId;
    private String guildId;

    public DraftReminderCreate(String firstInteractionId, String userId, String guildId) {
        this.firstInteractionId = firstInteractionId;
        this.userId = userId;
        this.guildId = guildId;
    }

    public String getFirstInteractionId() {
        return firstInteractionId;
    }

    public void setFirstInteractionId(String firstInteractionId) {
        this.firstInteractionId = firstInteractionId;
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
}
