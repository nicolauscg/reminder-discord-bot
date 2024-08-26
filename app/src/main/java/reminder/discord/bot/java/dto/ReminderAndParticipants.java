package reminder.discord.bot.java.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import reminder.discord.bot.java.model.DraftReminder;

// Helper class to convert a DraftReminder entity to the appropriate create DTOs of a non-draft reminder.
// A non-draft reminder consists of one Reminder entity and many ReminderParticipant entities.
// NOTE: getParticipantsCreate() returns DTOs with null reminderId which must be set by the caller.
public class ReminderAndParticipants {
    private ReminderCreate reminderCreate;
    private List<ReminderParticipantCreate> participantsCreate;
    
    public ReminderAndParticipants(DraftReminder draftReminder, Instant nextRemindAt) {
        this.reminderCreate = new ReminderCreate(
            draftReminder.getUserId(),
            draftReminder.getGuildId(),
            draftReminder.getTitle(),
            draftReminder.getDescription()
        );
        this.participantsCreate = new ArrayList<>();
        for (String participantUserId : draftReminder.getParticipantUserIdsAsClass().getUserIds()) {
            this.participantsCreate.add(new ReminderParticipantCreate(null, participantUserId, nextRemindAt));
        }
    }

    public ReminderCreate getReminderCreate() {
        return reminderCreate;
    }

    // Returns ReminderParticipantCreate DTOs with null reminderId which must be set by the caller.
    public List<ReminderParticipantCreate> getParticipantsCreate() {
        return participantsCreate;
    }
}
