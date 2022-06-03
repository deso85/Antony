package bot.antony.comparators;

import java.util.Comparator;

import net.dv8tion.jda.api.entities.GuildChannel;

public class ChannelComparator implements Comparator<GuildChannel> {

	@Override
	public int compare(GuildChannel first, GuildChannel second) {
		if(first == second) {
			return 0;
		}
		if(first == null) {
			return -1;
		}
		if(second == null) {
			return 1;
		}
		
		String firstName = first.getName().replace("-sp", "").replace("-cf", "");
		String secondName = second.getName().replace("-sp", "").replace("-cf", "");
		
		if(firstName.contains("chat-hb")) {
			return -1;
		}
		if(secondName.contains("chat-hb")) {
			return 1;
		}
		return firstName.compareTo(secondName);
	}
}