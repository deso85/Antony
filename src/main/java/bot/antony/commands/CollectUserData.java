package bot.antony.commands;

import java.util.List;

import bot.antony.commands.types.ServerCommand;
import bot.antony.guild.UserData;
import bot.antony.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class CollectUserData implements ServerCommand {
	
	@Override
	public void performCommand(Member member, TextChannel channel, Message message) {
		Guild guild = channel.getGuild();
		List<Member> members = guild.getMembers();
		
		for(Member memba : members) {
			boolean store = false;
			UserData user = Utils.loadUserData(memba);
			if(user.getNames().size() == 0) {
				user.getNames().put(System.currentTimeMillis(), user.getName());
				store = true;
			}
			if(user.getNicknames().size() == 0 &&
					memba.getNickname() != null &&
					memba.getNickname() != "") {
				user.getNicknames().put(System.currentTimeMillis(), memba.getNickname());
				store = true;
			}
			if(store) {
				Utils.storeUserData(user, guild);
			}
		}
	}

}
