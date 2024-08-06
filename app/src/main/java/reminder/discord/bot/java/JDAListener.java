package reminder.discord.bot.java;

import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.Builder;

public class JDAListener extends ListenerAdapter 
{
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
        switch (event.getName())
        {
        case "createreminder":
            // Respond a create reminder cmd with a multi select for reminder's participants  
            event.deferReply(true).queue();
            List<Member> guildMembers = event.getGuild().getMembers();
            Builder selectMenuBuilder = StringSelectMenu.create("create-reminder-participant")
                .setPlaceholder("Select participants of reminder")
                .setMinValues(1)
                .setMaxValues(guildMembers.size());
            for (Member mem : guildMembers) {
                selectMenuBuilder = selectMenuBuilder.addOption(mem.getEffectiveName(), mem.getId());
            }
            event.getHook().sendMessageComponents(ActionRow.of(selectMenuBuilder.build())).queue();
            break;
        default:
            event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("create-reminder-participant")) {
            // For now just print out multi select data
            event.reply("You chose " + event.getValues().toString()).queue();
        }
    }
}
