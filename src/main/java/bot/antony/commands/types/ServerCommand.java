package bot.antony.commands.types;

import java.util.LinkedHashMap;
import java.util.Map;

import bot.antony.Antony;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

/**
 * A generic ServerCommand Class. Can be used to build new functionality for the bot.
 * 
 * @since  7.0.0
 * @author deso85
 */
public class ServerCommand implements IServerCommand {
	
	protected Boolean privileged = false;
	protected String name;
	protected String description;
	protected String shortDescription;
	protected String example;
	protected Map<String, String> cmdParams = new LinkedHashMap<String, String>(); //<parameter, explanation>
	
	/**
	 * Constructs a new ServerCommand instance.
	 */
	public ServerCommand() {
		super();
	}
	
	/**
	 * Constructs a new ServerCommand instance and sets the commands name.
	 */
	public ServerCommand(String cmdName) {
		super();
		this.name = cmdName;
	}
	
	/**
	 * Function which gets triggered by the user
	 * 
	 * @param  member
	 *         Member who called the function
	 * @param  channel
	 *         Channel in which the function got called
	 * @param  message
	 *         Members message which could include parameters
	 */
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		
	}
	
	/**
	 * Function to check if the member may use this ServerCommand
	 * 
	 * @param  member
	 * @return boolean
	 *         if the member may use the called function
	 */
	public boolean mayUse(Member member){
		if(member == null || member.getUser().isBot()) {
			return false;
		}
		if(!isPrivileged()) {
			return true;
		}
		if(member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)) {
			return true;
		}
		return Antony.getGuildController().memberMayUseCommand(member, name);
	}
	
	/**
	 * Prints a standardized help for the member to let him know how to use the ServerCommand
	 * 
	 * @param  channel
	 */
	public void printHelp(GuildMessageChannel channel) {
		EmbedBuilder eb = new EmbedBuilder()
				.setColor(Antony.getBaseColor())
				.setTitle(Antony.getCmdPrefix() + name)
				.setFooter("Antony Version " + Antony.getVersion());
		
		if(description != null && !description.isEmpty()) {
			eb.setDescription(description);
		}
		
		eb.addField("__Zugriffsbeschränkung__", (privileged ? "ja" : "nein"), false);
		
		if(!cmdParams.isEmpty()) {
			StringBuilder params = new StringBuilder();
			for(Map.Entry<String, String> entry : cmdParams.entrySet()) {
				params.append("***" + entry.getKey() + "***");
				if(entry.getValue() != null && !entry.getValue().equals("")) {
					params.append("\n" + entry.getValue());
				}
				params.append("\n");
			}
			eb.addField("__Parameter / Optionen__", params.toString(), false);
		}
		
		if(example != null && !example.isEmpty()) {
			eb.addField("__Beispiel__", Antony.getCmdPrefix() + name + " " + example, false);
		}
		
		channel.sendMessageEmbeds(eb.build()).queue();
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

	public void setName(String cmdName) {
		this.name = cmdName;
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

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public Map<String, String> getCmdParams() {
		return cmdParams;
	}

	public void setCmdParams(Map<String, String> cmdParams) {
		this.cmdParams = cmdParams;
	}
	
	
}
