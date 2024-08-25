package reminder.discord.bot.java.dto;

import reminder.discord.bot.java.model.ParticipantUserIdsString;

public class DraftReminderUpdate {
    private String firstInteractionId;
    private ParticipantUserIdsString participantUserIds;
    private String title;
    private String description;

    public DraftReminderUpdate(
            String firstInteractionId,
            ParticipantUserIdsString participantUserIds,
            String title,
            String description) {
        this.firstInteractionId = firstInteractionId;
        this.participantUserIds = participantUserIds;
        this.title = title;
        this.description = description;
    }

    public String getFirstInteractionId() {
        return firstInteractionId;
    }

    public void setFirstInteractionId(String firstInteractionId) {
        this.firstInteractionId = firstInteractionId;
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
}
