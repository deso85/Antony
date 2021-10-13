package bot.antony.events.reaction.add;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import bot.antony.Antony;
import bot.antony.events.softban.UserDataSB;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class HammerReaction extends MessageReaction {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public HammerReaction(MessageReactionAddEvent event) {
		super(event);
		allowedRoles.add("Intermorphe");
		responseChannel = Utils.getLogChannel(guild, message.getTextChannel());
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void play() {
		if(shallTrigger()) {
			UserDataSB user = new UserDataSB(message.getAuthor().getId(), message.getAuthor().getName());
			removeReaction();
			if(Antony.getSoftbanController().ban(user)) {
				responseChannel.sendMessage("🔨 User soft banned by " + reactor.getAsMention()).queue();
				responseChannel.sendMessageEmbeds(getEmbedBuilder().build()).queue();
			} else {
				Antony.getSoftbanController().unban(user);
				responseChannel.sendMessage("🔨 Softban removed from user \"" + user.getName() + "\" by " + reactor.getAsMention()).queue();
			}
		}
	}
	
	public EmbedBuilder getEmbedBuilder() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		TextChannel txtChan = message.getTextChannel();
		EmbedBuilder eb = new EmbedBuilder()
				.setColor(Color.red)
				.setAuthor(message.getAuthor().getAsTag() + " | ID: " + message.getAuthor().getId(), null, message.getAuthor().getAvatarUrl())
				.setDescription(message.getContentDisplay())
				.addField("#" + txtChan.getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + guild.getId() + "/" + txtChan.getId() + "/" + message.getId() + ")**", false)
				.setFooter(formatter.format(date));
		return eb;
	}
	
}
