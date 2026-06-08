package bot.antony.comparators;

import java.util.Comparator;

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class ChannelComparator implements Comparator<GuildChannel> {

	@Override
	public int compare(GuildChannel first, GuildChannel second) {
		if(first == second) return 0;
		if(first == null) return -1;
		if(second == null) return 1;

		String firstName = normalize(first.getName());
		String secondName = normalize(second.getName());

		if(firstName.contains("chat-hb")) return -1;
		if(secondName.contains("chat-hb")) return 1;

		return firstName.compareTo(secondName);
	}

	private String normalize(String name) {
		return name.toLowerCase()
				.replace("—", "-")
				.replaceAll("-(?:sp|spec|cf)(?=-|—|$)", "");
	}
}