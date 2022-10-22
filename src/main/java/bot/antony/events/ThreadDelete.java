package bot.antony.events;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ThreadDelete extends ListenerAdapter {

	Guild guild;
	
	@Override
	public void onChannelDelete(ChannelDeleteEvent event) {
		guild = event.getGuild();
		StringBuilder logMessage = new StringBuilder();
		
		if(event.getChannel().getType().isThread()) {
			ThreadChannel tc = (ThreadChannel) event.getChannel();
			logMessage.append("Thread \"" + tc.getAsMention() + "\" has been deleted.");

		}
		
		if(!logMessage.toString().isEmpty()) {
			Antony.getLogger().info(logMessage.toString());
		}
	}
}
