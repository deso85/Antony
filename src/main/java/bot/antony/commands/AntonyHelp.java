package bot.antony.commands;

import java.util.Map.Entry;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class AntonyHelp extends ServerCommand {

	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public AntonyHelp() {
		super();
		this.privileged = false;
		this.name = "antony";
		this.description = "Mit diesem Befehl lassen sich alle verfügbaren Funktionen anzeigen.";
		this.shortDescription = "Zeigt alle verfügbaren Befehle.";
		this.example = "help";
		this.cmdParams.put("help", "Zeigt einen Hilfetext an.");
	}
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		String[] userMessage = message.getContentDisplay().split(" ");
		if (userMessage.length > 1) {
			switch (userMessage[1].toLowerCase()) {
				case "help":
					printHelp(channel);
					break;
				default:
					channel.sendMessageEmbeds(getCommandList(member).build()).queue();
					break;
			}
		} else {
			channel.sendMessageEmbeds(getCommandList(member).build()).queue();
		}
	}

	private EmbedBuilder getCommandList(Member member) {
		StringBuilder ebField;
		EmbedBuilder eb = new EmbedBuilder().setTitle("***Antony***")
				.setColor(Antony.getBaseColor())
				.setDescription("Du kannst folgende Befehle nutzen:")
				.setThumbnail(member.getJDA().getSelfUser().getEffectiveAvatarUrl())
				.setFooter("Version " + Antony.getVersion());
		
		for(Entry<String, ServerCommand> entry : Antony.getCmdMan().getAvailableCommands(member).entrySet()) {
			ebField = new StringBuilder();
			ebField.append(entry.getValue().getDescription());
			if(entry.getValue().getExample() != null && !entry.getValue().getExample().isEmpty()) {
				ebField.append("\n*__Beispiel:__ "
						+ Antony.getCmdPrefix() + entry.getValue().getName()
						+ entry.getValue().getExample() + "*");
			}
			eb.addField(Antony.getCmdPrefix() + entry.getValue().getName(), ebField.toString(), false);
		}
		
		return eb;
	}
	
	/**
	 * Function to get all commands inside an embedded message
	 * @return	commandList
	 * 			as EmbedBuilder
	 */
	/*private EmbedBuilder getCommandList() {

		//Commands for everyone
		BotCommand addhb = new BotCommand("addhb", "Hiermit lässt sich die Erstellung eines Haltungsberichtes anfragen.");
		BotCommand giveawayEnd = new BotCommand("giveaway end", "Löst ein laufendes Giveaway auf und ermittelt die Gewinner.", "https://discord.com/channels/375031723601297409/605451097699647665/798303589763252224 :tada:");
		BotCommand map = new BotCommand("map","Zeigt eine Karte von https://antmaps.org, auf der zu sehen ist, wo auf der Welt die gesuchte Ameisenart vorkommt.", "Lasius niger");
		BotCommand notify = new BotCommand("notify", "Kann genutzt werden, um über neue Einträge in Kanälen informiert zu werden. Nutze den Befehl, um detaillierte Informationen zur Handhabung zu bekommen.", "#kanal1");
		BotCommand pnlink = new BotCommand("pnlink", "Gibt einen formatierten Text für einen Kanal aus, der z.B. in PNs genutzt werden kann, um Kanäle zu verlinken, was über # nicht möglich ist. (Das letzte Leerzeichen der Ausgabe muss entfernt werden)", "#kanal1");
		BotCommand sells = new BotCommand("sells", "Listet zu der gesuchten Ameisenart alle Shops und zugehörigen Preise. Die Shops werden nach Namen sortiert ausgegeben. Die Daten werden von https://antcheck.info zur Verfügung gestellt. Vielen Dank hierfür!", "Lasius niger");
		BotCommand serverstats = new BotCommand("serverstats", "Zeigt Serverstatistiken.");
		BotCommand showAvatar = new BotCommand("showavatar", "Zeigt eine vergrößerte Version des Avatars/Profilbildes eines Benutzers.", "Antony");
		BotCommand userinfo = new BotCommand("userinfo", "Zeigt Details über den Benutzer.", "Antony");
		
		//Commands for mods
		BotCommand user = new BotCommand("user", "Funktion zur Verwaltung von Discord Usern.");
		BotCommand softban = new BotCommand("softban", "Funktion zur Verwaltung von Usern, deren Inhalte direkt nach posten gelöscht werden sollen.", "add ID NAME");
		BotCommand watchlist = new BotCommand("watchlist", "Funktion zur Verwaltung von Begriffen, über die das Mod-Team bei Verwendung eine Benachrichtigung erhalten möchte.", "add Ameisengesicht");
		BotCommand whitelist = new BotCommand("whitelist", "Funktion zur Verwaltung von Begriffen, die keine moderativen Meldungen auslösen sollen.", "add Barsch");
		
		//Commands for admins
		BotCommand blacklist = new BotCommand("blacklist", "Funktion zur Verwaltung von Begriffen, die zur sofortigen Löschung des Beitrags führen und das Mod-Team darüber benachrichtigen.", "add d1scord.hack");
		BotCommand category = new BotCommand("category", "Funktion zur Administration von Server-Kategorien.", "sort CategoryName");
		BotCommand channel = new BotCommand("channel", "Funktion, die z.B. dafür genutzt werden kann, alle Kanäle auszugeben, in denen schon länger keine Inhalte mehr gepostet wurden.", "list abandoned");
		BotCommand guild = new BotCommand("guild", "Funktion zur Verwaltung des Discord Servers.");

	}*/
	
}