package bot.antony.commands;

import java.util.ArrayList;
import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class AntonyHelp implements ServerCommand {

	private TextChannel channel;

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {

		setChannel(channel);

		String[] userMessage = message.getContentDisplay().split(" ");

		if (userMessage.length > 1) {

			switch (userMessage[1].toLowerCase()) {

			case "changelog":
				
				// Generate changelog entries
				List<ChangeLogEntry> changeLog = new ArrayList<ChangeLogEntry>();
				changeLog.add(new ChangeLogEntry("09.12.2020 - Version 1.3.1", "Der Code wurde an einigen Stellen überarbeitet."));
				changeLog.add(new ChangeLogEntry("05.12.2020 - Version 1.3.0", "Die ***" + Antony.getCmdPrefix() + "userinfo*** Funktion wurde fertig implementiert."));
				changeLog.add(new ChangeLogEntry("29.11.2020 - Version 1.2.1", "Kleineres Update aufgrund einer Änderung an der antcheck API"));
				changeLog.add(new ChangeLogEntry("26.11.2020 - Version 1.2.0", "Einige Basis-Funktionalitäten wurden hinzugefügt, um den Bot einfacher nutzen zu können. "
						+ "Dazu zählt unter anderem das ***" + Antony.getCmdPrefix() + "antony*** Kommando, um dem Anwender Informationen zum Bot zur Verfügung zu stellen. "
						+ "Zusätzlich wurden kleinere Bugs behoben."));
				changeLog.add(new ChangeLogEntry("25.11.2020 - Version 1.1.0", "Ein Großteil des Codes wurde umgeschrieben, um eine bessere Ausgangslage für neue Funktionen zu bieten."));
				changeLog.add(new ChangeLogEntry("20.11.2020 - Version 1.0.0", "Die ***" + Antony.getCmdPrefix() + "sells*** Funktion wurde fertig implementiert und der Bot auf dem Discord Server \"Ameisen an die Macht!\" vorgestellt."));
				changeLog.add(new ChangeLogEntry("17.11.2020 - Version 0.0.1", "Antony wurde bei Discord registriert und erste Test-Funktionen wurden geschrieben."));
					
				// Build output
				EmbedBuilder eb = new EmbedBuilder().setTitle("***Antony - Changelog***")
						.setColor(Antony.getBaseColor())
						.setThumbnail(channel.getJDA().getSelfUser().getEffectiveAvatarUrl())
						.setDescription("Hier kannst du nachvollziehen, wie sich Antony weiterentwickelt hat.")
						.setFooter("Version " + Antony.getVersion());
				
				// Add changelog entries to embedded message
				for(ChangeLogEntry cle: changeLog) {
					eb.addField(cle.getTitle(), cle.getNotes(), false);
				}
				
				channel.sendMessage(eb.build()).queue();
				break;

			default:
				channel.sendMessage(getCommandList().build()).queue();
				break;
			}
		} else {
			channel.sendMessage(getCommandList().build()).queue();
		}

	}

	/**
	 * Function to get all commands inside an embedded message
	 * @return	commandList
	 * 			as EmbedBuilder
	 */
	private EmbedBuilder getCommandList() {
		
		List<BotCommand> botCommands = new ArrayList<BotCommand>();
		BotCommand antony = new BotCommand("antony", "Zeigt diese Übersicht an.");
		BotCommand antonyChangelog = new BotCommand("antony changelog", "Zeigt den Changelog von Antony an.");
		BotCommand sells = new BotCommand("sells", "Listet zu der gesuchten Ameisenart alle Shops und zugehörigen Preise. Die Shops werden nach Namen sortiert ausgegeben. Die Daten werden von https://antcheck.de zur Verfügung gestellt. Vielen Dank hierfür!", "Lasius niger");
		BotCommand userinfo = new BotCommand("userinfo", "Zeigt Details über den Benutzer.", "Antony");
		botCommands.add(antony);
		botCommands.add(antonyChangelog);
		botCommands.add(sells);
		botCommands.add(userinfo);
		
		EmbedBuilder eb = new EmbedBuilder().setTitle("***Antony***")
				.setColor(Antony.getBaseColor())
				.setDescription("Folgende Befehle können genutzt werden.")
				.setThumbnail(getChannel().getJDA().getSelfUser().getEffectiveAvatarUrl())
				.setFooter("Version " + Antony.getVersion());
		
		// add command descriptions to embedded output
		for(BotCommand bc: botCommands) {
			StringBuilder sb = new StringBuilder();
			sb.append(bc.getDescription());
			if(bc.getExample() != null) {
				sb.append("\n*Beispiel: " + Antony.getCmdPrefix() + bc.getCommand() + " " + bc.getExample() + "*");
			}
			eb.addField(Antony.getCmdPrefix() + bc.getCommand(), sb.toString(), false);
		}
		return eb;

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
	private class ChangeLogEntry {
	    private String title;
		private String notes;

	    ChangeLogEntry(String title, String notes) {
	        setTitle(title);
	        setNotes(notes);
	    }
	    
	    public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getNotes() {
			return notes;
		}

		public void setNotes(String notes) {
			this.notes = notes;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("ChangeLogEntry(");
			sb.append("Title: " + getTitle());
			sb.append(", Notes: " + getNotes());
			sb.append(")");
			return sb.toString();
		}
	}
	
	private class BotCommand {
	    private String command;
		private String description;
		private String example;

		BotCommand(String function, String description, String example) {
	        setCommand(function);
	        setDescription(description);
	        setExample(example);
	    }
		
		BotCommand(String function, String description) {
	        setCommand(function);
	        setDescription(description);
	    }

		public String getCommand() {
			return command;
		}

		public void setCommand(String function) {
			this.command = function;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getExample() {
			return example;
		}

		public void setExample(String example) {
			this.example = example;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("BotCommand(");
			sb.append("Function: " + getCommand());
			sb.append(", Description: " + getDescription());
			if(getExample() != null) {
				sb.append(", Example: " + getExample());
			}
			sb.append(")");
			return sb.toString();
		}
	    
	}
}