package bot.antony.commands.aam.events;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OfferListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		User user = event.getAuthor();
		String userString = user.getAsMention() + " (ID: " + user.getId() + ")";
				
		//check which channel ...
		if(event.isFromType(ChannelType.TEXT)) {
			final TextChannel channel = event.getTextChannel();
			Guild guild = event.getGuild();
			
			//TODO: Use Variable
			long offerChannelId = 778960516461625347L;		//is test ID
			long offerLogChannelId = 778960515631415324L;	//is test ID
			
			if(Antony.isProdStage()) {
				offerChannelId = 543512785171251201L;
				offerLogChannelId = 726444104627519549L;
			}
			
			//Is a new offer
			if(channel.getIdLong() == offerChannelId) {
				//TODO: may the user add a new offer?
				
				guild.getTextChannelById(offerLogChannelId).sendMessage(userString).queue();
			}
		}
	}
}
