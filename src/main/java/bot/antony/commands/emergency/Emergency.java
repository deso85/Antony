package bot.antony.commands.emergency;

import javax.annotation.Nullable;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Emergency extends ServerCommand {

	boolean longtext = false;
	StringBuilder fileName = new StringBuilder();
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public Emergency() {
		super();
		this.privileged = false;
		this.name = "emergency";
		this.description = "Dieser Befehl stellt Informationen bereit, die bei Notfällen helfen sollen.";
		this.shortDescription = "Stellt Informationen breit, die bei Notfällen helfen sollen.";
		this.example = "milben";
		this.cmdParams.put("milben", "Stellt Informationen zu Milben bereit.");
		this.cmdParams.put("schimmel", "Stellt Informationen zu Schimmel bereit.");
	}
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");

		if (userMessage.length > 1) {
			if (userMessage.length > 2) {
				if(userMessage[2].toLowerCase().equals("long")) {
					longtext = true;
				}
			}
			setFileName(userMessage[1].toLowerCase());
			EmbedBuilder eb = getEmergencyEmbed(getEmergencyData());
			if(eb != null) {
				channel.sendMessageEmbeds(eb.build()).queue();
			} else {
				channel.sendMessage("Oh No! Ich konnte keine Einträge finden... 😕").queue();
				printHelp(channel);
			}
		} else {
			printHelp(channel);
		}
	}
	
	private void setFileName(String emergencyCase) {
		fileName.append("antony.emergency.");
		switch (emergencyCase) {
			case "milben":
				fileName.append("mites");
				break;
			case "schimmel":
				fileName.append("mould");
				break;
			default:
				fileName.append(emergencyCase);
				break;
		}
		if(longtext) {
			fileName.append(".long");
		}
		fileName.append(".json");
	}
	
	private EmergencyData getEmergencyData() {
		EmergencyData data = new EmergencyData();
		data = (EmergencyData) Utils.loadJSONData(fileName.toString(), new TypeReference<EmergencyData>(){}, data);
		return data;
	}
	
	@Nullable
	private EmbedBuilder getEmergencyEmbed(EmergencyData data) {
		if(data.hasTitle()) {
			EmbedBuilder eb = new EmbedBuilder()
				.setColor(Antony.getBaseColor())
				.setFooter("Antony Version " + Antony.getVersion());
			
			// Set title
			if(data.hasTitleURI()) {
				eb.setTitle(data.getTitle(), data.getTitleURI());
			}else {
				eb.setTitle(data.getTitle());
			}
			
			// Set description if exists
			if(data.hasDescription()) {
				eb.setDescription(data.getDescription());
			}
			
			// Set content fields
			for(ContentPair cp : data.getContent()) {
				eb.addField(cp.getTitle(), cp.getContent(), cp.isInline());
			}
			
			// Add an image at the end if exists
			if(data.hasImage()) {
				eb.setImage(data.getImgURI());
			}
			return eb;
		} else {
			return null;
		}
	}
}