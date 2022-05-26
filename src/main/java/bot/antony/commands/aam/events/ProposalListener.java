package bot.antony.commands.aam.events;

import bot.antony.Antony;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ProposalListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
				
		//check which channel ...
		if(event.isFromType(ChannelType.TEXT)) {
			if(event.getMessage().getType() != MessageType.CHANNEL_PINNED_ADD) {
				final TextChannel channel = event.getTextChannel();
				Guild guild = event.getGuild();
				
				//TODO: Use Variable
				long proposalChannelId = 778960515895918632L;	//is test ID
				if(Antony.isProdStage()) {
					proposalChannelId = 650687328863518740L;
				}
				
				//Is a new offer
				if(channel.getIdLong() == proposalChannelId) {				
					//guild.getTextChannelById(proposalChannelId).sendMessage(userString).queue();
					if(guild.getEmotesByName("ausstehend", true).size() > 0) {
						event.getMessage().addReaction(guild.getEmotesByName("ausstehend", true).get(0)).complete();
					}
					event.getMessage().pin().complete();
				}
			} else {
				event.getMessage().delete().complete();
			}
		} 
	}
}
