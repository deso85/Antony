package bot.antony.events;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		String message = event.getMessage().getContentDisplay();
		
		//check which channel ...
		if(event.isFromType(ChannelType.TEXT) || event.isFromThread() &&
				!event.getAuthor().equals(event.getJDA().getSelfUser())) {
			final GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
			
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
