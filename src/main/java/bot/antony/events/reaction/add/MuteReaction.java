package bot.antony.events.reaction.add;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import bot.antony.Antony;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class MuteReaction extends MessageReaction {
	
	private String subDir;
	private String filename;
	private List<Long> muteList = new ArrayList<Long>();
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public MuteReaction() {
		super();
		this.description = "Diese Reaction kann dazu verwendet werden, Benutzern den Zugriff auf die Voice-Kanäle zu verbieten oder zu gewähren.";
		this.shortDescription = "Sperrt oder entsperrt User für Voice-Kanäle.";
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public void perform(MessageReactionAddEvent event) {
		setVariables(event);
		if(shallTrigger(event.getMember())) {
			removeReaction();
			boolean icon = toggleVoiceMute();
			mentionReactor(icon);
			printInfo(icon);
		}
	}
	
	@Override
	public boolean shallTrigger(Member member) {
		if(super.shallTrigger(member)
				&& !guild.getVoiceChannels().isEmpty()) {
			return true;
		}
		return false;
	}
	
	public void mentionReactor(boolean icon) {
		if(responseChannel != null) {
			String emoji = "🔊";
			if(icon) {
				emoji = "🔇";
			}
			responseChannel.sendMessage(emoji + " " + reactor.getUser().getAsMention()).queue();
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean toggleVoiceMute() {
		muteList = (List<Long>) Utils.loadJSONData(subDir, filename, new TypeReference<List<Long>>(){}, muteList);
		List<VoiceChannel> vchans = guild.getVoiceChannels();
		
		ArrayList<Permission> deny = new ArrayList<Permission>();
		deny.add(Permission.VOICE_CONNECT);
		deny.add(Permission.VOICE_SPEAK);
		deny.add(Permission.VIEW_CHANNEL);
		
		if(muteList.contains(message.getAuthor().getIdLong())) {
			//unmute user
			for(VoiceChannel vchan : vchans) {
				vchan.getManager().removePermissionOverride(message.getAuthor().getIdLong()).complete();
			}
			
			//save updated list
			muteList.remove(message.getAuthor().getIdLong());
			Utils.saveJSONData(subDir, filename, muteList);
			return false;
		} else {
			//mute user
			for(VoiceChannel vchan : vchans) {
				vchan.getManager().putMemberPermissionOverride(message.getAuthor().getIdLong(), null, deny).complete();
			}
			
			//save updated list
			muteList.add(message.getAuthor().getIdLong());
			Utils.saveJSONData(subDir, filename, muteList);
			return true;
		}
	}
	
	private void printInfo(boolean muted) {
		StringBuilder sb = new StringBuilder();
		sb.append("ID: " + message.getAuthor().getId() + "\n");
		sb.append("Tag: " + message.getAuthor().getAsTag() + "\n");
		sb.append("Name: " + message.getAuthor().getName());
		if(message.getMember().getNickname() != null) {
			sb.append("\nNickname: " + message.getMember().getNickname());
		}
		if(responseChannel != null) {
			if(muted) {
				responseChannel.sendMessage("__User wurde für Voice-Kanäle gesperrt:__").complete();
			} else {
				responseChannel.sendMessage("__User wurde für Voice-Kanäle entsperrt:__").complete();
			}
			responseChannel.sendMessage(sb.toString()).queue();
		}
	}
	
	@Override
	public void setVariables(MessageReactionAddEvent event) {
		super.setVariables(event);
		responseChannel = Antony.getGuildController().getLogChannel(event.getGuild());
		subDir = "guilds" + File.separator + guild.getId() + " - " + guild.getName() + File.separator;
		filename = "voice.mute.json";
	}
}
