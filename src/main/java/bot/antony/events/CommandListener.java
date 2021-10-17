package bot.antony.events;

import bot.antony.Antony;
import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		String message = event.getMessage().getContentDisplay();
		
		//check which channel ...
		if(event.isFromType(ChannelType.TEXT)) {
			final TextChannel channel = event.getTextChannel();
			
			if(event.getMember() != null) {
				UserData user = Utils.loadUserData(event.getMember());
				user.setLastOnline(System.currentTimeMillis());
				Utils.storeUserData(user, event.getGuild());
			}
			
			//!cmd arg0 arg1 arg2 ...
			if(message.startsWith(Antony.getCmdPrefix())) {
				String[] args = message.substring(Antony.getCmdPrefix().length()).split(" ");
				
				if(args.length > 0) {
					if(!Antony.getCmdMan().perform(args[0], event.getMember(), channel, event.getMessage())) {
						// TODO What if command is unknown?
						// System.out.println("Unknown Command: '" + event.getMessage() + "'");
					} else {
						StringBuilder logMessage = new StringBuilder();
						logMessage.append("User \"" + event.getMember().getUser().getName() + "\" (UID: " + event.getMember().getUser().getId() + ") ");
						logMessage.append("used command \"" + message + "\" ");
						logMessage.append("on server \"" + event.getGuild().getName() + "\" (GID: " + event.getGuild().getId() + ") ");
						logMessage.append("in Channel \"#" + channel.getName() + "\" (CID: " + channel.getId() + ")");
						Antony.getLogger().info(logMessage.toString());
					}
				}
			}
		}
	}
}
