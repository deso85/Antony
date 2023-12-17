package bot.antony.commands.shopping;

import java.util.List;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class ShoppingCmd extends ServerCommand {
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public ShoppingCmd() {
		super();
		this.privileged = false;
		this.name = "shopping";
		this.description = "Mit diesem Befehl lässt sich eine Liste mit Kaufempfehlungen für die Ameisenhaltung anzeigen.";
		this.shortDescription = "Zeigt eine Liste mit Kaufempfehlungen für die Ameisenhaltung.";
	}
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void performCommand(Member member, GuildMessageChannel channel, Message message) {
		ShoppingController controller = new ShoppingController();
		List<ShoppingCategory> categories = controller.getCategories();
		EmbedBuilder eb = new EmbedBuilder()
				.setColor(Antony.getBaseColor())
				.setTitle("Kaufempfehlungen")
				.setDescription("Da häufig nach Kaufempfehlungen gefragt wird, findest du hier eine Liste von Dingen, die man für die Ameisenhaltung und DIY-Projekte gut gebrauchen kann."
						+ "\n\nDu hast eine Empfehlung? Lass es mich wissen und ggf. füge ich sie der Liste hinzu.")
				.setFooter("Solltet ihr über die gelisteten Links etwas kaufen, erhält Chasil hierdurch ggf. eine kleine Verkaufsprovision, ihr zahlt aber keinen Cent mehr!"
						+ "\n\nAntony Version " + Antony.getVersion());
		
		for(ShoppingCategory category : categories) {
			if(category.hasItems()) {
				String ebFieldName = category.getName();
				StringBuilder ebFieldText = new StringBuilder();
				
				for(ShoppingItem item : category.getItems()) {
					String prep = "[" + item.getDescription() + "](" + item.getUrl() + ")\n";
					if(ebFieldText.length() + prep.length() > 1024) {
						eb.addField(ebFieldName, ebFieldText.toString(), false);
						ebFieldName = "";
						ebFieldText = new StringBuilder();
					}
					ebFieldText.append(prep);
				}
				eb.addField(ebFieldName, ebFieldText.toString(), false);
			}
		}
		
		channel.sendMessageEmbeds(eb.build()).queue();
	}

}