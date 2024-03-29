package bot.antony.commands;

import java.util.Map.Entry;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class AntonyCmd extends ServerCommand {

	Member member;
	GuildMessageChannel channel;
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AntonyCmd() {
		super();
		this.privileged = false;
		this.name = "antony";
		this.description = "Mit diesem Befehl lassen sich alle verfügbaren Funktionen anzeigen.";
		this.shortDescription = "Zeigt alle verfügbaren Befehle.";
		this.example = "help";
		this.cmdParams.put("help", "Zeigt einen Hilfetext an.");
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		this.member = member;
		this.channel = channel;
		
		String[] userMessage = message.getContentDisplay().split(" ");
		if (userMessage.length > 1) {
			switch (userMessage[1].toLowerCase()) {
				case "help":
					printHelp(channel);
					break;
				default:
					listCommands();
					break;
			}
		} else {
			listCommands();
		}
	}

	private void listCommands() {
		StringBuilder ebField;
		EmbedBuilder eb = new EmbedBuilder().setTitle("***Antony***")
				.setColor(Antony.getBaseColor())
				.setDescription("Du kannst folgende Befehle nutzen:")
				.setThumbnail(member.getJDA().getSelfUser().getEffectiveAvatarUrl());
				
		
		int fieldCounter = 0;
		for(Entry<String, ServerCommand> entry : Antony.getCmdMan().getAvailableCommands(member).entrySet()) {
			
			if(fieldCounter >= 25) { //Embed may only have 25 fields
				channel.sendMessageEmbeds(eb.build()).queue();
				eb = new EmbedBuilder()
						.setColor(Antony.getBaseColor());
				fieldCounter = 0;
			}
			
			ebField = new StringBuilder();
			ebField.append(entry.getValue().getDescription());
			if(entry.getValue().getDescription().isEmpty()) {
				System.out.println("ALARM!");
			}
			if(entry.getValue().getExample() != null && !entry.getValue().getExample().isEmpty()) {
				ebField.append("\n*__Beispiel:__ "
						+ Antony.getCmdPrefix() + entry.getValue().getName() + " "
						+ entry.getValue().getExample() + "*");
			}
			eb.addField(Antony.getCmdPrefix() + entry.getValue().getName(), ebField.toString(), false);
			
			fieldCounter++;
		}
		eb.setFooter("Version " + Antony.getVersion());
		channel.sendMessageEmbeds(eb.build()).queue();
	}
	
	/**
	 * Function to get all commands inside an embedded message
	 * @return	commandList
	 * 			as EmbedBuilder
	 */
	/*private EmbedBuilder getCommandList() {

		//Commands for everyone
		BotCommand giveawayEnd = new BotCommand("giveaway end", "Löst ein laufendes Giveaway auf und ermittelt die Gewinner.", "https://discord.com/channels/375031723601297409/605451097699647665/798303589763252224 :tada:");
		BotCommand pnlink = new BotCommand("pnlink", "Gibt einen formatierten Text für einen Kanal aus, der z.B. in PNs genutzt werden kann, um Kanäle zu verlinken, was über # nicht möglich ist. (Das letzte Leerzeichen der Ausgabe muss entfernt werden)", "#kanal1");
		BotCommand sells = new BotCommand("sells", "Listet zu der gesuchten Ameisenart alle Shops und zugehörigen Preise. Die Shops werden nach Namen sortiert ausgegeben. Die Daten werden von https://antcheck.info zur Verfügung gestellt. Vielen Dank hierfür!", "Lasius niger");
		BotCommand serverstats = new BotCommand("serverstats", "Zeigt Serverstatistiken.");
		
		//Commands for mods
		BotCommand user = new BotCommand("user", "Funktion zur Verwaltung von Discord Usern.");
		BotCommand softban = new BotCommand("softban", "Funktion zur Verwaltung von Usern, deren Inhalte direkt nach posten gelöscht werden sollen.", "add ID NAME");

	}*/
	
}