package reminder.discord.bot.java.dto;

import java.time.Instant;

public class ReminderParticipantCreate {
    private Integer reminderId;
    private String userId;
    private Boolean isComplete;
    private Integer remindedCount;
    private Instant lastRemindedAt;
    private Instant nextRemindAt;

    public ReminderParticipantCreate(Integer reminderId, String userId, Instant nextRemindAt) {
        this.reminderId = reminderId;
        this.userId = userId;
        this.isComplete = false;
        this.remindedCount = 0;
        this.lastRemindedAt = null;
        this.nextRemindAt = nextRemindAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
    }

    public Integer getRemindedCount() {
        return remindedCount;
    }

    public void setRemindedCount(Integer remindedCount) {
        this.remindedCount = remindedCount;
    }

    public Instant getLastRemindedAt() {
        return lastRemindedAt;
    }

    public void setLastRemindedAt(Instant lastRemindedAt) {
        this.lastRemindedAt = lastRemindedAt;
    }

    public Instant getNextRemindAt() {
        return nextRemindAt;
    }

    public void setNextRemindAt(Instant nextRemindAt) {
        this.nextRemindAt = nextRemindAt;
    }

    public Integer getReminderId() {
        return reminderId;
    }

    public void setReminderId(Integer reminderId) {
        this.reminderId = reminderId;
    }
}
