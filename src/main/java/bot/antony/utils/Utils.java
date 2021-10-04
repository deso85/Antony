package bot.antony.utils;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import bot.antony.Antony;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class Utils {

	public static void sendPM(User user, EmbedBuilder eb) {
		try {
			user.openPrivateChannel().complete().sendMessageEmbeds(eb.build()).complete();
		} catch (ErrorResponseException e) {
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
			
			TextChannel channel = (TextChannel) user.getJDA().getGuildChannelById(ChannelType.TEXT, Antony.getAntonyLogChannelId());
			channel.sendMessage(":postbox: Fehler bei der Zustellung einer privaten Nachricht.").complete();
			eb = new EmbedBuilder()
					.setColor(Color.red)
					.setAuthor(user.getAsTag() + " | ID: " + user.getId(), null, user.getAvatarUrl())
					.setDescription("Ich konnte keine PN an den User " + user.getAsMention() + " senden. Es ist sehr wahrscheinlich, dass seine Privatsphäre-Einstellungen einen direkten Versand an ihn verhindern. "
							+ "Bitte informiert ihn hierüber, damit er die passenden Einstellungen setzen oder die Benachrichtigungen deaktivieren kann.\n\n"
							+ "Hier finden sich Hintergrundinformationen zu dem Thema:\n"
							+ "https://support.discord.com/hc/de/articles/217916488-Blocken-Datenschutzeinstellungen")
					.setFooter(now.format(formatter) + " Uhr");
			channel.sendMessageEmbeds(eb.build()).complete();
			
			Antony.getLogger().error("ErrorResponseException: Wasn't able to send PN to User " + user.getAsTag() + " (ID " + user.getId() + ")");
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
		return guild.getTextChannelById(Antony.getAntonyLogChannelId());
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
}
