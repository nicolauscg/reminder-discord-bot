package reminder.discord.bot.java.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;

import reminder.discord.bot.java.model.Reminder;

public interface ReminderMapper {
    @Results(id = "reminderMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "guildId", column = "guild_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "isNotifiedAfterComplete", column = "is_notified_after_complete"),
        @Result(property = "createdAt", column = "created_at")
    })
    @Insert("INSERT INTO reminder(user_id, guild_id, title, description, is_notified_after_complete, created_at) " +
        "VALUES (#{userId}, #{guildId}, #{title}, #{description}, false, current_timestamp)")
    public void createOne(Reminder reminder);
}
