import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class main {

    public static JDA jda;
    public static Connection conn;


    public static void main(String[] args) throws LoginException {
        jda = JDABuilder.createDefault(Discordtoken)
                .addEventListeners(new Discordclass())
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOTE)
                .build();

        try {

            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();


            conn = DriverManager.getConnection(dbUrl + "?user=" + username + "&password=" + password + "&sslmode=require");
            System.out.println("Connected to sql");
        } catch (SQLException | URISyntaxException throwables) {
            throwables.printStackTrace();
            System.out.println("ERROR IN SQL.");
        }
    }
}
