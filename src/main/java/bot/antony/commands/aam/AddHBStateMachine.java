package bot.antony.commands.aam;

import java.time.LocalDateTime;
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
    private long availCheckMsgID, approvalMsgID;
    private LocalDateTime lastInteraction = LocalDateTime.now();
    private Boolean awaitApproval = false;
    private String antSpecies = "";
    private Boolean antAvailable = false;
    private Category hbCategory;

    public AddHBStateMachine(MessageChannel channel, User author, long msgID) {
        this.channelId = channel.getIdLong();
        this.authorId = author.getIdLong();
        this.initMessageId = msgID;
    }

    @Override
	public void onMessageReceived(MessageReceivedEvent event) {
    	if (event.getAuthor().isBot()) return; // don't respond to other bots
        if (event.getChannel().getIdLong() != channelId) return; // ignore other channels
        if (event.getAuthor().getIdLong() != authorId) return;
        if(!awaitApproval) {
        	//Stop because of another command
	        if (event.getMessage().getContentRaw().startsWith("!")) {
	        	event.getJDA().removeEventListener(this);
	        	return;
	        }
	        //Stop because user took too long
	        if(lastInteraction.isBefore(LocalDateTime.now().minusMinutes(1))) {
	        	event.getJDA().removeEventListener(this);
	        	return;
	        }
	        
	        String content = event.getMessage().getContentRaw();
	        Message message = event.getMessage();
	        
	        //1. have to name the ant
	        if(antSpecies.equals("")) {
	        	lastInteraction = LocalDateTime.now();
	        	ResteasyClient client = new ResteasyClientBuilder().build();
	    		ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
	    		client.register(instance);
	    		instance.registerProvider(ResteasyJackson2Provider.class);
	    		ResteasyWebTarget target = client.target(AntCheckClient.BASE_URL);
	    		AntCheckClient antCheckClient = target.proxy(AntCheckClient.class);
	    		
	        	String antSpeciesName = content.replace("-", " ").trim().replaceAll(" +", " ");
				List<Specie> species = getSpecies(antCheckClient, antSpeciesName.replace(" ", "_"));
				
				if (species.isEmpty()) {
					message.reply(
							"Es konnte keine Ameisenart mit \"" + antSpeciesName + "\" im Namen gefunden werden.\n"
									+ "Bitte überprüfe die Schreibweise und versuche es erneut.")
							.queue();
				} else {
					//Too many species found
					if(species.size() > 1) {
						message.reply("Es wurden " + species.size() + " Ameisenarten gefunden, bitte schränke deine Suche weiter ein.").queue();
					} else {
						antSpecies = species.get(0).getName();
						message.reply("**" + antSpecies + "** - Hast du die Kolonie schon?").queue(msg -> {
							msg.addReaction("✅").queue();
							msg.addReaction("❌").queue();
							availCheckMsgID = msg.getIdLong();
						});
					}
				}
	        }
	        
	        //3. in which category shall the channel be?
	        if(hbCategory == null && antAvailable == true) {
	        	lastInteraction = LocalDateTime.now();
	        	if(message.getMentionedChannels().size() > 0) {
	        		hbCategory = message.getMentionedChannels().get(0).getParent();
	        	} else {
	        		if(message.getGuild().getCategoriesByName(content, true).size() == 1) {
	        			hbCategory = message.getGuild().getCategoriesByName(content, true).get(0);
	        		}
	        	}
	        	if(hbCategory == null) {
	        		message.reply("Die Kategorie existiert nicht. Bitte versuch es erneut.").queue();
	        	} else {
	        		awaitApproval = true;
	        		message.reply("Danke für die Infos. Ein Mod wird zeitnah darüber entscheiden, ob ein Haltungsbericht angelegt wird und du wirst dann darüber informiert.").queue();
	        		TextChannel replyChan;
	        		if(Antony.getGuildController().getLogChannel(message.getGuild()) != null) {
	        			replyChan = Antony.getGuildController().getLogChannel(message.getGuild());
	        		} else {
	        			replyChan = message.getTextChannel();
	        		}
	        		StringBuilder sb = new StringBuilder();
	        		sb.append("ℹ️ Anfrage für neuen Haltungsbericht\n");
	        		sb.append("Author: " + event.getMember().getAsMention() + "\n");
	        		sb.append("Art: "+ antSpecies + "\n");
	        		sb.append("Kategorie: " + hbCategory.getName() + "\n\n");
	        		sb.append("Soll der Haltungsbericht angelegt werden?");
	        		
	        		replyChan.sendMessage(sb.toString()).queue(msg -> {
	        			msg.addReaction("✅").queue();
						msg.addReaction("❌").queue();
						approvalMsgID = msg.getIdLong();
	        		});
	        	}
	        }
        }
    }
    
    
    @Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
    	if (event.getMember().getUser().isBot()) return; // don't respond to bots
    	if (event.getMember().getIdLong() != authorId &&
    			!Antony.getGuildController().memberIsMod(event.getMember()) &&
    			!Antony.getGuildController().memberIsAdmin(event.getMember())) return;
    	
    	//2. user has to verify that the colony exists
    	if(antAvailable == false) {
	    	if (event.getMessageIdLong() == availCheckMsgID) {
	    		lastInteraction = LocalDateTime.now();
	    		if(event.getReactionEmote().getName().equals("✅")) {
	    			antAvailable = true;
	    			event.getTextChannel().retrieveMessageById(availCheckMsgID).queue(msg -> {
	    				msg.reply("In welcher Kategorie soll der HB erstellt werden?\n"
	    						+ "*Du kannst den Kategorie-Namen schreiben oder einen bestehenden Kanal in der passenden Kategorie verlinken*").queue();
	    			});
	    		} else if (event.getReactionEmote().getName().equals("❌")) {
	    			event.getTextChannel().retrieveMessageById(availCheckMsgID).queue(msg -> {
	    				msg.reply("Es tut mir leid, aber Haltungsberichte können nur zu vorhandenen Kolonien angefragt werden.").queue();
	    			});
	    			event.getJDA().removeEventListener(this);
	    		}
	    	}
    	}
    	
    	//4. mods have to decide if the channel shall be created
    	if(awaitApproval) {
    		if(Antony.getGuildController().memberIsMod(event.getMember()) ||
    				Antony.getGuildController().memberIsAdmin(event.getMember())) {
    			if (event.getMessageIdLong() == approvalMsgID) {
    				if(event.getReactionEmote().getName().equals("✅")) {
    	    			//add channel
    					List<Member> members = new ArrayList<Member>();
    					members.add(event.getGuild().getMemberById(authorId));
    					TextChannel newChan = bot.antony.commands.Channel.addChannel(antSpecies + "—" + event.getGuild().getMemberById(authorId).getEffectiveName(), hbCategory, members, event.getMember(), true);
    					
    					event.getGuild().getTextChannelById(channelId).retrieveMessageById(initMessageId).queue(msg -> {
    	    				msg.reply("Der neue Haltungsbericht " + newChan.getAsMention() + " wurde angelegt.").queue();
    	    			});
    					
    					event.getJDA().removeEventListener(this);
    	    		} else if (event.getReactionEmote().getName().equals("❌")) {
    	    			event.getTextChannel().retrieveMessageById(initMessageId).queue(msg -> {
    	    				msg.reply("Leider wurde die Erstellung eines Haltungsberichtes abgelehnt. Bitte wende dich für mögliche Rückfragen direkt an die Moderation.").queue();
    	    			});
    	    			event.getJDA().removeEventListener(this);
    	    		}
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