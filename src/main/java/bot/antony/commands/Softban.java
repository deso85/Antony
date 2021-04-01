package bot.antony.commands;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.guild.user.UserData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Softban implements ServerCommand {
	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		
		List<String> allowedRoles = new ArrayList<String>();
		
		//Roles which may use the command
		allowedRoles.add("Admin");
		allowedRoles.add("Soldat");
		allowedRoles.add("Intermorphe");
		
		boolean mayUse = false;
		for(Role role: m.getRoles()) {
			if(allowedRoles.contains(role.getName())) {
				mayUse = true;
			}
		}
		
		if(mayUse) {
			String[] userMessage = message.getContentDisplay().split(" ");
			if (userMessage.length > 1) {
				switch (userMessage[1].toLowerCase()) {
				case "add":
					if (userMessage.length > 3) {
						UserData user = new UserData(userMessage[2], userMessage[3]);
						if(Antony.getSoftbanController().ban(user)) {
							m.getGuild().getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage("🔨 User manually soft banned by " + m.getUser().getAsMention()).queue();
							Date date = new Date(System.currentTimeMillis());
							SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
							EmbedBuilder eb = new EmbedBuilder()
									.setColor(Color.red)
									.setAuthor(userMessage[3] + " | ID: " + userMessage[2])
									.setDescription(message.getContentDisplay())
									.addField("#" + channel.getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + m.getGuild().getId() + "/" + channel.getId() + "/" + message.getId() + ")**", false)
									.setFooter(formatter.format(date));
							m.getGuild().getTextChannelById(Antony.getAntonyLogChannelId()).sendMessage(eb.build()).queue();
						}
					}
					break;
				case "remove":
					if (userMessage.length > 3) {
						UserData user = new UserData(userMessage[2], userMessage[3]);
						if(Antony.getSoftbanController().unban(user)) {
							//System.out.println("Unbanned user");
						}
					}
					break;
				case "list":
					StringBuilder sb = new StringBuilder();
					if(Antony.getSoftbanController().getBannedUser().size() > 0) {
						sb.append("Folgende User sind softbanned:\n");
						for(UserData user: Antony.getSoftbanController().getBannedUser()) {
							sb.append("- " + user.getName() + " (" + user.getId() + ")");
							//System.out.println("- " + user.getName() + " (" + user.getId() + ")");
						}
					} else {
						sb.append("Es sind keine User softbanned.");
					}
					channel.sendMessage(sb.toString()).queue();
					break;
				case "clear":
					Antony.getSoftbanController().setBannedUser(new ArrayList<UserData>());
					Antony.getSoftbanController().persistData();
					break;
				}
			}
		}
	}
}
