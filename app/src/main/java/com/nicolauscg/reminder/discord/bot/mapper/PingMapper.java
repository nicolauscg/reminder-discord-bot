package com.nicolauscg.reminder.discord.bot.mapper;

import org.apache.ibatis.annotations.Select;

public interface PingMapper {
    @Select("SELECT 1")
    public Integer ping();
}
