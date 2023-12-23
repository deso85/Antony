package bot.antony.events.reaction.add;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import bot.antony.Antony;
import bot.antony.events.softban.UserDataSB;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class HammerReaction extends MessageReaction {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public HammerReaction() {
		super();
		this.description = "Diese Reaction kann dazu verwendet werden, einen Benutzer mit einen Softban zu belegen, der sämtliche Nachrichten sofot nach dem Absenden wieder löscht.";
		this.shortDescription = "Reaction zum belegen eines Users mit einem Softban.";
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void perform(MessageReactionAddEvent event) {
		setVariables(event);
		if(shallTrigger(event.getMember())) {
			removeReaction();
			UserDataSB user = new UserDataSB(message.getAuthor().getId(), Utils.escapeControlChars(message.getAuthor().getName()));
			String logMsg = "";
			if(Antony.getSoftbanController().ban(user)) {
				logMsg = "🔨 User soft banned by " + reactor.getAsMention();
				if(responseChannel != null) {
					responseChannel.sendMessage(logMsg).complete();
					responseChannel.sendMessageEmbeds(getEmbedBuilder().build()).complete();
				}
			} else {
				Antony.getSoftbanController().unban(user);
				logMsg = "🔨 Softban removed from user \"" + Utils.escapeControlChars(user.getName()) + "\" by " + reactor.getAsMention();
				if(responseChannel != null) {
					responseChannel.sendMessage(logMsg).complete();
				}
			}
			if(logMsg.length() > 0) {
				Antony.getLogger().info(logMsg);
			}
		}
	}
	
	@Override
	public boolean shallTrigger(Member member) {
		if(super.shallTrigger(member)
				&& !message.getMember().isOwner()
				&& !message.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			return true;
		}
		return false;
	}
	
	@Override
	public void setVariables(MessageReactionAddEvent event) {
		super.setVariables(event);
		responseChannel = Antony.getGuildController().getLogChannel(event.getGuild());
	}
	
	public EmbedBuilder getEmbedBuilder() {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		TextChannel txtChan = message.getChannel().asTextChannel();
		EmbedBuilder eb = new EmbedBuilder()
				.setColor(Color.red)
				.setAuthor(Utils.escapeControlChars(message.getAuthor().getAsTag()) + " | ID: " + message.getAuthor().getId(), null, message.getAuthor().getAvatarUrl())
				.setDescription(message.getContentDisplay())
				.addField("#" + txtChan.getName(), "**[Hier klicken, um zur Nachricht zu kommen.](https://discord.com/channels/" + guild.getId() + "/" + txtChan.getId() + "/" + message.getId() + ")**", false)
				.setFooter(formatter.format(date));
		return eb;
	}
	
}
