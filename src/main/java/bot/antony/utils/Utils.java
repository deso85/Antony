package bot.antony.utils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bot.antony.Antony;
import bot.antony.guild.UserData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class Utils {

	public static void sendPM(Member member, EmbedBuilder eb) {
		try {
			member.getUser().openPrivateChannel().complete().sendMessageEmbeds(eb.build()).complete();
		} catch (ErrorResponseException e) {
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
			
			TextChannel channel = getLogChannel(member.getGuild());
			
			channel.sendMessage(":postbox: Fehler bei der Zustellung einer privaten Nachricht.").complete();
			eb = new EmbedBuilder()
					.setColor(Color.red)
					.setAuthor(member.getUser().getAsTag() + " | ID: " + member.getId(), null, member.getUser().getAvatarUrl())
					.setDescription("Ich konnte keine PN an den User " + member.getAsMention() + " senden. Es ist sehr wahrscheinlich, dass seine Privatsphäre-Einstellungen einen direkten Versand an ihn verhindern. "
							+ "Bitte informiert ihn hierüber, damit er die passenden Einstellungen setzen oder die Benachrichtigungen deaktivieren kann.\n\n"
							+ "Hier finden sich Hintergrundinformationen zu dem Thema:\n"
							+ "https://support.discord.com/hc/de/articles/217916488-Blocken-Datenschutzeinstellungen")
					.setFooter(now.format(formatter) + " Uhr");
			channel.sendMessageEmbeds(eb.build()).complete();
			
			Antony.getLogger().error("ErrorResponseException: Wasn't able to send PN to User " + member.getUser().getAsTag() + " (ID " + member.getId() + ")");
		}
	}
	
	/**
	 * 
	 * @param args	the arguments which came with the users message
	 * @return String with the trimmed ant species name
	 */
	public static String getAntSpeciesName(String[] args) {

		StringBuilder ant = new StringBuilder();

		int counter = 1;
		for(String part: args) {
			if(!part.equals("")) {
				ant.append(part.replace("-", " ").trim().replaceAll(" +", " "));
				if(counter < args.length) {
					ant.append(" ");
				}
			}
			counter++;
		}
		
		return ant.toString();
	}
	
	public static boolean memberHasRole(Member member, List<String> roles) {
		for(Role role: member.getRoles()) {
			if(roles.contains(role.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isId(String member) {
		if (member == null) {
			return false;
		}
		try {
			Long.parseLong(member);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	public static boolean isLogChannel(TextChannel channel) {
		if(channel == channel.getGuild().getTextChannelById(Antony.getAntonyLogChannelId())) {
			return true;
		}
		return false;
	}
	
	public static TextChannel getLogChannel(Guild guild) {
		if(guild.getChannels().stream().filter(c -> c.getIdLong() == Antony.getAntonyLogChannelId()).count() > 0) {
			return guild.getTextChannelById(Antony.getAntonyLogChannelId());
		} else {
			return null;
		}
	}
	
	public static TextChannel getLogChannel(TextChannel altChannel) {
		if(altChannel.getGuild().getChannels().stream().filter(chan -> chan.getIdLong() == Antony.getAntonyLogChannelId()).count() > 0) {
			return altChannel.getGuild().getTextChannelById(Antony.getAntonyLogChannelId());
		} else {
			return altChannel;
		}
	}
	
	public static boolean storeJSONData(String subFolderPath, String filename, Object file) {
		File directory = new File(Antony.getDataPath() + subFolderPath);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }
		
		return storeJSONData(subFolderPath + filename, file);
	}
	
	public static boolean storeJSONData(String filename, Object file) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(new File(Antony.getDataPath() + filename), file);
			return true;
			
		} catch (IOException e) {
			Antony.getLogger().error("Could not store \"" + Antony.getDataPath() + filename + "\"!", e);
		}
		return false;
	}
	
	public static Object loadJSONData(String subFolderPath, String filename, TypeReference<?> tr, Object objectOrigin) {
		File directory = new File(Antony.getDataPath() + subFolderPath);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }
		
	    return loadJSONData(subFolderPath + filename, tr, objectOrigin);
	}
	
	public static Object loadJSONData(String filename, TypeReference<?> tr, Object objectOrigin) {
		ObjectMapper objectMapper = new ObjectMapper();
		File file = new File(Antony.getDataPath() + filename);
		if(file.exists() && !file.isDirectory()) { 
			try {
				return objectMapper.readValue(file, tr);
			} catch (JsonParseException | JsonMappingException e) {
				Antony.getLogger().error("Could not parse " + Antony.getDataPath() + filename + " data!", e);
			} catch (IOException e) {
				Antony.getLogger().error("Could not read " + Antony.getDataPath() + filename + "!", e);
			}
		}
		return objectOrigin;
	}
	
	public static UserData loadUserData(Member member) {
		UserData user = new UserData();
		String subfolder = "guilds" + File.separator + member.getGuild().getId() + " - " + member.getGuild().getName() + File.separator + "user" + File.separator;
		String fileName = member.getId() + ".json";
		
		//Load user data if exists
		user = (UserData) Utils.loadJSONData(subfolder, fileName, new TypeReference<UserData>(){}, user);
		
		//If it is the first nickname change
		if(user.getId() == null || user.getId() == "") {
			user = new UserData(member);
			storeUserData(user, member.getGuild());
		}
		
		return user;
	}
	
	public static void storeUserData(UserData user, Guild guild) {
		String subfolder = "guilds" + File.separator + guild.getId() + " - " + guild.getName() + File.separator + "user" + File.separator;
		String fileName = user.getId() + ".json";
		
		storeJSONData(subfolder, fileName, user);
	}
	
	public static Consumer<? super Throwable> ERROR_RESPONSE_EXCEPTION_CONSUMER = exception -> {
		if (exception instanceof ErrorResponseException) {
			int errorCode = ((ErrorResponseException) exception).getErrorCode();
			switch (errorCode) {
			case 50007:
				// exception.printStackTrace();
				System.out.println(exception.getMessage());
				System.out.println(exception.getCause());
				System.out.println(((ErrorResponseException) exception).getErrorResponse());
				System.out.println(((ErrorResponseException) exception).getMeaning());
				System.out.println(((ErrorResponseException) exception).getResponse());

				break;
			default:
				exception.printStackTrace();
				break;
			}
		} else {
			exception.printStackTrace();
		}
	};
	
	public Role findRole(Member member, String name) {
	    List<Role> roles = member.getRoles();
	    return roles.stream()
	                .filter(role -> role.getName().equals(name)) // filter by role name
	                .findFirst() // take first result
	                .orElse(null); // else return null
	}
}
