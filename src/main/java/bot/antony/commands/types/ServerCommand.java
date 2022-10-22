package bot.antony.commands.types;

import java.util.LinkedHashMap;
import java.util.Map;

import bot.antony.Antony;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ServerCommand implements IServerCommand {
	
	protected Boolean privileged = false;
	protected String name;
	protected String description;
	protected String shortDescription;
	protected String example;
	protected Map<String, String> cmdParams = new LinkedHashMap<String, String>(); //<parameter, explanation>
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ServerCommand() {
		super();
	}
	
	public ServerCommand(String cmdName) {
		super();
		this.name = cmdName;
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	public void performCommand(Member member, TextChannel channel, Message message) {
		
	}
	
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
	
	public void printHelp(TextChannel channel) {
		/*StringBuilder helptext = new StringBuilder();
		helptext.append("------------------------------\n");
		helptext.append("Befehl: *" + Antony.getCmdPrefix() + cmdName + "*\n");
		helptext.append("Zugriffsbeschränkung: *" + (privileged ? "ja" : "nein") + "*\n");
		helptext.append("Beschreibung: *" + description + "*\n");
		if(!cmdParams.isEmpty()) {
			helptext.append("------------------------------\n");
			helptext.append("Benutzung:\n");
			for(Map.Entry<String, String> entry : cmdParams.entrySet()) {
				helptext.append("**" + Antony.getCmdPrefix() + cmdName + " " + entry.getKey() + "**");
				if(entry.getValue() != null && !entry.getValue().equals("")) {
					helptext.append(" - " + entry.getValue());
				}
				helptext.append("\n");
			}
		}
		helptext.append("------------------------------\n");
		channel.sendMessage(helptext.toString()).queue();*/
		
		
		
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
