package reminder.discord.bot.java.mapper;

import java.time.Instant;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.jdbc.SQL;

import reminder.discord.bot.java.dto.ReminderParticipantCreate;
import reminder.discord.bot.java.dto.ReminderParticipantUpdate;
import reminder.discord.bot.java.model.ReminderParticipant;

public interface ReminderParticipantMapper {
    @Results(id = "reminderParticipantMap", value = {
        @Result(property = "reminderId", column = "reminder_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "isComplete", column = "is_complete"),
        @Result(property = "remindedCount", column = "reminded_count"),
        @Result(property = "lastRemindedAt", column = "last_reminded_at"),
        @Result(property = "nextRemindAt", column = "next_remind_at")
    })
    @Select({"SELECT * FROM reminder_participant WHERE is_complete = false AND",
        "next_remind_at < '${nextRemindAt}'::timestamptz ORDER BY next_remind_at ASC LIMIT 1"})
    public ReminderParticipant getOneToRemind(Instant nextRemindAt);

    // An Insert with <script> is used to be able to do multi-row insert from a list
    // with <foreach>, looks like it is not available in the MyBatis SQL builder class.
    @ResultMap("reminderParticipantMap")
    @Insert({"<script>",
        "insert into reminder_participant",
        "   (reminder_id, user_id, is_complete, reminded_count, last_reminded_at, next_remind_at) values",
        "   <foreach item=\"item\" collection=\"list\" separator=\",\">",
        "       (#{item.reminderId}, #{item.userId}, #{item.isComplete}, #{item.remindedCount}, ",
        "           #{item.lastRemindedAt}, #{item.nextRemindAt})",
        "   </foreach>",
        "</script>"})
    public void createMany(List<ReminderParticipantCreate> createList);

    @ResultMap("reminderParticipantMap")
    @UpdateProvider(type = ReminderParticipantMapper.class, method = "updateOneSql")
    public void updateOne(ReminderParticipantUpdate update);

    public static String updateOneSql(final ReminderParticipantUpdate update) {
        return new SQL(){{
            UPDATE("reminder_participant");
            if (update.getIsComplete() != null) {
                SET("is_complete = #{isComplete}");
            }
            if (update.getRemindedCount() != null) {
                SET("reminded_count = #{remindedCount}");
            }
            if (update.getLastRemindedAt() != null) {
                SET("last_reminded_at = #{lastRemindedAt}");
            }
            if (update.getNextRemindAt() != null) {
                SET("next_remind_at = #{nextRemindAt}");
            }
            WHERE("reminder_id = #{reminderId}");
            WHERE("user_id = #{userId}");
        }}.toString();
    }
}
