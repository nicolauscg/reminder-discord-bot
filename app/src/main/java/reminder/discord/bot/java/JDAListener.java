package reminder.discord.bot.java;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;

import javax.annotation.Nonnull;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import reminder.discord.bot.java.dto.DraftReminderCreate;
import reminder.discord.bot.java.dto.DraftReminderUpdate;
import reminder.discord.bot.java.dto.ReminderAndParticipants;
import reminder.discord.bot.java.dto.ReminderParticipantCreate;
import reminder.discord.bot.java.dto.ReminderParticipantUpdate;
import reminder.discord.bot.java.mapper.DraftReminderMapper;
import reminder.discord.bot.java.mapper.ReminderMapper;
import reminder.discord.bot.java.mapper.ReminderParticipantMapper;
import reminder.discord.bot.java.model.DraftReminder;
import reminder.discord.bot.java.model.ParticipantUserIdsString;
import reminder.discord.bot.java.model.Reminder;

public class JDAListener extends ListenerAdapter 
{
    static final String SELECT_PARTICIPANT_LABEL = "create-reminder-participant";
    static final String MODAL_LABEL = "create-reminder-modal";
    static final String MODAL_TITLE_LABEL = "create-reminder-title";
    static final String MODAL_CONTENT_LABEL = "create-reminder-content";
    static final String CONFIRM_LABEL = "create-reminder-confirm";

    static final String SELECT_REMINDER_LABEL = "complete-reminder-select";
    static final String CONFIRM_COMPLETE_LABEL = "complete-reminder-confirm";

    private SqlSessionFactory sqlSessionFactory;

