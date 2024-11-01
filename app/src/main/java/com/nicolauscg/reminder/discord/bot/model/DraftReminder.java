package com.nicolauscg.reminder.discord.bot.model;

import java.time.Instant;

// Class methods are generated with help from IDE.

public class DraftReminder {
    private String firstInteractionId;
    private String userId;
    private String guildId;
    private ParticipantUserIdsString participantUserIds;
    private String title;
    private String description;
    private Instant updatedAt;

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
        if (participantUserIds == null) {
            return null;
        }
        return participantUserIds.toString();
    }

    public ParticipantUserIdsString getParticipantUserIdsAsClass() {
        return participantUserIds;
    }

    public void setParticipantUserIds(String participantUserIds) {
        this.participantUserIds = ParticipantUserIdsString.fromString(participantUserIds);
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DraftReminder [firstInteractionId=" + firstInteractionId + ", userId=" + userId + ", guildId=" + guildId
                + ", participantUserIds=" + participantUserIds + ", title=" + title + ", description=" + description
                + ", updatedAt=" + updatedAt + "]";
    }
}
