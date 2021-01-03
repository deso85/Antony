package bot.antony.commands;

import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class ChannelUpdateNotification implements ServerCommand {

	private TextChannel channel;

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		setChannel(channel);

		String[] userMessage = message.getContentDisplay().split(" ");
		// Exp. Parameter: (on|off|stats|statistics) #MentionedChannel
		
		if ((userMessage.length > 1) && (message.getMentionedChannels().size() > 0)) {
			
			System.out.println("Discord Server: " + message.getGuild().getId());
			System.out.println("Channel: " + message.getChannel().getId());
			System.out.println("Nachricht: " + message.getId());
			
			//Test PN for later reuse
			message.getAuthor().openPrivateChannel().queue((privChannel) ->
	        {
	        	privChannel.sendMessage("Es wurde eine neue Nachricht veröffentlich:\n"
						+ "#" + message.getChannel().getName() + " - "
						+ "https://discord.com/channels/" + message.getGuild().getId() + "/" + message.getChannel().getId() + "/" + message.getId()).queue();
	        });
			
			/*getChannel().sendMessage("Es wurde eine neue Nachricht veröffentlich:\n"
					+ "#" + message.getChannel().getName() + " - "
					+ "https://discord.com/channels/" + message.getGuild().getId() + "/" + message.getChannel().getId() + "/" + message.getId()).queue();
			*/
			
			//System.out.println(getChannel().getJDA().toString());
			//System.out.println(message.getMentionedChannels());
			
			for(TextChannel tc: message.getMentionedChannels()) {
				System.out.println(tc.getName());
			}
			
			/*for(String parameter: userMessage) {
				System.out.println(parameter);
			}*/
			
			/*System.out.println("Name: " + getChannel().getName());
			System.out.println("ToString: " + getChannel().toString());*/
			
		} else {
			getChannel().sendMessage("Zu welchem Kanal möchtest du Updates erhalten?\n\n"
					+ "*!notify (on | off | stats) #Kanal*").queue();
		}

	}

	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------

	public TextChannel getChannel() {
		return channel;
	}

	public void setChannel(TextChannel channel) {
		this.channel = channel;
	}

	
	// --------------------------------------------------
	// Additional Classes
	// --------------------------------------------------

}