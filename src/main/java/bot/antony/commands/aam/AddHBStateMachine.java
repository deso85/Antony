package bot.antony.commands.aam;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import bot.antony.Antony;
import bot.antony.commands.antcheck.client.AntCheckClient;
import bot.antony.commands.antcheck.client.dto.Specie;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class AddHBStateMachine extends ListenerAdapter {
	private final long channelId, authorId, initMessageId; // id because keeping the entity would risk cache to become outdated
	private long startInteractionMsgId, availCheckMsgID, approvalMsgID;
	private int errorCount = 0;
	private Boolean awaitApproval = false;
	private String antSpecies = "";
	private Boolean antAvailable = false;
	private Category hbCategory;
	private Boolean rightsResponsibilities = false;

	public AddHBStateMachine(MessageChannel channel, User author, long initMessageId, long startInteractionMsgId) {
		this.channelId = channel.getIdLong();
		this.authorId = author.getIdLong();
		this.startInteractionMsgId = startInteractionMsgId;
		this.initMessageId = initMessageId;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getAuthor().isBot())
			return; // don't respond to other bots
		if (event.getChannel().getIdLong() != channelId)
			return; // ignore other channels
		if (event.getAuthor().getIdLong() != authorId)
			return;
		if (!awaitApproval) {
			// Stop because of another command
			if (event.getMessage().getContentRaw().startsWith("!") || event.getMessage().getContentRaw().toLowerCase().contains("stop")) {
				event.getJDA().removeEventListener(this);
				return;
			}
			
			if(errorCount >= 5) {
				event.getJDA().removeEventListener(this);
				return;
			}
			
			String content = event.getMessage().getContentRaw();
			Message message = event.getMessage();

			// 2. have to name the ant
			if (antSpecies.equals("")) {
				handleAntSpecies(content, message);
			}

			// 4. in which category shall the channel be?
			if (hbCategory == null && antAvailable == true) {
				handleCategory(event, content, message);
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (event.getMember().getUser().isBot())
			return; // don't respond to bots
		if (event.getMember().getIdLong() != authorId && !Antony.getGuildController().memberIsMod(event.getMember())
				&& !Antony.getGuildController().memberIsAdmin(event.getMember()))
			return;

		// 1. User has to ensure he wants to write regularly
		if(rightsResponsibilities == false) {
			handleRightsResponsibilities(event);
		}
		
		// 3. user has to verify that the colony exists
		if (antAvailable == false) {
			handleAntAvailable(event);
		}

		// 5. mods have to decide if the channel shall be created
		if (awaitApproval) {
			handleApproval(event);
		}
	}
	
	
	private void handleRightsResponsibilities(MessageReactionAddEvent event) {
		if (event.getMessageIdLong() == startInteractionMsgId) {
			if (event.getReactionEmote().getName().equals("✅")) {
				rightsResponsibilities = true;
				event.getTextChannel().retrieveMessageById(initMessageId).queue(msg -> {
					msg.reply("Zu welcher Ameisen-Art willst du einen Haltungsbericht führen?")
							.queue();
				});
			} else if (event.getReactionEmote().getName().equals("❌")) {
				event.getTextChannel().retrieveMessageById(initMessageId).queue(msg -> {
					msg.reply(
							"Frage gerne einen neuen Haltungsbericht an, sobald du es dir zutraust.")
							.queue();
				});
				event.getJDA().removeEventListener(this);
			}
		}
	}
	
	private void handleAntSpecies(String content, Message message) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
		client.register(instance);
		instance.registerProvider(ResteasyJackson2Provider.class);
		ResteasyWebTarget target = client.target(AntCheckClient.BASE_URL);
		AntCheckClient antCheckClient = target.proxy(AntCheckClient.class);

		String antSpeciesName = content.replace("-", " ");
		String antSpeciesChannelName = antSpeciesName.trim().replace("spec.", "sp.").replace("conf.", "cf.").replaceAll(" +", " ");
		antSpeciesName = antSpeciesName.replace("sp.", "").replace("spec.", "");
		antSpeciesName = antSpeciesName.replace("cf.", "").replace("conf.", "");
		antSpeciesName = antSpeciesName.trim().replaceAll(" +", " ");
		
		List<Specie> species = getSpecies(antCheckClient, antSpeciesName.replace(" ", "_"));

		if (species.isEmpty()) {
			errorCount++;
			if(errorCount <= 4) {
				message.reply(
					"Es konnte keine Ameisenart mit \"" + antSpeciesChannelName + "\" im Namen gefunden werden.\n"
							+ "Bitte überprüfe die Schreibweise und versuche es erneut.")
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
						+ " Ameisenarten gefunden, bitte schränke deine Suche weiter ein.").queue();
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
				//antSpecies = species.get(0).getName();
				antSpecies = antSpeciesChannelName;
				message.reply("**" + antSpecies + "** - Hast du die Kolonie schon?").queue(msg -> {
					msg.addReaction("✅").queue();
					msg.addReaction("❌").queue();
					availCheckMsgID = msg.getIdLong();
				});
			}
		}
	}	
	
	private void handleAntAvailable(MessageReactionAddEvent event) {
		if (event.getMessageIdLong() == availCheckMsgID) {
			if (event.getReactionEmote().getName().equals("✅")) {
				antAvailable = true;
				event.getTextChannel().retrieveMessageById(availCheckMsgID).queue(msg -> {
					msg.reply("In welcher Kategorie soll der HB erstellt werden?\n"
							+ "*Du kannst den Kategorie-Namen schreiben oder einen bestehenden Kanal in der passenden Kategorie verlinken*")
							.queue();
				});
			} else if (event.getReactionEmote().getName().equals("❌")) {
				event.getTextChannel().retrieveMessageById(availCheckMsgID).queue(msg -> {
					msg.reply(
							"Es tut mir leid, aber Haltungsberichte können nur zu vorhandenen Kolonien angefragt werden.")
							.queue();
				});
				event.getJDA().removeEventListener(this);
			}
		}
	}
	
	private void handleCategory(MessageReceivedEvent event, String content, Message message) {
		if (message.getMentions().getChannels(TextChannel.class).size() > 0) {
			hbCategory = message.getMentions().getChannels(TextChannel.class).get(0).getParentCategory();
		} else {
			if (message.getGuild().getCategoriesByName(content, true).size() >= 1) {
				hbCategory = message.getGuild().getCategoriesByName(content, true).get(0);
			}
		}
		if (hbCategory == null) {
			errorCount++;
			if(errorCount <= 4) {
				message.reply("Die Kategorie existiert nicht. Bitte versuch es erneut.").queue();
			} else {
				message.reply("Die Kategorie existiert nicht.\n"
						+ "**Abbruch wegen zu häufiger Fehl-Eingaben.**").queue();
				Antony.getLogger().info("HB dialogue cancelled because of too many mistakes by the user");
			}
			
		} else {
			awaitApproval = true;
			message.reply(
				"Danke für die Infos. Ein Mod wird zeitnah darüber entscheiden, ob ein Haltungsbericht angelegt wird und du wirst dann darüber informiert.")
				.queue();
			TextChannel replyChan;
			if (Antony.getGuildController().getLogChannel(message.getGuild()) != null) {
				replyChan = Antony.getGuildController().getLogChannel(message.getGuild());
			} else {
				replyChan = message.getTextChannel();
			}
			StringBuilder sb = new StringBuilder();
			sb.append("ℹ️ Anfrage für neuen Haltungsbericht\n");
			sb.append("Author: " + event.getMember().getAsMention() + "\n");
			sb.append("Art: " + antSpecies + "\n");
			sb.append("Kategorie: " + hbCategory.getName() + "\n\n");
			sb.append("Soll der Haltungsbericht angelegt werden?");

			replyChan.sendMessage(sb.toString()).queue(msg -> {
				msg.addReaction("✅").queue();
				msg.addReaction("❌").queue();
				approvalMsgID = msg.getIdLong();
			});
			Antony.getLogger().info("HB dialogue is over and user awaits approval.");
		}
	}
	
	private void handleApproval(MessageReactionAddEvent event) {
		if (Antony.getGuildController().memberIsMod(event.getMember())
				|| Antony.getGuildController().memberIsAdmin(event.getMember())) {
			if (event.getMessageIdLong() == approvalMsgID) {
				if (event.getReactionEmote().getName().equals("✅")) {
					// add channel
					List<Member> members = new ArrayList<Member>();
					members.add(event.getGuild().getMemberById(authorId));
					TextChannel newChan = bot.antony.commands.Channel.addChannel(
							antSpecies + "—" + event.getGuild().getMemberById(authorId).getEffectiveName(),
							hbCategory, members, event.getMember(), true);

					event.getGuild().getTextChannelById(channelId).retrieveMessageById(initMessageId).queue(msg -> {
						msg.reply("Der neue Haltungsbericht " + newChan.getAsMention() + " wurde angelegt.")
								.queue();
					});

					event.getJDA().removeEventListener(this);
				} else if (event.getReactionEmote().getName().equals("❌")) {
					event.getGuild().getTextChannelById(channelId).retrieveMessageById(initMessageId).queue(msg -> {
						msg.reply(
								"Leider wurde die Erstellung eines Haltungsberichtes abgelehnt. Bitte wende dich für mögliche Rückfragen direkt an die Moderation.")
								.queue();
					});
					event.getJDA().removeEventListener(this);
				}
			}
		}
	}

	private List<Specie> getSpecies(AntCheckClient client, String antName) {
		Response response = client.getSpecies(antName);
		String responsePayload = response.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(responsePayload, new TypeReference<List<Specie>>() {
			});
		} catch (JsonProcessingException e) {
			return new ArrayList<>();
		} finally {
			response.close();
		}
	}
}