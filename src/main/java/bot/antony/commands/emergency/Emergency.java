package bot.antony.commands.emergency;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.commands.types.IServerCommand;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Emergency implements IServerCommand {

	private TextChannel channel;
	private boolean longtext;
	StringBuilder fileName;

	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		this.channel = channel;
		longtext = false;
		fileName = new StringBuilder();
		
		String[] userMessage = message.getContentDisplay().split(" ");
		
		if (userMessage.length > 1) {
			if (userMessage.length > 2) {
				if(userMessage[2].toLowerCase().equals("long")) {
					longtext = true;
				}
			}
			
			//Prepare file name
			fileName.append("antony.emergency.");
			switch (userMessage[1].toLowerCase()) {
				case "milben":
					fileName.append("mites");
					break;
				case "schimmel":
					fileName.append("mould");
					break;
				default:
					fileName.append(userMessage[1].toLowerCase());
					break;
			}
			if(longtext) {
				fileName.append(".long");
			}
			fileName.append(".json");
			
			// Load data and prepare output
			EmergencyData data = new EmergencyData();
			data = (EmergencyData) Utils.loadJSONData(fileName.toString(), new TypeReference<EmergencyData>(){}, data);
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
				
				channel.sendMessageEmbeds(eb.build()).queue();
			} else {
				channel.sendMessage("Oh No! Ich konnte keine Einträge finden... 😕").queue();
				printHelp();
			}
				
		} else {
			printHelp();
		}
	}


	private void printHelp() {
		channel.sendMessage("Benutzung: " + Antony.getCmdPrefix() + "emergency (milben | schimmel ) [long]").queue();
	}
}