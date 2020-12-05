package bot.antony.commands;

import java.awt.Color;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class AntonyHelp implements ServerCommand {

	TextChannel channel;

	@Override
	public void performCommand(Member m, TextChannel channel, Message message) {
		this.channel = channel;

		String[] userMessage = message.getContentDisplay().split(" ");

		if (userMessage.length > 1) {

			switch (userMessage[1].toLowerCase()) {

			case "changelog":
				EmbedBuilder eb = new EmbedBuilder().setTitle("***Antony - Changelog***") // title
						.setColor(new Color(31, 89, 152)) // color of side stripe
						.setThumbnail(channel.getJDA().getSelfUser().getEffectiveAvatarUrl())
						.setDescription("Hier kannst du nachvollziehen, wie sich Antony weiterentwickelt hat.")
						.addField("05.12.2020 - Version 1.3.0",
								"Die ***!userinfo*** Funktion wurde fertig implementiert.", false)
						.addField("29.11.2020 - Version 1.2.1",
								"Kleineres Update aufgrund einer Änderung an der antcheck API", false)
						.addField("26.11.2020 - Version 1.2.0",
								"Einige Basis-Funktionalitäten wurden hinzugefügt, um den Bot einfacher nutzen zu können. "
										+ "Dazu zählt unter anderem das ***!antony*** Kommando, um dem Anwender Informationen zum Bot zur Verfügung zu stellen. "
										+ "Zusätzlich wurden kleinere Bugs behoben.",
								false)
						.addField("25.11.2020 - Version 1.1.0",
								"Ein Großteil des Codes wurde umgeschrieben, um eine bessere Ausgangslage für neue Funktionen zu bieten.",
								false)
						.addField("20.11.2020 - Version 1.0.0",
								"Die ***!sells*** Funktion wurde fertig implementiert und der Bot auf dem Discord Server \"Ameisen an die Macht!\" vorgestellt.",
								false)
						.addField("17.11.2020 - Version 0.0.1",
								"Antony wurde bei Discord registriert und erste Test-Funktionen wurden geschrieben.",
								false)
						.setFooter("Version " + Antony.getVersion());
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

	private EmbedBuilder getCommandList() {

		EmbedBuilder eb = new EmbedBuilder().setTitle("***Antony***") // title
				.setColor(new Color(31, 89, 152)) // color of side stripe
				.setDescription("Folgende Befehle können genutzt werden.")
				.setThumbnail(channel.getJDA().getSelfUser().getEffectiveAvatarUrl())
				.addField("!antony", "Zeigt diese Übersicht an.", false)
				.addField("!antony changelog", "Zeigt den Changelog von Antony an.", false)
				.addField("!sells",
						"Listet zu der gesuchten Ameisenart alle Shops und zugehörigen Preise. Die Shops werden nach Namen sortiert ausgegeben. Die Daten werden von https://antcheck.de zur Verfügung gestellt. Vielen Dank hierfür!\n"
								+ "*Beispiel: !sells Lasius niger*",
						false)
				.addField("!userinfo", "Zeigt Details über den Benutzer.\n" + "*Beispiel: !userinfo Antony*", false)
				.setFooter("Version " + Antony.getVersion());

		return eb;

	}

}
