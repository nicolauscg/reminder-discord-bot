package reminder.discord.bot.java.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import reminder.discord.bot.java.model.DraftReminder;

public interface DraftReminderMapper {
    @Results(id = "draftReminderMap", value = {
        @Result(property = "firstInteractionId", column = "first_interaction_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "guildId", column = "guild_id"),
        @Result(property = "participantUserIds", column = "participant_user_ids"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT * FROM draft_reminder WHERE first_interaction_id = #{firstInteractionId}")
    DraftReminder getOneByFirstInteractionId(@Param("firstInteractionId") String firstInteractionId);
}
