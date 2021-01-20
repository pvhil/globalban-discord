import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Discordclass extends ListenerAdapter implements EventListener {
    public String bantext = "haha no";

    //collect bans! but not like this
    public void onGuildMemberRemove(GuildMemberRemoveEvent event){
        System.out.println("working 1");
        String userID = event.getUser().getId();
        int bans = 0;

        try {
            event.getGuild().retrieveBanById(event.getUser().getId()).queue(ban -> System.out.println(ban));
        }catch (Exception e){
            System.out.println("not a ban!");
            return;
        }


        try {
            Statement stmt = main.conn.createStatement();
            ResultSet rs9 = stmt.executeQuery("SELECT * FROM bansystem WHERE userid='"+userID+"'");

            if(rs9.next()){
                bans = rs9.getInt(2);
                System.out.println("Getting there");
            }else return;
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println(bans);
        int rlBan = ++bans;
        System.out.println(rlBan);
        try {
            Statement stmt = main.conn.createStatement();
            stmt.execute("INSERT INTO bansystem(userid,bans) VALUES ('"+userID +"',"+rlBan +") ON CONFLICT ON CONSTRAINT bansystem_pkey DO UPDATE SET bans=EXCLUDED.bans;");
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    //get bans on join
    public void onGuildMemberJoin(GuildMemberJoinEvent event){
        String userID = event.getUser().getId();
        String GuildID = event.getGuild().getId();
        String username = event.getUser().getName();
        String logChannel = null;
        int maxBans = 0;
        int bans = 0;
        String allowed = "";

        try {
            Statement stmt = main.conn.createStatement();
            ResultSet rs9 = stmt.executeQuery("SELECT * FROM bansystem WHERE userid='"+userID+"'");

            if(rs9.next()){
                bans = rs9.getInt(2);
                stmt.close();
                rs9.close();
            }else {
                stmt.close();
                rs9.close();
                return;
            }

            stmt = main.conn.createStatement();
            ResultSet rs8 = stmt.executeQuery("SELECT * FROM bansystemguild WHERE guildid='"+GuildID+"'");

            if(rs8.next()){
                logChannel = rs8.getString(2);
                maxBans = rs8.getInt(3);
                allowed = rs8.getString(4);
                if(allowed.contains(userID)){
                    stmt.close();
                    rs8.close();
                    return;
                }
            }else{
                return;
            }
            stmt.close();
            rs8.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        if(maxBans <= bans){
            int finalBans = bans;
            event.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("Hello, Global Ban System Here!\nBecause you got banned "+ finalBans +" times, you are not able to join "+event.getGuild().getName()+"!\nThe Server admins already were notified and will decide if you are able to join!").queue());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            event.getMember().kick().queue();
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("A new User joined with existing bans!")
                    .setDescription("User "+username+" joined with "+bans+" existing bans!\nBecause of his bans, he automatically got kicked!\nYou can allow him to join again with .allow "+event.getMember().getUser().getId())
                    .setFooter("An User gets automatically kicked with "+maxBans+" bans!");
            main.jda.getGuildById(GuildID).getTextChannelById(logChannel).sendMessage(embed.build()).queue();
            return;

        }else if(bans > 0){

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("A new User joined with existing bans!")
                    .setDescription("User " + username + " joined with " + bans + " existing bans!")
                    .setFooter("An User gets automatically kicked with "+maxBans+" bans!");
            main.jda.getGuildById(GuildID).getTextChannelById(logChannel).sendMessage(embed.build()).queue();
        }

    }
    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        String[] args = event.getMessage().getContentRaw().split("\\s+");
        String GuildID = event.getGuild().getId();
        if(event.getAuthor().isBot()||!event.getMember().hasPermission(Permission.MANAGE_ROLES)){
            return;
        }
        if(args[0].equalsIgnoreCase(".bansystem") && args.length ==1){
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Thanks for using the GlobalBan System!")
                    .setDescription("To setup the Bot, please provide a log Channel and a max acceptable ban value!\nExample:\n.bansystem #logchannel 5")
                    .setFooter("Bot made by phil#0346");
            event.getChannel().sendMessage(embed.build()).queue();
        }
        if(args.length ==3 && args[0].equalsIgnoreCase(".bansystem")){
            List<TextChannel> channels = event.getMessage().getMentionedChannels();
            if (args[2] == null || args[2].length() == 0) {
                event.getChannel().sendMessage("invalid input!!!").queue();
                return;
            }
            for (char c : args[2].toCharArray()) {
                if (!Character.isDigit(c)) {
                    event.getChannel().sendMessage("invalid input!!!").queue();
                    return;
                }
            }
            int maxacc = Integer.parseInt(args[2]);
            if(channels.size() != 1 || maxacc == 0 ){
                event.getChannel().sendMessage("invalid input!!!").queue();
                return;
            }
            String channelID = channels.get(0).getId();
            try {
                Statement stmt = main.conn.createStatement();
                stmt.execute("INSERT INTO bansystemguild(guildid,logchannel,maxbans,allowed) VALUES ('"+GuildID +"','"+channelID+"',"+maxacc +",' ') ON CONFLICT ON CONSTRAINT bansystemguild_pkey DO UPDATE SET maxbans=EXCLUDED.maxbans;");
                stmt.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Bot is working!!")
                    .setDescription("To renew settings, just reinvite the bot!")
                    .setFooter("Bot made by phil#0346");
            event.getChannel().sendMessage(embed.build()).queue();
        }
        if(args[0].equalsIgnoreCase(".allow")){
            if(args.length != 2){
                event.getChannel().sendMessage("Invalid input!!").queue();
            }
            else {
                try {
                    Statement stmt = main.conn.createStatement();
                    stmt.execute("INSERT INTO bansystemguild(guildid,allowed) VALUES ('"+GuildID +"','"+args[1]+"') ON CONFLICT ON CONSTRAINT bansystemguild_pkey DO UPDATE SET allowed=EXCLUDED.allowed;");
                    stmt.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                event.getChannel().sendMessage("The User with the Discord ID "+args[1]+" is now always allowed to join!").queue();


            }
        }
    }



}
