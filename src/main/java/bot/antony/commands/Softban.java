package bot.antony.commands;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import bot.antony.Antony;
import bot.antony.commands.types.IServerCommand;
import bot.antony.events.softban.UserDataSB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Softban implements IServerCommand {
	
	Guild guild;
	TextChannel channel;
	TextChannel responseChannel;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		this.channel = channel;
		guild = channel.getGuild();
		responseChannel = Antony.getGuildController().getLogChannel(channel.getGuild());
		
		String[] userMessage = message.getContentDisplay().split(" ");
		if (userMessage.length > 1) {
			StringBuilder sb = new StringBuilder();
			switch (userMessage[1].toLowerCase()) {
			case "add":
				if (userMessage.length > 3) {
					UserDataSB user = new UserDataSB(userMessage[2], userMessage[3]);
					if(Antony.getSoftbanController().ban(user)) {
						if(responseChannel != null) {
							responseChannel.sendMessage("🔨 User \"" + userMessage[3] + "\" manually soft banned by " + member.getUser().getAsMention()).complete();
						}
						Date date = new Date();
						SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
						EmbedBuilder eb = new EmbedBuilder()
								.setColor(Color.red)
								.setAuthor(userMessage[3] + " | ID: " + userMessage[2])
								.setDescription(message.getContentDisplay())
								.addField("#" + channel.getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + member.getGuild().getId() + "/" + channel.getId() + "/" + message.getId() + ")**", false)
								.setFooter(formatter.format(date));
						if(responseChannel != null) {
							responseChannel.sendMessageEmbeds(eb.build()).complete();
						}
					} else {
						sb.append("User \"" + userMessage[3] + "\" konnte nicht gebannt werden.");
					}
				}
				break;
			case "remove":
				if (userMessage.length > 3) {
					UserDataSB user = new UserDataSB(userMessage[2], userMessage[3]);
					if(Antony.getSoftbanController().unban(user)) {
						sb.append("User \"" + userMessage[3] + "\" wurde entbannt.");
					} else {
						sb.append("User \"" + userMessage[3] + "\" konnte nicht entbannt werden.");
					}
				}
				break;
			case "list":
				
				if(Antony.getSoftbanController().getBannedUser().size() > 0) {
					sb.append("Folgende User sind softbanned:\n");
					for(UserDataSB user: Antony.getSoftbanController().getBannedUser()) {
						sb.append("- " + user.getName() + " (" + user.getId() + ")\n");
					}
				} else {
					sb.append("Es sind keine User softbanned.");
				}
				
				break;
			case "reload":
				Antony.getSoftbanController().initData();
				channel.sendMessage("Die Liste wurde mit " + Antony.getSoftbanController().getBannedUser().size() + " Einträgen neu geladen.").queue();
				break;
				
			case "clear":
				Antony.getSoftbanController().setBannedUser(new ArrayList<UserDataSB>());
				Antony.getSoftbanController().persistData();
				break;
				
			default:
				printHelp();
				break;
				
			}
			if(sb.length() > 0) {
				channel.sendMessage(sb.toString()).queue();
			}
		} else {
			printHelp();
		}
	}
	
	private void printHelp() {
		channel.sendMessage("Benutzung: " + Antony.getCmdPrefix() + "softban (add | remove | list | reload | clear) [ID NAME]").queue();
	}
}
