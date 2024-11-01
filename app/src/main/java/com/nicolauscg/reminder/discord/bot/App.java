package com.nicolauscg.reminder.discord.bot;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nicolauscg.reminder.discord.bot.mapper.DraftReminderMapper;
import com.nicolauscg.reminder.discord.bot.mapper.PingMapper;
import com.nicolauscg.reminder.discord.bot.mapper.ReminderMapper;
import com.nicolauscg.reminder.discord.bot.mapper.ReminderParticipantMapper;
import com.zaxxer.hikari.HikariDataSource;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        /*
         * Load properties
         */ 
        
        // Load env file as a resource
        Properties envProps = new Properties();
        InputStream envPropsIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("env.properties");
        if (envPropsIn == null) {
            throw new Exception("Failed to get env.properties resource");
        }
        envProps.load(envPropsIn);
        logger.info("Loaded properties from env.properties resource");

        // Mandatory properties
        String botToken = envProps.getProperty("BOT_TOKEN");
        if (botToken == null) {
            throw new Exception("BOT_TOKEN must be specified");
        }
        String dbUrl = envProps.getProperty("DB_URL");
        if (dbUrl == null) {
            throw new Exception("DB_URL must be specified");
        }
        String dbUsername = envProps.getProperty("DB_USERNAME");
        if (dbUsername == null) {
            throw new Exception("DB_USERNAME must be specified");
        }
        String dbPassword = envProps.getProperty("DB_PASSWORD");
        if (dbPassword == null) {
            throw new Exception("DB_PASSWORD must be specified");
        }
        
        // Optional properties
        // DEV_GUILD_ID should only be specified during development to make slash cmds available immediately,
        // otherwise global slash cmds are created and they take time to be available.
        String devGuildId = envProps.getProperty("DEV_GUILD_ID");

        /*
         * Prepare database connection
         */ 
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dbUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        dataSource.setAutoCommit(false);
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(PingMapper.class);
        configuration.addMapper(DraftReminderMapper.class);
        configuration.addMapper(ReminderMapper.class);
        configuration.addMapper(ReminderParticipantMapper.class);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        
        // Test database connection
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PingMapper mapper = session.getMapper(PingMapper.class);
            mapper.ping();
            logger.info("Checked database connectivity");
        }

        /*
         * Run Discord bot
         */ 
        JDA jdaApi = JDABuilder.createDefault(botToken)
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .addEventListeners(new JDAListener(sqlSessionFactory))
            .build();
        jdaApi.awaitReady();
        logger.info("Bot is ready");

        /*
         * Register slash commands
         */ 
        SlashCommandData createReminderCmd = Commands.slash("createreminder", "Create a reminder")
            .setGuildOnly(true);
        SlashCommandData completeReminderCmd = Commands.slash("completereminder", "Mark a reminder as completed");
        List<SlashCommandData> cmds = Arrays.asList(createReminderCmd, completeReminderCmd);
        if (devGuildId == null) {
            jdaApi.updateCommands().addCommands(cmds).complete();
            logger.info("Registered slash commands as global application commands, " +
                "it may take time for the commands to be available");
        } else {
            Guild devGuild = jdaApi.getGuildById(devGuildId);
            devGuild.updateCommands().addCommands(createReminderCmd, completeReminderCmd).complete();
            logger.info("Registered slash commands as guild application commands");
        }

        /*
         * Run reminder service (handles sending Discord DMs to users)
         */
        ScheduledExecutorService reminderExecSvc = Executors.newSingleThreadScheduledExecutor();
        SendDMForOneDueReminderTask task = new SendDMForOneDueReminderTask(sqlSessionFactory, jdaApi);
        reminderExecSvc.scheduleWithFixedDelay(task, 1, 60, TimeUnit.SECONDS);
    }
}
