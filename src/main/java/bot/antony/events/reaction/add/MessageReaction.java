package bot.antony.events.reaction.add;

import bot.antony.Antony;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class MessageReaction {

	protected Boolean privileged = true;
	protected String name;
	protected String description;
	protected String shortDescription;
	
	protected Emoji emote;
	protected Guild guild;
	protected Message message;
	protected GuildMessageChannel responseChannel;	//Channel to respond to reaction
	protected Member reactor;
	protected StringBuilder logMessage = new StringBuilder();
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public MessageReaction() {
		super();
	}
	
	public MessageReaction(MessageReactionAddEvent event) {
		super();
		setVariables(event);
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public void perform(MessageReactionAddEvent event) {
		setVariables(event);
	}
	
	public boolean shallTrigger(Member member) {
		if(member == null || member.getUser().isBot()) {
			return false;
		}
		if(!isPrivileged()) {
			return true;
		}
		if(member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
			return true;
		}
		return Antony.getGuildController().memberTriggersReactionCmd(member, name);
	}
	
	public void removeReaction() {
		message.removeReaction(emote, reactor.getUser()).queue();
	}
	
	public void mentionReactor() {
		if(responseChannel != null) {
			responseChannel.sendMessage(emote.getFormatted() + " " + reactor.getUser().getAsMention()).queue();
		}
	}
	
	public void printAttachments() {
		if(message.getAttachments().size() > 0) {
			responseChannel.sendMessage("Folgende Attachments wurden gepostet:").complete();
			for(Attachment attachment : message.getAttachments()) {
				responseChannel.sendMessage(attachment.getUrl()).complete();
			}
		}
	}
	
	public void log() {
		if(logMessage.length() > 0) {
			Antony.getLogger().info(logMessage.toString());
		}
	}
	
	public void setVariables(MessageReactionAddEvent event) {
		this.name = event.getEmoji().getName();
		this.emote = event.getEmoji();
		this.guild = event.getGuild();
		this.message = event.retrieveMessage().complete();
		this.responseChannel = message.getChannel().asGuildMessageChannel();
		this.reactor = event.getMember();
	}

	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public Boolean isPrivileged() {
		return privileged;
	}


	public void setPrivileged(Boolean privileged) {
		this.privileged = privileged;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getShortDescription() {
		return shortDescription;
	}


	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public Emoji getEmote() {
		return emote;
	}

	public void setEmote(Emoji emote) {
		this.emote = emote;
	}

	public Guild getGuild() {
		return guild;
	}

	public void setGuild(Guild guild) {
		this.guild = guild;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public GuildMessageChannel getResponseChannel() {
		return responseChannel;
	}

	public void setResponseChannel(TextChannel responseChannel) {
		this.responseChannel = responseChannel;
	}

	public Member getReactor() {
		return reactor;
	}

	public void setReactor(Member reactor) {
		this.reactor = reactor;
	}

	public StringBuilder getLogMessage() {
		return logMessage;
	}

	public void setLogMessage(StringBuilder logMessage) {
		this.logMessage = logMessage;
	}
	
}
