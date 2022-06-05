package bot.antony.events;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ThreadCreate extends ListenerAdapter {

	Guild guild;
	
	@Override
	public void onChannelCreate(ChannelCreateEvent event) {
		guild = event.getGuild();
		StringBuilder logMessage = new StringBuilder();
		
		if(event.getChannel().getType().isThread()) {
			ThreadChannel tc = (ThreadChannel) event.getChannel();
			logMessage.append("Thread \"" + tc.getAsMention() + "\" has been created.");
			if(tc.isPublic()) {
				if(!tc.isJoined()) {
					logMessage.append(" Joined it.");
					tc.join().queue();
				}
			} else {
				logMessage.append(" It is private - Can't join it.");
			}
		}
		
		if(!logMessage.toString().isEmpty()) {
			Antony.getLogger().info(logMessage.toString());
		}
	}
}
