package reminder.discord.bot.java.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;

import reminder.discord.bot.java.dto.ReminderParticipantCreate;

public interface ReminderParticipantMapper {
    // An Insert with <script> is used to be able to do multi-row insert from a list
    // with <foreach>, looks like it is not available in the MyBatis SQL builder class.
    @Results(id = "reminderParticipantMap", value = {
        @Result(property = "reminderId", column = "reminder_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "isComplete", column = "is_complete"),
        @Result(property = "remindedCount", column = "reminded_count"),
        @Result(property = "lastRemindedAt", column = "last_reminded_at"),
        @Result(property = "nextRemindAt", column = "next_remind_at")
    })
    @Insert({"<script>",
        "insert into reminder_participant",
        "   (reminder_id, user_id, is_complete, reminded_count, last_reminded_at, next_remind_at) values",
        "   <foreach item=\"item\" collection=\"list\" separator=\",\">",
        "       (#{item.reminderId}, #{item.userId}, #{item.isComplete}, #{item.remindedCount}, ",
        "           #{item.lastRemindedAt}, #{item.nextRemindAt})",
        "   </foreach>",
        "</script>"})
    public void createMany(List<ReminderParticipantCreate> createList);
}
