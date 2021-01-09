package bot.antony.events;

import java.util.ArrayList;

import bot.antony.Antony;
import bot.antony.commands.notification.NotificationController;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class NotificationListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		//check which channel ...
		if(event.isFromType(ChannelType.TEXT)) {
			NotificationController nc = Antony.getNotificationController();
			final Guild guild = event.getGuild();
			final TextChannel channel = event.getTextChannel();
			final Message  message = event.getMessage();
			
			//check if the guild has notification lists
			if(nc.hasGCNL(guild.getId())) {
				//check if there is a notification list for this channel
				if(nc.getGCNL(guild.getId()).hasCNL(channel.getId())) {
					ArrayList<String> usrlist = nc.getGCNL(guild.getId()).getCNL(channel.getId()).getUserList();
					
					//if there are user to inform
					//TODO: Can be removed in future when lists will be deleted after all user have been removed
					if(!usrlist.isEmpty()) {
						StringBuilder logMessage = new StringBuilder();
						logMessage.append("On server \"" + guild.getName() + "\" (" + guild.getId() + ") ");
						logMessage.append("channel \"#" + channel.getName() + "\" (" + channel.getId() + ") got an update. Notified user: ");
						int counter = 1;
						for(String userID: usrlist) {
							//TODO: Catch SEVERE: RestAction queue returned failure: [ErrorResponseException] 50007: Cannot send messages to this user
							User usr = guild.getMemberById(userID).getUser();
							usr.openPrivateChannel().queue((privChannel) ->
					        {
					        	EmbedBuilder eb = new EmbedBuilder().setTitle("Benachrichtigung über ein Kanal-Update")
										.setColor(Antony.getBaseColor())
										.setThumbnail(guild.getIconUrl())
										.setDescription("Auf dem Server [" + guild.getName() + "](https://discord.com/channels/" + guild.getId() + ") "
												+ "gibt es im Kanal [#" + channel.getName() + "](https://discord.com/channels/" + guild.getId() + "/" + channel.getId() + "/" +  message.getId() + ") "
												+ "einen neuen Eintrag. Schau es dir gleich mal an!")
										.setFooter("Antony | Version " + Antony.getVersion());
					        	privChannel.sendMessage(eb.build()).queue();
					        });
							logMessage.append("\"" + usr.getName() + "\" (" + usr.getId() + ")");
							if(counter < usrlist.size()) {
								logMessage.append(", ");
							}
							counter++;
						}
						Antony.getLogger().info(logMessage.toString());
					}
					
				}
			}
		}
	}
}
