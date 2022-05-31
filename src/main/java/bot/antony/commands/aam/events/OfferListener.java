package bot.antony.commands.aam.events;

import java.util.Arrays;

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
		//check which channel ...
		if(event.isFromType(ChannelType.TEXT)) {
			Guild guild = event.getGuild();
			User user = event.getAuthor();
			String userString = user.getAsMention() + " (ID: " + user.getId() + ")";
			
			TextChannel offerChan = Antony.getGuildController().getValidChannel(guild, Arrays.asList(543512785171251201L, 778960516461625347L)); //Prod, Test
			TextChannel offerLogChan = Antony.getGuildController().getValidChannel(guild, Arrays.asList(726444104627519549L, 778960515631415324L)); //Prod, Test
			
			//Is a new offer
			if(event.getTextChannel() == offerChan) {
				//TODO: may the user add a new offer?
				
				if(offerLogChan != null) {
					offerLogChan.sendMessage(userString).queue();
				}
			}
		}
	}
}
