package com.nicolauscg.reminder.discord.bot.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.nicolauscg.reminder.discord.bot.dto.ReminderCreate;
import com.nicolauscg.reminder.discord.bot.model.Reminder;

public interface ReminderMapper {
    @Results(id = "reminderMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "guildId", column = "guild_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "isNotifiedAfterComplete", column = "is_notified_after_complete"),
        @Result(property = "createdAt", column = "created_at")
    })
    @Select({"SELECT * FROM reminder WHERE id = #{id}"})
    public Reminder getOneById(Integer id);

    // Returned reminders are limited to 20 as the max items in a Discord string select menu is 25
    @ResultMap("reminderMap")
    @Select({"SELECT r.* FROM reminder r INNER JOIN reminder_participant rp ON r.id = rp.reminder_id",
        "WHERE rp.user_id = #{participantUserId} AND rp.is_complete = false ORDER BY r.created_at LIMIT 20"})
    public List<Reminder> getManyUncompletedReminderByParticipantUserId(String participantUserId);

    // @Select is used instead of @Insert as the returning clause is used to return the inserted row id.
    @Select({"INSERT INTO reminder(user_id, guild_id, title, description, is_notified_after_complete, created_at)",
        "VALUES (#{userId}, #{guildId}, #{title}, #{description}, #{isNotifiedAfterComplete}, current_timestamp)",
        "RETURNING id"})
    public Integer createOne(ReminderCreate create);
}
