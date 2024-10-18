package reminder.discord.bot.java.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.jdbc.SQL;

import reminder.discord.bot.java.dto.DraftReminderCreate;
import reminder.discord.bot.java.dto.DraftReminderUpdate;
import reminder.discord.bot.java.model.DraftReminder;

public interface DraftReminderMapper {
    @Select("SELECT 1")
    public Integer testConnection();

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
    public DraftReminder getOneByFirstInteractionId(@Param("firstInteractionId") String firstInteractionId);

    @ResultMap("draftReminderMap")
    @Insert("INSERT INTO draft_reminder(first_interaction_id, user_id, guild_id, updated_at) " +
        "VALUES (#{firstInteractionId}, #{userId}, #{guildId}, current_timestamp)")
    public void createOne(DraftReminderCreate create);

    @ResultMap("draftReminderMap")
    @UpdateProvider(type = DraftReminderMapper.class, method = "updateOneSql")
    public void updateOne(DraftReminderUpdate update);

    public static String updateOneSql(final DraftReminderUpdate update) {
        return new SQL(){{
            UPDATE("draft_reminder");
            if (update.getParticipantUserIds() != null) {
                SET("participant_user_ids = #{participantUserIds}");
            }
            if (update.getTitle() != null) {
                SET("title = #{title}");
            }
            if (update.getDescription() != null) {
                SET("description = #{description}");
            }
            SET("updated_at = current_timestamp");
            WHERE("first_interaction_id = #{firstInteractionId}");
        }}.toString();
    }
}
