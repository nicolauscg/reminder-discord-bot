package reminder.discord.bot.java.model;

import java.sql.Timestamp;
import java.util.List;

// Class methods are generated with help from IDE.

public class DraftReminder {
    private String firstInteractionId;
    private String userId;
    private String guildId;
    private ParticipantUserIdsString participantUserIds;
    private String title;
    private String description;
    private Timestamp updatedAt;

    public DraftReminder() {
    }

    public DraftReminder(String firstInteractionId, String userId, String guildId) {
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

    public String getParticipantUserIds() {
        return participantUserIds.toString();
    }

    public ParticipantUserIdsString getParticipantUserIdsAsClass() {
        return participantUserIds;
    }

    public void setParticipantUserIds(String participantUserIds) {
        this.participantUserIds = new ParticipantUserIdsString(participantUserIds);
    }

    public void setParticipantUserIds(List<String> participantUserIds) {
        this.participantUserIds = new ParticipantUserIdsString(participantUserIds);
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

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DraftReminder [firstInteractionId=" + firstInteractionId + ", userId=" + userId + ", guildId=" + guildId
                + ", participantUserIds=" + participantUserIds + ", title=" + title + ", description=" + description
                + ", updatedAt=" + updatedAt + "]";
    }
}
