package reminder.discord.bot.java;

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import net.dv8tion.jda.api.entities.Member;
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
import reminder.discord.bot.java.mapper.DraftReminderMapper;
import reminder.discord.bot.java.mapper.ReminderMapper;
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

    private SqlSessionFactory sqlSessionFactory;

    public JDAListener(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        switch (event.getName())
        {
        case "createreminder":
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
            DraftReminder draftReminder = new DraftReminder(firstInteractionId,
                event.getUser().getId(), event.getGuild().getId());
            try (SqlSession session = this.sqlSessionFactory.openSession()) {
                DraftReminderMapper mapper = session.getMapper(DraftReminderMapper.class);
                mapper.createOne(draftReminder);
                session.commit();
            }

            /*
             * Respond with a multi select about reminder's participants 
             */
            List<Member> guildMembers = event.getGuild().getMembers();
            // Put the first interaction id in the custom id, the interaction id will be
            // passed throughout the create reminder interaction chain so later interactions
            // can identify which reminder they are working on
            CustomId customId = new CustomId(SELECT_PARTICIPANT_LABEL, firstInteractionId);
            Builder selectMenuBuilder = StringSelectMenu.create(customId.toString())
                .setPlaceholder("Select participants of reminder (Step 1 of 3)")
                .setMinValues(1)
                .setMaxValues(guildMembers.size());
            for (Member mem : guildMembers) {
                String optVal = String.format("%s%s%s",
                    mem.getId(), ParticipantUserIdsString.ATTR_SEPARATOR, mem.getEffectiveName());
                selectMenuBuilder = selectMenuBuilder.addOption(mem.getEffectiveName(), optVal);
            }
            event.getHook().sendMessageComponents(ActionRow.of(selectMenuBuilder.build())).queue();
            break;
        default:
            event.reply(String.format("Command %s is not recognized", event.getName()))
                .setEphemeral(true).queue();
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
                DraftReminder draftReminder = mapper.getOneByFirstInteractionId(firstInteractionId);
                draftReminder.setParticipantUserIds(event.getValues());
                mapper.updateOne(draftReminder);
                session.commit();
            }

            // Looks like deferReply() cannot be used when replying with a modal 
            TextInput title = TextInput.create(MODAL_TITLE_LABEL, "Title", TextInputStyle.SHORT)
                .setPlaceholder("Title e.g. Dinner at ...")
                .setMaxLength(50)
                .setRequired(true)
                .build();
            TextInput content = TextInput.create(MODAL_CONTENT_LABEL, "Content", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Content e.g. List who owes how much for a shared expense")
                    .setMaxLength(500)
                    .setRequired(true)
                    .build();
            CustomId customId = new CustomId(MODAL_LABEL, eventCustomId.firstIntrId);
            Modal modal = Modal.create(customId.toString(), "Enter reminder details (Step 2 of 3)")
                    .addComponents(ActionRow.of(title), ActionRow.of(content))
                    .build();
            event.replyModal(modal).queue();
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
                DraftReminder draftReminder = mapper.getOneByFirstInteractionId(firstInteractionId);
                participants = draftReminder.getParticipantUserIdsAsClass();
                draftReminder.setTitle(title);
                draftReminder.setDescription(content);
                mapper.updateOne(draftReminder);
                session.commit();
            }

            String summary = "Confirm reminder details (Step 3 of 3)" +
                "\nParticipants: " + String.join(", ", participants.getNames()) +
                "\nTitle: " + title +
                "\nContent: " + content;
            CustomId customId = new CustomId(CONFIRM_LABEL, eventCustomId.firstIntrId);
            event.getHook().sendMessage(summary).queue((message) -> {
                event.getHook().editOriginalComponents(
                    ActionRow.of(Button.primary(customId.toString(), "Create reminder"))).queue();
            });
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
                DraftReminder draftReminder = draftReminderMapper.getOneByFirstInteractionId(firstInteractionId);

                Reminder reminder = Reminder.fromDraft(draftReminder);
                ReminderMapper reminderMapper = session.getMapper(ReminderMapper.class);
                reminderMapper.createOne(reminder);

                session.commit();
            }
            
            event.getHook().sendMessage(
                String.format("%s created a reminder", event.getUser().getEffectiveName())).queue();
        }
    }
}
