package bot.antony.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.controller.UserController;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class User implements ServerCommand {
	
	TextChannel channel;
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		UserController usrCntrl = Antony.getUserController();
		List<String> allowedRoles = new ArrayList<>(Arrays.asList("Admin", "Soldat", "Intermorphe"));	//Roles which may use the command
		this.channel = channel;
		
		if(Utils.memberHasRole(member, allowedRoles)) {
			String[] userMessage = message.getContentDisplay().split(" ");
			if (userMessage.length > 1) {
				StringBuilder returnMessage = new StringBuilder();
				
				switch (userMessage[1].toLowerCase()) {
					case "updateall":
						boolean force = false;
						
						if(userMessage.length > 2 && userMessage[2].toLowerCase().equals("force")) {
							force = true;
						}
						
						usrCntrl.updateAllGuildMember(channel.getGuild(), force);
						break;
						
					default:
						printHelp();
						break;
				}
				
				if(returnMessage.length() > 0) {
					channel.sendMessage(returnMessage.toString()).queue();
				}
			} else {
				printHelp();
			}
		}
	}
	
	private void printHelp() {
		channel.sendMessage("Benutzung: " + Antony.getCmdPrefix() + "user (updateall) [force]").queue();
	}
}
