package bot.antony.events.softban;

import bot.antony.Antony;
import bot.antony.guild.user.UserData;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SoftbanFilterListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

		//String message = event.getMessage().getContentDisplay();

		// check which channel ...
		if (event.isFromType(ChannelType.TEXT)) {
			UserData user = new UserData(event.getAuthor().getId(), event.getAuthor().getName());
			if(Antony.getSoftbanController().getBannedUser().contains(user)) {
				event.getMessage().delete().queue();
				Antony.getLogger().info("Message in channel #" + event.getChannel().getName() + " sent by user \"" + event.getAuthor().getName() + "\" (UID: " + event.getAuthor().getId() + ") "
					+ "deleted because this user is softbanned.");
			}
		}
	}
}
