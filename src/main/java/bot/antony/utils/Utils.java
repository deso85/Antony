package bot.antony.utils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import bot.antony.Antony;
import bot.antony.commands.antcheck.client.AntCheckClient;
import jakarta.ws.rs.client.ClientBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public abstract class Utils {

	public static void sendPM(Member member, EmbedBuilder eb) {
		try {
			member.getUser().openPrivateChannel().complete().sendMessageEmbeds(eb.build()).complete();
		} catch (ErrorResponseException e) {
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
			
			TextChannel channel = Antony.getGuildController().getLogChannel(member.getGuild());
			
			if(channel != null) {
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
			}
			
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
	
	public static boolean saveJSONData(String subFolderPath, String filename, Object file) {
		File directory = new File(Antony.getDataPath() + subFolderPath);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }
		
		return saveJSONData(subFolderPath + filename, file);
	}
	
	public static boolean saveJSONData(String filename, Object file) {
		ObjectMapper objectMapper = new ObjectMapper();
		//objectMapper.findAndRegisterModules();
		objectMapper.registerModule(new JSR310Module());
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
		//objectMapper.findAndRegisterModules();
		objectMapper.registerModule(new JSR310Module());
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
	
	public static AntCheckClient getAntCheckClient() {
		ResteasyClient client = (ResteasyClient) ClientBuilder.newClient();
		client.register(JacksonJsonProvider.class);	//needed because of "RESTEASY003145: Unable to find a MessageBodyReader of content-type application/json ..."
		ResteasyWebTarget target = client.target(AntCheckClient.BASE_URL);
		return target.proxy(AntCheckClient.class);
	}
}
