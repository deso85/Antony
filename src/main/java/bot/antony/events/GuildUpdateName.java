package bot.antony.events;

import bot.antony.Antony;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildUpdateName extends ListenerAdapter {
	
	@Override
	public void onGuildUpdateName(GuildUpdateNameEvent event) {
		Antony.getGuildController().changeGuildName(event.getGuild(), event.getOldName(), event.getNewName());
	}
}
