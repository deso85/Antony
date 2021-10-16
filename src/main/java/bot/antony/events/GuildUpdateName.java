package bot.antony.events;

import java.io.File;

import bot.antony.Antony;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildUpdateName extends ListenerAdapter {
	
	@Override
	public void onGuildUpdateName(GuildUpdateNameEvent event) {
		String oldDir = Antony.getDataPath() + "guilds" + File.separator + event.getGuild().getId() + " - " + event.getOldName();
		String newDir = Antony.getDataPath() + "guilds" + File.separator + event.getGuild().getId() + " - " + event.getNewName();
		
		File directory = new File(oldDir);
	    if (! directory.exists()){
	        directory.mkdirs();
	    }
		
		File sourceFile = new File(oldDir);
		File destFile = new File(newDir);
		 
		if (sourceFile.renameTo(destFile)) {
			Antony.getLogger().info("Guild name changed from \"" + event.getOldName() + "\" to \"" + event.getNewName() + "\" - Updated directory name.");
		} else {
			Antony.getLogger().error("Guild name changed from \"" + event.getOldName() + "\" to \"" + event.getNewName() + "\" - Wasn't able to update directory name.");
		}
		
	}
}
