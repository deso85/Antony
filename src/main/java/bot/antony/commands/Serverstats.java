package bot.antony.commands;

import java.util.ArrayList;

import bot.antony.Antony;
import bot.antony.commands.types.IServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Serverstats implements IServerCommand {

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		Guild guild = channel.getGuild();
		
		//Prepare server stats
		int boostCount = guild.getBoostCount();
		String boostTier = guild.getBoostTier().toString();
		int emoteCount = guild.getEmotes().size();
		int maxEmoteCount = guild.getMaxEmotes();
		
		int categoryCount = guild.getCategories().size();
		int textChannelCount = guild.getTextChannels().size();
		int voiceChannelCount = guild.getVoiceChannels().size();
		int channelCount = guild.getChannels().size();
		
		int offlineMemberCount = 0;
		int dndMemberCount = 0;
		int idleMemberCount = 0;
		int onlineMemberCount = 0;
		int botCount = 0;
		int realMemberCount = 0;
		for(Member mbr : guild.getMembers()) {
			if(mbr.getUser().isBot()) {
				botCount++;
			} else {
				realMemberCount++;
			}
			
			if(mbr.getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
				offlineMemberCount++;
			}
			if(mbr.getOnlineStatus().equals(OnlineStatus.DO_NOT_DISTURB)) {
				dndMemberCount++;
			}
			if(mbr.getOnlineStatus().equals(OnlineStatus.IDLE)) {
				idleMemberCount++;
			}
			if(mbr.getOnlineStatus().equals(OnlineStatus.ONLINE)) {
				onlineMemberCount++;
			}
		}
		int userCount = guild.getMemberCount();
		
		int roleCount = guild.getRoles().size();
		ArrayList<String> roles = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		for(Role role : guild.getRoles()) {
			int userWithRoleCount = guild.getMembersWithRoles(role).size();
			String userWithRoleCountAsString = "" + userWithRoleCount;
			if((sb.toString().length() + role.getAsMention().length() + userWithRoleCountAsString.length() + 4) > 1014) {
				roles.add(sb.toString());
				sb = new StringBuilder();
			}
			sb.append("\n" + role.getAsMention() + ": " + userWithRoleCount);
		}
		roles.add(sb.toString());
		
		/* ----------------------------------------
		 * Print Server Stats
		 * ----------------------------------------
		 */
		EmbedBuilder eb = new EmbedBuilder().setTitle("***" + guild.getName() + "***")
				.setColor(Antony.getBaseColor())
				.setDescription("Im folgenden werden Server-Statistiken ausgegeben.")
				.setThumbnail(guild.getIconUrl())
				.setFooter("Antony Version " + Antony.getVersion());
		
		//Basics
		eb.addField("Allgemeines",
				"Server-Boosts: " + boostCount
				+ "\nBoost Tier: " + boostTier
				+ "\nEmote Count: " + emoteCount + " / " + maxEmoteCount,
				false);
		
		//Categories and Channels
		eb.addField("Anzahl Kategorien und Kanäle",
				"Kategorien: " + categoryCount
				+ "\nText-Kanäle: " + textChannelCount
				+ "\nVoice-Kanäle: " + voiceChannelCount
				+ "\nGesamt: " + channelCount + " / 500",
				false);
		
		//User
		eb.addField("User",
				"User: " + realMemberCount
				+ "\nBots: " + botCount
				+ "\nGesamt: " + userCount
				+ "\n\nUser Online: " + onlineMemberCount
				+ "\nUser DND: " + dndMemberCount
				+ "\nUser Idle: " + idleMemberCount
				+ "\nUser Offline: " + offlineMemberCount,
				false);
		
		//Roles
		if(roles.size() == 1) {
			eb.addField("Rollen",
					"Rollen: " + roleCount
					+ "\n" + roles.get(0),
					false);
		} else {
			int forCounter = 1;
			for(String roleText : roles) {
				String msgContent = "";
				if(forCounter == 1) {
					msgContent = "Rollen: " + roleCount + "\n";
				}
				msgContent += roleText;
				
				eb.addField("Rollen (" + forCounter + "/" + roles.size() + ")",
						msgContent,
						false);
				forCounter++;
			}
		}
		channel.sendMessageEmbeds(eb.build()).queue();
		
	}

}
