package reminder.discord.bot.java.model;

import java.time.Instant;

public class ReminderParticipant {
    private Integer reminderId;
    private String userId;
    private Boolean isComplete;
    private Integer remindedCount;
    private Instant lastRemindedAt;
    private Instant nextRemindAt;
    
    public ReminderParticipant() {
    }

    public Integer getReminderId() {
        return reminderId;
    }

    public void setReminderId(Integer reminderId) {
        this.reminderId = reminderId;
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

    @Override
    public String toString() {
        return "ReminderParticipant [reminderId=" + reminderId + ", userId=" + userId + ", isComplete=" + isComplete
                + ", remindedCount=" + remindedCount + ", lastRemindedAt=" + lastRemindedAt + ", nextRemindAt="
                + nextRemindAt + "]";
    }
}
