package bot.antony.commands.aam;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import bot.antony.Antony;
import bot.antony.commands.antcheck.AntcheckController;
import bot.antony.commands.antcheck.client.dto.Specie;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AddHBStateMachine extends ListenerAdapter {
	private final long channelId, authorId, initMessageId; // id because keeping the entity would risk cache to become outdated
	private long startInteractionMsgId, availCheckMsgID, nativeOrExoticMsgID, approvalMsgID;
	private int errorCount = 0;
	private Boolean awaitApproval = false;
	private String antSpecies = "";
	private Boolean antAvailable = false;
	private Category hbCategory;
	private Boolean rightsResponsibilities = false;
	private AntcheckController controller = Antony.getAntcheckController();

	public AddHBStateMachine(MessageChannel channel, User author, long initMessageId, long startInteractionMsgId) {
		this.channelId = channel.getIdLong();
		this.authorId = author.getIdLong();
		this.startInteractionMsgId = startInteractionMsgId;
		this.initMessageId = initMessageId;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getMember() != null && mayInteract(event.getMember(), event.getChannel())) {
			if (!awaitApproval) {
				// Stop because of another command
				if (event.getMessage().getContentRaw().startsWith("!") || event.getMessage().getContentRaw().toLowerCase().contains("stop")) {
					event.getJDA().removeEventListener(this);
					Antony.getLogger().info("HB dialogue intentionally stopped");
					return;
				}
				
				if(errorCount >= 5) {
					event.getJDA().removeEventListener(this);
					return;
				}
				
				String content = event.getMessage().getContentRaw();
				if(content.startsWith("#")) {
					content = content.replace("#", "");
				}
				Message message = event.getMessage();
	
				// 2. have to name the ant
				if (antSpecies.equals("")) {
					handleAntSpecies(content, message);
				}
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if(mayInteract(event.getMember())) {
			// 1. User has to ensure he wants to write regularly
			if(rightsResponsibilities == false) {
				handleRightsResponsibilities(event);
			}
			
			// 3. user has to verify that the colony exists
			if (antAvailable == false) {
				handleAntAvailable(event);
			}
			
			// 4. in which category shall the channel be?
			if (hbCategory == null && antAvailable == true) {
				handleCategory(event);
			}
		}
		// 5. mods have to decide if the channel shall be created
		if (awaitApproval && mayApprove(event.getMember())) {
			handleApproval(event);
		}
	}
	
	private void handleRightsResponsibilities(MessageReactionAddEvent event) {
		if (event.getMessageIdLong() == startInteractionMsgId) {
			if (event.getEmoji().getFormatted().equals("✅")) {
				rightsResponsibilities = true;
				event.getChannel().retrieveMessageById(initMessageId).queue(msg -> {
					msg.reply("Zu welcher Ameisen-Art willst du einen Haltungsbericht führen?\n"
							+ "*Wenn nur die Gattung bekannt ist oder die Art nicht genau bestimmt wurde, nutze bitte \"sp.\" (z.B. Lasius sp.) oder \"cf.\" (z.B. Lasius cf. niger).*")
							.queue();
				});
			} else if (event.getEmoji().getFormatted().equals("❌")) {
				event.getChannel().retrieveMessageById(initMessageId).queue(msg -> {
					msg.reply(
							"Frage gerne einen neuen Haltungsbericht an, sobald du es dir zutraust.")
							.queue();
				});
				event.getJDA().removeEventListener(this);
			}
		}
	}
	
	private void handleAntSpecies(String content, Message message) {
		List<Specie> species = new ArrayList<Specie>();
		String antSpeciesName = content.replace("-", " ");
		String antSpeciesChannelName = antSpeciesName.trim().replace("spec.", "sp.").replace("conf.", "cf.").replaceAll(" +", " ");
		antSpeciesName = antSpeciesName.replace("sp.", "").replace("spec.", "");
		antSpeciesName = antSpeciesName.replace("cf.", "").replace("conf.", "");
		antSpeciesName = antSpeciesName.trim().replaceAll(" +", " ");
		
		String[] antSpeciesNameParts = antSpeciesName.replaceAll("\\s+", " ").trim().split(" ");
		if (antSpeciesNameParts.length > 1) { //Genus and specie name are given
			species = controller.findAnt(antSpeciesNameParts[0], antSpeciesNameParts[1]);
		} else { //only genus or specie name is given
			species = controller.findAnt(antSpeciesNameParts[0]);
		}

		if (species.isEmpty()) {
			errorCount++;
			if(errorCount <= 4) {
				message.reply(
					"Es konnte keine Ameisenart mit \"" + antSpeciesChannelName + "\" im Namen gefunden werden.\n"
							+ "Bitte überprüfe die Schreibweise und versuche es erneut."
							+ "\n*Bitte beachte, dass Abkürzungen mit einem Punkt geschrieben werden (z.B.: Lasius **cf.** niger).*")
					.queue();
			} else {
				message.reply(
					"Es konnte keine Ameisenart mit \"" + antSpeciesChannelName + "\" im Namen gefunden werden.\n"
							+ "**Abbruch wegen zu häufiger Fehl-Eingaben.**")
					.queue();
				Antony.getLogger().info("HB dialogue cancelled because of too many mistakes by the user");
			}
		} else {
			// Too many species found
			if (species.size() > 1 && !antSpeciesChannelName.contains("sp.")) {
				errorCount++;
				if(errorCount <= 4) {
					message.reply("Es wurden " + species.size()
						+ " Ameisenarten gefunden, bitte schränke deine Suche weiter ein."
						+ "\n*Bitte beachte, dass Abkürzungen mit einem Punkt geschrieben werden (z.B.: Lasius **cf.** niger).*").queue();
				} else {
					message.reply(
						"Es wurden " + species.size()
							+ " Ameisenarten gefunden\n"
							+ "**Abbruch wegen zu häufiger Fehl-Eingaben.**")
						.queue();
					Antony.getLogger().info("HB dialogue cancelled because of too many mistakes by the user");
				}
			} else {
				errorCount = 0;
				if(!antSpeciesChannelName.contains("sp.") && !antSpeciesChannelName.contains("cf.")) {
					antSpecies = species.get(0).getName();
				} else {
					antSpecies = antSpeciesChannelName;
				}
				message.reply("**" + antSpecies + "** - Hast du die Kolonie schon?").queue(msg -> {
					Utils.addBooleanChoiceReactions(msg);
					availCheckMsgID = msg.getIdLong();
				});
			}
		}
	}	
	
	private void handleAntAvailable(MessageReactionAddEvent event) {
		if (event.getMessageIdLong() == availCheckMsgID) {
			if (event.getEmoji().getFormatted().equals("✅")) {
				antAvailable = true;
				event.getChannel().retrieveMessageById(availCheckMsgID).queue(msg -> {
					msg.reply("Bitte wähle aus, ob deine Kolonie einheimisch oder exotisch ist.\n"
							+ "1️⃣ Einheimisch\n"
							+ "2️⃣ Exotisch").queue(submsg -> {
								submsg.addReaction(Emoji.fromUnicode("1️⃣")).queue();
								submsg.addReaction(Emoji.fromUnicode("2️⃣")).queue();
								nativeOrExoticMsgID = submsg.getIdLong();
							});
					
				});
			} else if (event.getEmoji().getFormatted().equals("❌")) {
				event.getChannel().retrieveMessageById(availCheckMsgID).queue(msg -> {
					msg.reply(
							"Es tut mir leid, aber Haltungsberichte können nur zu vorhandenen Kolonien angefragt werden.")
							.queue();
				});
				event.getJDA().removeEventListener(this);
			}
		}
	}
	
	private void handleCategory(MessageReactionAddEvent event) {
		if (event.getMessageIdLong() == nativeOrExoticMsgID) {
			//get all Categories related to HBs
			List<Category> categories = new ArrayList<Category>();
			
			//pre filter categories
			if (event.getEmoji().getFormatted().equals("1️⃣")) { //native
				categories = event.getGuild().getCategories().stream()
					.filter(cat -> cat.getName().toLowerCase().contains("hb einheimisch"))
					.filter(cat -> !cat.getName().toLowerCase().contains("geschlossen"))
					.collect(Collectors.toList());
			} else if(event.getEmoji().getFormatted().equals("2️⃣")) { //exotic
				categories = event.getGuild().getCategories().stream()
					.filter(cat -> cat.getName().toLowerCase().contains("hb exotisch"))
					.filter(cat -> !cat.getName().toLowerCase().contains("geschlossen"))
					.collect(Collectors.toList());
			}
			
			//compare category name to ant species name
			for(Category category : categories) {
				String[] range = category.getName().substring(category.getName().lastIndexOf(" ")+1).split("-");
				
				//It is a category with a given range
				if(range.length > 1) {
					int rangeStartLength = range[0].length();
					int rangeEndLength = range[1].length();
					char[] rangeStartChars = giveChars(range[0]);
					char[] rangeEndsChars = giveChars(range[1]);
					char[] antSpecieChars = giveChars(antSpecies);
					
					boolean matches = true;
					for(int i=0; i<rangeStartLength; i++) {
						if(rangeStartChars[i] > antSpecieChars[i]) {
							matches = false;
							break;
						}	
					}
					if(matches) {
						for(int i=0; i<rangeEndLength; i++) {
							if(rangeEndsChars[i] < antSpecieChars[i]) {
								matches = false;
								break;
							}	
						}
					}
					
					if(matches) {
						//Thats the category
						hbCategory = category;
					}
					
				} else { //this is a unique category
					if(antSpecies.substring(0, range[0].length()).toLowerCase().equals(range[0].toLowerCase())) {
						//Thats the category
						hbCategory = category;
					}
				}
			}
			
			if(hbCategory != null) {
				awaitApproval = true;
				
				event.getChannel().retrieveMessageById(initMessageId).queue(msg -> {
					msg.reply("Danke für die Infos. Ein Mod wird zeitnah darüber entscheiden, ob ein Haltungsbericht angelegt wird und du wirst dann darüber informiert.")
							.queue();
					TextChannel replyChan;
					if (Antony.getGuildController().getLogChannel(msg.getGuild()) != null) {
						replyChan = Antony.getGuildController().getLogChannel(msg.getGuild());
					} else {
						replyChan = msg.getChannel().asTextChannel();
					}
					StringBuilder sb = new StringBuilder();
					sb.append("ℹ️ Anfrage für neuen Haltungsbericht\n");
					sb.append("Author: " + event.getMember().getAsMention() + "\n");
					sb.append("Art: " + antSpecies + "\n");
					sb.append("Kategorie: " + hbCategory.getName() + "\n\n");
					sb.append("Soll der Haltungsbericht angelegt werden?");
	
					replyChan.sendMessage(sb.toString()).queue(submsg -> {
						Utils.addBooleanChoiceReactions(submsg);
						approvalMsgID = submsg.getIdLong();
					});
				});
				
				Antony.getLogger().info("HB dialogue is over and user awaits approval.");
			}
		}
	}
	
	public char[] giveChars(String text) {
		String textLower = text.toLowerCase();
		char[] charArray = new char[textLower.length()];
        // Zeichen aus dem String in das Char-Array kopieren
		for (int i = 0; i < textLower.length(); i++) {
        	charArray[i] = textLower.charAt(i);
        }
		return charArray;
	}
	
	private void handleApproval(MessageReactionAddEvent event) {
		if (event.getMessageIdLong() == approvalMsgID) {
			if (event.getEmoji().getFormatted().equals("✅")) {
				// add channel
				List<Member> members = new ArrayList<Member>();
				members.add(event.getGuild().getMemberById(authorId));
				TextChannel newChan = bot.antony.commands.ChannelCmd.addChannel(
						antSpecies + "—" + event.getGuild().getMemberById(authorId).getEffectiveName(),
						hbCategory, members, event.getMember(), true);

				//TODO Doesn't work with Thread
				event.getGuild().getTextChannelById(channelId).retrieveMessageById(initMessageId).queue(msg -> {
					msg.reply("Der neue Haltungsbericht " + newChan.getAsMention() + " wurde angelegt.")
							.queue();
				});

				event.getJDA().removeEventListener(this);
			} else if (event.getEmoji().getFormatted().equals("❌")) {
				//Message in channel is not wanted -> switch to pm
				/*event.getGuild().getTextChannelById(channelId).retrieveMessageById(initMessageId).queue(msg -> {
					msg.reply(
							"Leider wurde die Erstellung eines Haltungsberichtes abgelehnt. Bitte wende dich für mögliche Rückfragen direkt an die Moderation.")
							.queue();
				});*/
				//send pm
				Member member = event.getGuild().getMemberById(authorId);
				try {
					member.getUser().openPrivateChannel().complete().sendMessage("Deine Anfrage zur Erstellung eines Haltungsberichts wurde bearbeitet und leider abgelehnt. Falls sich nicht schon ein Teammitglied bei dir gemeldet hat, wende dich für Rückfragen bitte direkt an das @Team-Beiträge."
							+ "\nDas zuständige Team findest du unter: https://discord.com/channels/375031723601297409/789417087382192148/793624078497611786").complete();
				} catch (ErrorResponseException e) {
					LocalDateTime now = LocalDateTime.now();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
					
					TextChannel channel = Antony.getGuildController().getLogChannel(member.getGuild());
					
					if(channel != null) {
						channel.sendMessage(":postbox: Fehler bei der Zustellung einer privaten Nachricht.").complete();
						EmbedBuilder eb = new EmbedBuilder()
								.setColor(Color.red)
								.setAuthor(member.getUser().getAsTag() + " | ID: " + member.getId(), null, member.getUser().getAvatarUrl())
								.setDescription("Ich konnte keine PN an den User " + member.getAsMention() + " senden. Es ist sehr wahrscheinlich, dass seine Privatsphäre-Einstellungen einen direkten Versand an ihn verhindern. "
										+ "Bitte informiert ihn hierüber, damit er die passenden Einstellungen setzen oder die Benachrichtigungen deaktivieren kann.\n\n"
										+ "Hier finden sich Hintergrundinformationen zu dem Thema:\n"
										+ "https://support.discord.com/hc/de/articles/217916488-Blocken-Datenschutzeinstellungen")
								.setFooter(now.format(formatter) + " Uhr");
						channel.sendMessageEmbeds(eb.build()).complete();
					}
					
					Antony.getLogger().error("ErrorResponseException: Wasn't able to send PN to User " + member.getUser().getAsTag() + " (ID " + member.getId() + ")");
				}
				event.getJDA().removeEventListener(this);
			}
		}
	}

	private boolean mayInteract(Member member, MessageChannel channel) {
		if (member.getUser().isBot())
			return false; // don't respond to other bots
		if (channel.getIdLong() != channelId)
			return false; // ignore other channels
		if (member.getIdLong() == authorId)
			return true; // Member who started the dialogue may interact
		
		return false;
	}
	
	private boolean mayInteract(Member member) {
		if (member.getUser().isBot())
			return false; // don't respond to other bots
		if (member.getIdLong() == authorId)
			return true; // Member who started the dialogue may interact
		if(Antony.getGuildController().memberIsMod(member))
			return true; // Mods (old authorization system) may approve
		
		return false;
	}
	
	private boolean mayApprove(Member member) {
		if (member.getUser().isBot())
			return false; // don't respond to other bots
		if (Antony.getGuildController().memberIsMod(member))
			return true; // Mods (old authorization system) may approve
		
		ArrayList<String> approvalRoles = new ArrayList<String>();
		approvalRoles.add("Team-Administration-Strategie");
		approvalRoles.add("Team-Beiträge");
		for(String roleName : approvalRoles) {
			if(member.getGuild().getRolesByName(roleName, true).size() > 0) { // Guild has special role
				if(member.getRoles().contains(member.getGuild().getRolesByName(roleName, true).get(0))) { // Member has special role
					return true;
				}
			}
		}
		
		return false;
	}
	
}