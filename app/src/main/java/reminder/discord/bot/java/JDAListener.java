package reminder.discord.bot.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

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

public class JDAListener extends ListenerAdapter 
{
    static final String SELECT_PARTICIPANT_LABEL = "create-reminder-participant";
    static final String MODAL_LABEL = "create-reminder-modal";
    static final String MODAL_TITLE_LABEL = "create-reminder-title";
    static final String MODAL_CONTENT_LABEL = "create-reminder-content";
    static final String CONFIRM_LABEL = "create-reminder-confirm";

    // Use a map for now instead of a database
    Map<String, List<String>> intrIdToParticipants;

    public JDAListener() {
        intrIdToParticipants = new ConcurrentHashMap<>();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        switch (event.getName())
        {
        case "createreminder":
            // Respond a create reminder cmd with a multi select for reminder's participants  
            event.deferReply(true).queue();
            List<Member> guildMembers = event.getGuild().getMembers();
            // Put the first interaction id in the custom id, the interaction id will be
            // passed throughout the create reminder interaction chain so later interactions
            // can identify which reminder they are working on
            CustomId customId = new CustomId(SELECT_PARTICIPANT_LABEL, event.getId());
            Builder selectMenuBuilder = StringSelectMenu.create(customId.toString())
                .setPlaceholder("Select participants of reminder (Step 1 of 3)")
                .setMinValues(1)
                .setMaxValues(guildMembers.size());
            for (Member mem : guildMembers) {
                String optVal = String.format("%s,%s", mem.getId(), mem.getEffectiveName());
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
            this.intrIdToParticipants.put(eventCustomId.intrId, event.getValues());

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
            CustomId customId = new CustomId(MODAL_LABEL, eventCustomId.intrId);
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
            String title = event.getValue(MODAL_TITLE_LABEL).getAsString();
            String content = event.getValue(MODAL_CONTENT_LABEL).getAsString();
            List<String> participants = this.intrIdToParticipants.getOrDefault(eventCustomId.intrId, new ArrayList<>());

            String summary = "Confirm reminder details (Step 3 of 3)" +
                "\nParticipants: " + participants.toString() +
                "\nTitle: " + title +
                "\nContent: " + content;
            CustomId customId = new CustomId(CONFIRM_LABEL, eventCustomId.intrId);
            event.getHook().sendMessage(summary).queue((message) -> {
                event.getHook().editOriginalComponents(
                    ActionRow.of(Button.primary(customId.toString(), "Confirm"))).queue();
            });
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        CustomId evenCustomId = new CustomId(event.getComponentId());
        if (evenCustomId.label.equals(CONFIRM_LABEL)) {
            event.reply("Reminder created (not yet implemented)").queue();
        }
    }
}
