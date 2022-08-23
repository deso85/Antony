package bot.antony.commands.aam.events;

import java.util.Arrays;

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
		if(event.isFromType(ChannelType.TEXT)) {
			Guild guild = event.getGuild();
			TextChannel proposalChan = Antony.getGuildController().getValidChannel(guild, Arrays.asList(650687328863518740L, 778960515895918632L)); //Prod, Test
			
			//check which channel ...
			if(event.getChannel() == proposalChan) {
				
				if(event.getMessage().getType() != MessageType.CHANNEL_PINNED_ADD) {
	
					if(guild.getEmojisByName("ausstehend", true).size() > 0) {
						event.getMessage().addReaction(guild.getEmojisByName("ausstehend", true).get(0)).complete();
					}
					event.getMessage().pin().complete();
					
				} else {
					event.getMessage().delete().complete();
				}
			}
		}
	}
}
