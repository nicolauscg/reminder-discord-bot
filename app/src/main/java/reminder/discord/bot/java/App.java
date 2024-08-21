package reminder.discord.bot.java;

import java.io.InputStream;
import java.util.Properties;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.postgresql.ds.PGSimpleDataSource;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import reminder.discord.bot.java.mapper.DraftReminderMapper;

public class App {
    public static void main(String[] args) throws Exception {
        /*
         * Load properties
         */ 
        InputStream envPropsIn = Thread.currentThread().getContextClassLoader().getResourceAsStream("env.properties");
        if (envPropsIn == null) {
            throw new Exception("Failed to get env.properties resource");
        }
        Properties envProps = new Properties();
        envProps.load(envPropsIn);
        String botToken = envProps.getProperty("BOT_TOKEN");
        if (botToken == null) {
            throw new Exception("BOT_TOKEN must be specified");
        }
        // For now always require DEV_GUILD_ID
        String devGuildId = envProps.getProperty("DEV_GUILD_ID");
        if (devGuildId == null) {
            throw new Exception("DEV_GUILD_ID must be specified");
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

        /*
         * Prepare database connection
         */ 
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(dbUrl);
        dataSource.setUser(dbUsername);
        dataSource.setPassword(dbPassword);
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(DraftReminderMapper.class);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        /*
         * Run Discord bot
         */ 
        JDA api = JDABuilder.createDefault(botToken)
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .addEventListeners(new JDAListener())
            .build();
        api.awaitReady();
        System.out.println("Bot is ready");

        /*
         * Register slash commands
         */ 
        Guild devGuild = api.getGuildById(devGuildId);
        SlashCommandData createReminderCmd = Commands.slash("createreminder", "Creates a reminder")
            .setGuildOnly(true);
        devGuild.updateCommands().addCommands(createReminderCmd).queue();
    }
}
