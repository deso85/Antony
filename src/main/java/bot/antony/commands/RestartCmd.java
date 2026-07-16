package bot.antony.commands;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestartCmd extends ServerCommand {

    private static final Logger logger = LoggerFactory.getLogger(RestartCmd.class);
    private static final String RESTART_SCRIPT_PROP = "bot.restart.script";
    private static final String RESTART_SCRIPT_DEFAULT = "~/bin/antony.sh";

    public RestartCmd() {
        super();
        this.privileged = true;
        this.name = "restart";
        this.description = "Startet Antony neu.";
        this.shortDescription = "Startet Antony neu.";
        this.example = "[script]";
        this.cmdParams.put("<script>", "Optionaler Pfad zum Restart-Script");
    }

    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {
        logger.info("Restart command executed by user '{}' (UID: {}) in channel '#{}'",
                member.getUser().getName(),
                member.getUser().getId(),
                channel.getName());

        // Bestätigung senden
        channel.sendMessage("🔄 **Antony wird neu gestartet...**").queue();

        String script = getProperty();

        new Thread(() -> {
            try {
                // kurze Pause damit Nachricht sicher rausgeht
                Thread.sleep(1000);

                // 🔥 Restart-Script triggern (nicht blockierend!)
                executeRestartScript(script);

                // Timer-Threads aufwecken
                logger.info("Interrupting all timer threads for restart...");
                Antony.interruptTimerThreads();

                // JDA sauber herunterfahren
                var jda = Antony.getJda();
                if (jda != null) {
                    jda.getPresence().setStatus(OnlineStatus.OFFLINE);
                    logger.info("Shutting down JDA for restart...");
                    jda.shutdown();
                }

            } catch (Exception e) {
                logger.error("Error during restart sequence.", e);
            }
        }, "antony-restart").start();
    }

    private void executeRestartScript(String script) {
        // ~ expandieren
        if (script.startsWith("~/")) {
            String home = System.getProperty("user.home");
            script = home + script.substring(1);
        }

        logger.info("Executing restart script: {} -c restart", script);

        try {
            ProcessBuilder pb;

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", script, "-c", "restart");
            } else {
                pb = new ProcessBuilder(script, "-c", "restart");
            }

            pb.redirectErrorStream(true);

            // ✅ Kein Blockieren durch Output
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);

            pb.start();

            logger.info("Restart script triggered successfully (detached).");

        } catch (Exception e) {
            logger.error("Failed to execute restart script: {}", script, e);
        }
    }

    private String getProperty() {
        String script = Antony.getProperty(RESTART_SCRIPT_PROP);
        return (script != null && !script.isEmpty()) ? script : RESTART_SCRIPT_DEFAULT;
    }
}