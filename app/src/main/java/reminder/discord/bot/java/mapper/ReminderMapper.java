package reminder.discord.bot.java.mapper;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import reminder.discord.bot.java.dto.ReminderCreate;

public interface ReminderMapper {
    // @Select is used instead of @Insert as the returning clause is used to return the inserted row id.
    @Results(id = "reminderMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "guildId", column = "guild_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "isNotifiedAfterComplete", column = "is_notified_after_complete"),
        @Result(property = "createdAt", column = "created_at")
    })
    @Select({"INSERT INTO reminder(user_id, guild_id, title, description, is_notified_after_complete, created_at)",
        "VALUES (#{userId}, #{guildId}, #{title}, #{description}, #{isNotifiedAfterComplete}, current_timestamp)",
        "RETURNING id"})
    public Integer createOne(ReminderCreate create);
}
