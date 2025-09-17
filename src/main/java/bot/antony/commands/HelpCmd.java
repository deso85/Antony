package bot.antony.commands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class HelpCmd extends ServerCommand {

    private static final int DISCORD_MESSAGE_LIMIT = 2000;

    // --------------------------------------------------
    // Constructor
    // --------------------------------------------------
    public HelpCmd() {
        super();
        this.privileged = false;
        this.name = "help";
        this.description = "Mit diesem Befehl lassen sich alle verfügbaren Befehle oder einen Hilfetext zu einem spezifischen Befehl anzeigen.";
        this.shortDescription = "Zeigt alle verfügbaren Befehle oder den Hilfetext zu einem Befehl an.";
        this.example = "antony";
        this.cmdParams.put("cmdName", "Zeigt die Hilfe für einen Befehl an.");
    }

    // --------------------------------------------------
    // Functions
    // --------------------------------------------------
    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {
        String[] userMessage = message.getContentDisplay().split(" ");
        Map<String, ServerCommand> commands = Antony.getCmdMan().getAvailableCommands(member);

        if (userMessage.length > 1) {
            ServerCommand cmd = commands.get(userMessage[1].toLowerCase());
            if (cmd != null) {
                cmd.printHelp(channel);
            } else {
                channel.sendMessage("Der Befehl \"" + userMessage[1] + "\" ist nicht verfügbar.").queue();
            }
        } else {
            printHelp(channel, member);
        }
    }

    public void printHelp(GuildMessageChannel channel, Member member) {
        String prefix = Antony.getCmdPrefix();

        List<String> commandLines = Antony.getCmdMan().getAvailableCommands(member).entrySet().stream()
                .map(entry -> formatCommand(prefix, entry))
                .collect(Collectors.toList());

        String header = "Folgende Befehle stehen dir zur Verfügung:\n\n";
        String footer = "\nHilfe zu einem Befehl erhältst du, wenn du diesen Befehl nutzt: `"
                + prefix + name + " cmdName`";

        sendChunkedMessage(channel, header, commandLines, footer);
    }

    // --------------------------------------------------
    // Helper Methods
    // --------------------------------------------------
    private String formatCommand(String prefix, Entry<String, ServerCommand> entry) {
        String desc = entry.getValue().getShortDescription();
        return "`" + prefix + entry.getKey() + "`"
                + (desc != null && !desc.isEmpty() ? " – " + desc : "");
    }

    private void sendChunkedMessage(GuildMessageChannel channel, String header, List<String> lines, String footer) {
        // First page starts with the header; subsequent pages do NOT repeat it
        StringBuilder block = new StringBuilder(header);

        for (String line : lines) {
            // If next line doesn't fit, flush current block
            if (block.length() + line.length() + 1 > DISCORD_MESSAGE_LIMIT) {
                channel.sendMessage(block.toString()).queue();
                block = new StringBuilder(); // no header on subsequent pages
            }
            block.append(line).append("\n");
        }

        // Append footer to the last page if it fits; otherwise send it separately
        if (block.length() + footer.length() > DISCORD_MESSAGE_LIMIT) {
            channel.sendMessage(block.toString()).queue();
            channel.sendMessage(footer).queue();
        } else {
            block.append(footer);
            channel.sendMessage(block.toString()).queue();
        }
    }
}
