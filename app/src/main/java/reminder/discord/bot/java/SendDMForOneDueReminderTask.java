package reminder.discord.bot.java;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import reminder.discord.bot.java.dto.ReminderParticipantUpdate;
import reminder.discord.bot.java.mapper.ReminderMapper;
import reminder.discord.bot.java.mapper.ReminderParticipantMapper;
import reminder.discord.bot.java.model.Reminder;
import reminder.discord.bot.java.model.ReminderParticipant;

public class SendDMForOneDueReminderTask implements Runnable {
    private SqlSessionFactory sqlSessionFactory;
    private JDA jda;

    public SendDMForOneDueReminderTask(SqlSessionFactory sqlSessionFactory, JDA jda) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.jda = jda;
    }

    @Override
    public void run() {
        System.out.println("running reminder task");
        // because sending discord DM is async, this try block exits first then we try to call updateOne, which fails
        try (SqlSession session = this.sqlSessionFactory.openSession()) {
            ReminderParticipantMapper reminderParticipantMapper = session.getMapper(ReminderParticipantMapper.class);
            ReminderMapper reminderMapper = session.getMapper(ReminderMapper.class);

            /*
             * Get information about the user to remind and the reminder itself
             */
            ReminderParticipant participant = reminderParticipantMapper.getOneToRemind(Instant.now());
            if (participant == null) {
                System.out.println("nothing to remind");
                return;
            }
            Reminder reminder = reminderMapper.getOneById(participant.getReminderId());
            User participantDiscordUser = jda.getUserById(participant.getUserId());

            /*
             * Send the Discord DM
             */
            PrivateChannel discordDmChannel = participantDiscordUser.openPrivateChannel().complete();
            // TODO improve DM message format
            discordDmChannel.sendMessage(reminder.getTitle() + ";" + reminder.getDescription()).complete();
            System.out.printf("sent a reminder with id %d to discord user id %s\n", reminder.getId(), participantDiscordUser.getId());

            /*
             * Remember that the user has been reminded
             */
            ReminderParticipantUpdate participantUpdate = new ReminderParticipantUpdate(
                participant.getReminderId(), participant.getUserId(),
                participant.getRemindedCount() + 1, Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS));
            reminderParticipantMapper.updateOne(participantUpdate);
            session.commit();
        }
        System.out.println("reminder task finished");
    }
}