    public JDAListener(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        switch (event.getName()) {
            case "createreminder": {
                event.deferReply(true).queue();

                // Error out if command not run on a guild
                if (event.getGuild() == null) {
                    event.getHook().sendMessage("This command must be run in a server and not in a DM");
                    return;
                }

                /*
                * Save a draft reminder to the DB with the information available so far
                */
                String firstInteractionId = event.getId();
                try (SqlSession session = this.sqlSessionFactory.openSession()) {
                    DraftReminderMapper mapper = session.getMapper(DraftReminderMapper.class);

                    DraftReminderCreate reminderCreate = new DraftReminderCreate(firstInteractionId,
                        event.getUser().getId(), event.getGuild().getId());
                    mapper.createOne(reminderCreate);

                    session.commit();
                }

                /*
                * Respond with a multi select about reminder's participants 
                */
                List<Member> nonBotGuildMembers = event.getGuild().getMembers()
                    .stream().filter(mem -> !mem.getUser().isBot()).toList();

                // Put the first interaction id in the custom id, the interaction id will be
                // passed throughout the create reminder interaction chain so later interactions
                // can identify which reminder they are working on
                CustomId customId = new CustomId(SELECT_PARTICIPANT_LABEL, firstInteractionId);
                Builder selectMenuBuilder = StringSelectMenu.create(customId.toString())
                    .setPlaceholder("Select participants")
                    .setMinValues(1)
                    .setMaxValues(nonBotGuildMembers.size());

                for (Member mem : nonBotGuildMembers) {
                    String optVal = String.format("%s%s%s",
                        mem.getId(), ParticipantUserIdsString.ATTR_SEPARATOR, mem.getEffectiveName());
                    selectMenuBuilder = selectMenuBuilder.addOption(mem.getEffectiveName(), optVal);
                }
                
                MessageEmbed embedMsg = new EmbedBuilder()
                    .setColor(Color.WHITE)
                    .setTitle("Select participants to remind (Step 1 of 3)")
                    .appendDescription("A participant will be reminded every day via DM until he or she completes it with the /completereminder command.")
                    .appendDescription("\n:warning: Only interact with the select menu below once.")
                    .build();

                event.getHook().sendMessage(
                    new MessageCreateBuilder()
                        .addEmbeds(embedMsg)
                        .addComponents(ActionRow.of(selectMenuBuilder.build()))
                        .build()
                ).queue();

                break;
            }
            case "completereminder": {
                event.deferReply(true).queue();

                String userId = event.getUser().getId();
                List<Reminder> uncompletedReminders;

                // Get uncompleted reminders
                try (SqlSession session = this.sqlSessionFactory.openSession()) {
                    ReminderMapper mapper = session.getMapper(ReminderMapper.class);
                    uncompletedReminders = mapper.getManyUncompletedReminderByParticipantUserId(userId);
                }
                
                // Return early if no uncompleted reminders
                if (uncompletedReminders.size() == 0) {
                    MessageEmbed embedMsg = new EmbedBuilder()
                        .setColor(Color.WHITE)
                        .setDescription("There are no reminders to complete")
                        .build();
                    event.getHook().sendMessage(
                        new MessageCreateBuilder()
                            .addEmbeds(embedMsg)
                            .build()
                    ).queue();
                    return;
                }

                /**
                 * Reply with a select menu of reminders to complete
                 */
                Builder selectMenuBuilder = StringSelectMenu.create(SELECT_REMINDER_LABEL)
                    .setPlaceholder("Select a reminder");
                Map<String, String> userIdToUserName = new HashMap<>();
                for (Reminder reminder : uncompletedReminders) {
                    String username = userIdToUserName.get(reminder.getUserId());
                    if (username == null) {
                        username = event.getJDA().getUserById(reminder.getUserId()).getEffectiveName();
                        userIdToUserName.put(reminder.getUserId(), username);
                    }
                    String optionLabel = String.format("%s from %s", reminder.getTitle(), username);
                    selectMenuBuilder = selectMenuBuilder.addOption(optionLabel, reminder.getId().toString());
                }
                
                MessageEmbed embedMsg = new EmbedBuilder()
                    .setColor(Color.WHITE)
                    .setTitle("Select a reminder to view its details (Step 1 of 2)")
                    .build();
                
                event.getHook().sendMessage(
                    new MessageCreateBuilder()
                        .addEmbeds(embedMsg)
                        .addComponents(ActionRow.of(selectMenuBuilder.build()))
                        .build()
                ).queue();

                break;
            }
            default: {
                event.reply(String.format("Command %s is not recognized", event.getName()))
                    .setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        CustomId eventCustomId = new CustomId(event.getComponentId());
        if (eventCustomId.label.equals(SELECT_PARTICIPANT_LABEL)) {
            String firstInteractionId = eventCustomId.firstIntrId;
            
            // Save participants to DB
            try (SqlSession session = this.sqlSessionFactory.openSession()) {
                DraftReminderMapper mapper = session.getMapper(DraftReminderMapper.class);

                ParticipantUserIdsString participants = new ParticipantUserIdsString(event.getValues());
                DraftReminderUpdate reminderUpdate = new DraftReminderUpdate(firstInteractionId, participants, null, null);
                mapper.updateOne(reminderUpdate);

                session.commit();
            }

            // Looks like deferReply() cannot be used when replying with a modal 
            TextInput title = TextInput.create(MODAL_TITLE_LABEL, "Title", TextInputStyle.SHORT)
                .setPlaceholder("Title")
                .setMaxLength(50)
                .setRequired(true)
                .build();
            TextInput content = TextInput.create(MODAL_CONTENT_LABEL, "Content", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Content")
                    .setMaxLength(500)
                    .setRequired(true)
                    .build();
            CustomId customId = new CustomId(MODAL_LABEL, eventCustomId.firstIntrId);
            Modal modal = Modal.create(customId.toString(), "Enter reminder details (Step 2 of 3)")
                    .addComponents(ActionRow.of(title), ActionRow.of(content))
                    .build();
            event.replyModal(modal).queue();
        } else if (eventCustomId.label.equals(SELECT_REMINDER_LABEL)) {
            event.deferReply(true).queue();
            Integer reminderId = Integer.parseInt(event.getValues().get(0));
            Reminder reminder;

            try (SqlSession session = this.sqlSessionFactory.openSession()) {
                ReminderMapper mapper = session.getMapper(ReminderMapper.class);
                reminder = mapper.getOneById(reminderId);
            }

            User authorDiscordUser = event.getJDA().getUserById(reminder.getUserId());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM").withZone(ZoneOffset.UTC);

            MessageEmbed embedMsg = new EmbedBuilder()
                .setColor(Color.WHITE)
                .setTitle("Confirm complete reminder (Step 2 of 2)")
                .addField("Title", reminder.getTitle(), false)
                .addField("Author", authorDiscordUser.getEffectiveName(), true)
                .addField("Create date", formatter.format(reminder.getCreatedAt()), true)
                .addField("Content", reminder.getDescription(), false)
                .appendDescription("\n:warning: Only interact with the button below once.")
                .build();
                
            CustomId customId = new CustomId(CONFIRM_COMPLETE_LABEL, reminderId.toString());
            event.getHook().sendMessage(
                new MessageCreateBuilder()
                    .addEmbeds(embedMsg)
                    .addComponents(ActionRow.of(Button.primary(customId.toString(), "Complete reminder")))
                    .build()
            ).queue();
        }
    }

    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        CustomId eventCustomId = new CustomId(event.getModalId());
        if (eventCustomId.label.equals(MODAL_LABEL)) {
            event.deferReply(true).queue();
            String firstInteractionId = eventCustomId.firstIntrId;

            /*
             * Parse modal values from previous interaction
             */
            String title = event.getValue(MODAL_TITLE_LABEL).getAsString();
            String content = event.getValue(MODAL_CONTENT_LABEL).getAsString();
            
            // Save title and description to DB while getting the participants
            ParticipantUserIdsString participants;
            try (SqlSession session = this.sqlSessionFactory.openSession()) {
                DraftReminderMapper mapper = session.getMapper(DraftReminderMapper.class);
                
                DraftReminderUpdate reminderUpdate = new DraftReminderUpdate(firstInteractionId, null, title, content);
                mapper.updateOne(reminderUpdate);

                DraftReminder draftReminder = mapper.getOneByFirstInteractionId(firstInteractionId);
                participants = draftReminder.getParticipantUserIdsAsClass();

                session.commit();
            }
            
            MessageEmbed embedMsg = new EmbedBuilder()
                .setColor(Color.WHITE)
                .setTitle("Confirm reminder details (Step 3 of 3)")
                .addField("Participants", String.join(", ", participants.getNames()), false)
                .addField("Title", title, false)
                .addField("Content", content, false)
                .appendDescription("\n:warning: Only interact with the button below once.")
                .build();

            CustomId customId = new CustomId(CONFIRM_LABEL, eventCustomId.firstIntrId);
            event.getHook().sendMessage(
                new MessageCreateBuilder()
                    .addEmbeds(embedMsg)
                    .addComponents(ActionRow.of(Button.primary(customId.toString(), "Create reminder")))
                    .build()
            ).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        CustomId eventCustomId = new CustomId(event.getComponentId());
        if (eventCustomId.label.equals(CONFIRM_LABEL)) {
            // Use non-ephemeral message to acknowledge the created reminder later
            event.deferReply(false).queue();
            String firstInteractionId = eventCustomId.firstIntrId;

            try (SqlSession session = this.sqlSessionFactory.openSession()) {
                DraftReminderMapper draftReminderMapper = session.getMapper(DraftReminderMapper.class);
                ReminderMapper reminderMapper = session.getMapper(ReminderMapper.class);
                ReminderParticipantMapper reminderParticipantMapper = session.getMapper(ReminderParticipantMapper.class);

                // Create Reminder
                DraftReminder draftReminder = draftReminderMapper.getOneByFirstInteractionId(firstInteractionId);
                ReminderAndParticipants reminderAndParticipants = new ReminderAndParticipants(
                    draftReminder, Instant.now());
                Integer reminderId = reminderMapper.createOne(reminderAndParticipants.getReminderCreate());

                // Create multiple ReminderParticipant,
                // reminderId is set manually as they are null from getParticipantsCreate()
                List<ReminderParticipantCreate> participantsCreate = reminderAndParticipants.getParticipantsCreate();
                for (ReminderParticipantCreate participantCreate : participantsCreate) {
                    participantCreate.setReminderId(reminderId);
                }
                reminderParticipantMapper.createMany(participantsCreate);

                session.commit();
            }
            
            MessageEmbed embedMsg = new EmbedBuilder()
                .setColor(Color.WHITE)
                .setDescription(String.format(":alarm_clock: %s created a reminder", event.getUser().getEffectiveName()))
                .build();
            event.getHook().sendMessage(
                new MessageCreateBuilder()
                    .addEmbeds(embedMsg)
                    .build()
            ).queue();
        } else if (eventCustomId.label.equals(CONFIRM_COMPLETE_LABEL)) {
            // Use non-ephemeral message to acknowledge the completed reminder later
            event.deferReply(false).queue();
            Integer reminderId = Integer.parseInt(eventCustomId.firstIntrId);

            try (SqlSession session = this.sqlSessionFactory.openSession()) {
                ReminderParticipantMapper mapper = session.getMapper(ReminderParticipantMapper.class);
                ReminderParticipantUpdate update = new ReminderParticipantUpdate(
                    reminderId,
                    event.getUser().getId(),
                    true
                );
                mapper.updateOne(update);
                session.commit();
            }
            
            MessageEmbed embedMsg = new EmbedBuilder()
                .setColor(Color.WHITE)
                .setDescription(String.format(":white_check_mark: %s has completed a reminder", event.getUser().getEffectiveName()))
                .build();
            event.getHook().sendMessage(
                new MessageCreateBuilder()
                    .addEmbeds(embedMsg)
                    .build()
            ).queue();
        }
    }
}
