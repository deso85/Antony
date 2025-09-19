package bot.antony.commands;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import bot.antony.Antony;
import bot.antony.commands.types.ServerCommand;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.utils.FileUpload;

// Flexmark (Markdown -> HTML)
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class ArchiveCmd extends ServerCommand {

    // --------------------------------------------------
    // Constructor & meta
    // --------------------------------------------------
    public ArchiveCmd() {
        super();
        this.privileged = true;
        this.name = "archive";
        this.description = "Archiviert Kanalinhalte als HTML-Datei (Discord-ähnliches Layout).";
        this.shortDescription = "Archiviert Kanalinhalte als HTML.";
        this.example = "#channel 50";
        this.cmdParams.put("#channel (MessageCount)",
                "Archiviert den genannten Kanal. Optional begrenzt (MessageCount). Ohne Limit wird der komplette Verlauf geholt.");
    }

    // --------------------------------------------------
    // Command entry
    // --------------------------------------------------
    @Override
    public void performCommand(Member member, GuildMessageChannel channel, Message message) {
        String[] userMessage = message.getContentDisplay().split(" ");
        List<TextChannel> mentioned = message.getMentions().getChannels(TextChannel.class);

        if (userMessage.length > 1 && !mentioned.isEmpty()) {
            TextChannel archiveChan = mentioned.get(0);

            // Output path
            DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
            String archivePath = Antony.getGuildController().getStoragePath(channel.getGuild())
                    + File.separator + "archive" + File.separator;
            String filePath = archivePath + "#" + archiveChan.getName() + "_"
                    + LocalDateTime.now().format(fileFormatter) + ".html";

            // --- Log Start ---
            Antony.getLogger().info("Archive start for channel {} (#{}) -> {}",
                    archiveChan.getId(), archiveChan.getName(), filePath);

            // Optional limit
            int msgCount = 0;
            if (userMessage.length > 2) {
                try {
                    msgCount = Integer.parseInt(userMessage[2]);
                } catch (NumberFormatException e) {
                    printHelp(channel);
                    return;
                }
            }

            List<Message> msgHistory = getMessageHistory(archiveChan, msgCount);
            if (!msgHistory.isEmpty()) {
                StringBuilder htmlOutput = replaceHtmlContent(
                        getHtmlCodeTemplate(archivePath),
                        getHtmlContentFromMsgHistory(msgHistory),
                        archiveChan
                );
                storeArchiveAsHTML(htmlOutput.toString(), filePath);

                channel.sendMessage("Es wurden **" + msgHistory.size() + "** Nachrichten aus **#"
                        + archiveChan.getName() + "** archiviert.").queue();

                File archiveAttachment = new File(filePath);
                channel.sendFiles(FileUpload.fromData(archiveAttachment)).queue();

                // --- Log Finish ---
                Antony.getLogger().info("Archive finished for channel {} (#{}) -> {} ({} messages)",
                        archiveChan.getId(), archiveChan.getName(), filePath, msgHistory.size());
            } else {
                channel.sendMessage("Keine Nachrichten gefunden.").queue();

                // Optional: Finish-Log auch im 0-Messages-Fall
                Antony.getLogger().info("Archive finished for channel {} (#{}) -> {} (0 messages)",
                        archiveChan.getId(), archiveChan.getName(), filePath);
            }
        } else {
            printHelp(channel);
        }
    }

    // --------------------------------------------------
    // HTML content builder (messages)
    // --------------------------------------------------
    private StringBuilder getHtmlContentFromMsgHistory(List<Message> msgHistory) {
        DateTimeFormatter textFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        StringBuilder htmlContent = new StringBuilder();

        // Markdown pipeline (flexmark)
        Parser parser = buildMarkdownParser();
        HtmlRenderer renderer = buildMarkdownRenderer(); // escapeHtml(true)

        for (Message msg : msgHistory.stream()
                .sorted(Comparator.comparing(Message::getTimeCreated))
                .collect(Collectors.toList())) {

            final var author = msg.getAuthor();
            final String avatarUrl = author.getAvatarUrl() != null ? author.getAvatarUrl() : author.getDefaultAvatarUrl();
            final String username = escapeHtml(author.getName());
            final String date = msg.getTimeCreated().atZoneSameInstant(ZoneId.systemDefault()).format(textFormatter);

            htmlContent.append("<div class=\"message\">");

            // Avatar
            htmlContent.append("<div class=\"icon\">")
                    .append("<img class=\"avatar\" src=\"").append(escapeHtml(avatarUrl)).append("\" alt=\"avatar\">")
                    .append("</div>");

            // Body
            htmlContent.append("<div class=\"body\">");

            // Username + Date (eine Zeile)
            htmlContent.append("<div>");
            htmlContent.append("<span class=\"username\">").append(username).append("</span> ");
            htmlContent.append("<span class=\"date\">").append(escapeHtml(date)).append("</span>");
            htmlContent.append("</div>");

            // Content (Text, Markdown, Emojis)
            htmlContent.append("<div class=\"content\">");
            {
                // 1) Ausgangstext
                String display = msg.getContentDisplay();

                // 2) Flags :flag_xx: -> Unicode
                display = replaceFlagShortcodes(display);

                // 3) Alle Shortcodes -> Unicode (emoji-java)
                display = EmojiParser.parseToUnicode(display);

                // 4) Markdown -> HTML (sicher, HTML wird escaped)
                String htmlFromMd = renderMarkdownToHtml(parser, renderer, display);

                // 5) Custom-Emojis (erst NACH dem Rendern, damit escapeHtml nicht greift)
                htmlFromMd = replaceCustomEmojisInHtml(htmlFromMd, msg);

                htmlContent.append(htmlFromMd);

                // 6) Attachments (Bilder / Videos / Links)
                for (Attachment att : msg.getAttachments()) {
                    String url = att.getUrl();
                    if (isImageUrl(url)) {
                        htmlContent.append("\n<img src=\"").append(escapeHtml(url)).append("\" alt=\"attachment\"/>\n");
                    } else if (isVideoUrl(url)) {
                        htmlContent.append("\n<video controls preload=\"metadata\">")
                                .append("<source src=\"").append(escapeHtml(url)).append("\" type=\"")
                                .append(escapeHtml(guessVideoMimeType(url))).append("\"/>")
                                .append("Your browser does not support the video tag.")
                                .append("</video>\n");
                    } else {
                        htmlContent.append("\n<a href=\"").append(escapeHtml(url)).append("\">")
                                .append(escapeHtml(url)).append("</a>\n");
                    }
                }
            }
            htmlContent.append("</div>"); // .content

            htmlContent.append("</div>"); // .body
            htmlContent.append("</div>"); // .message
        }
        return htmlContent;
    }

    // --------------------------------------------------
    // Storage & template
    // --------------------------------------------------
    private void storeArchiveAsHTML(String htmlCode, String filePath) {
        try {
            File out = new File(filePath);
            File parent = out.getParentFile();
            if (parent != null && !parent.exists()) {
                //noinspection ResultOfMethodCallIgnored
                parent.mkdirs();
            }
            try (OutputStream fos = new FileOutputStream(out);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 BufferedWriter writer = new BufferedWriter(osw)) {
                writer.append(htmlCode);
            }
        } catch (IOException e) {
            Antony.getLogger().error("Failed to write archive HTML to {}", filePath, e);
        }
    }

    private StringBuilder getHtmlCodeTemplate(String archivePath) {
        StringBuilder htmlCode = new StringBuilder();
        try {
            File directory = new File(archivePath);
            if (!directory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                directory.mkdirs();
            }

            try (InputStream is = Antony.class.getResourceAsStream("/archive_template.html");
                 InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {

                String line;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (!first) htmlCode.append("\n");
                    htmlCode.append(line);
                    first = false;
                }
            }
        } catch (IOException e) {
            Antony.getLogger().error("Failed to read archive template", e);
        }
        return htmlCode;
    }

    private List<Message> getMessageHistory(TextChannel channel, int limit) {
        MessageHistory history = channel.getHistory();

        if (limit > 0) {
            int remaining = limit;
            while (remaining > 0) {
                int toFetch = Math.min(remaining, 100);
                history.retrievePast(toFetch).complete();
                remaining -= toFetch;
            }
        } else {
            int historySize;
            do {
                historySize = history.getRetrievedHistory().size();
                history.retrievePast(100).complete();
            } while (historySize != history.getRetrievedHistory().size());
        }
        return history.getRetrievedHistory();
    }

    private StringBuilder replaceHtmlContent(StringBuilder htmlCode, StringBuilder htmlContent, TextChannel archiveChan) {
        htmlCode = replaceAll(htmlCode, "REPLACESERVERNAME", archiveChan.getGuild().getName());
        String catName = (archiveChan.getParentCategory() != null) ? archiveChan.getParentCategory().getName() : "-";
        htmlCode = replaceAll(htmlCode, "REPLACECATEGORY", catName);
        htmlCode = replaceAll(htmlCode, "REPLACECHANNELNAME", archiveChan.getName());
        htmlCode = replaceAll(htmlCode, "REPLACECONTENT", htmlContent.toString());
        return htmlCode;
    }

    /**
     * Ersetzt Platzhalter LITERAL (ohne Regex) – sicher für $ und \ in den Replacement-Strings.
     */
    private static StringBuilder replaceAll(StringBuilder sb, String find, String replace) {
        String text = sb.toString();
        text = text.replace(find, replace);
        return new StringBuilder(text);
    }

    // --------------------------------------------------
    // Emoji helpers
    // --------------------------------------------------

    /** Mappt :flag_xx: → Unicode-Flagge (🇩🇪 etc.). */
    private static String replaceFlagShortcodes(String s) {
        if (s == null || s.isBlank()) return s;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(":flag_([a-z]{2}):", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(s);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String cc = m.group(1).toUpperCase(Locale.ROOT);
            if (cc.length() == 2 &&
                    cc.charAt(0) >= 'A' && cc.charAt(0) <= 'Z' &&
                    cc.charAt(1) >= 'A' && cc.charAt(1) <= 'Z') {
                int base = 0x1F1E6;
                int cp1 = base + (cc.charAt(0) - 'A');
                int cp2 = base + (cc.charAt(1) - 'A');
                String flag = new String(Character.toChars(cp1)) + new String(Character.toChars(cp2));
                m.appendReplacement(out, java.util.regex.Matcher.quoteReplacement(flag));
            } else {
                m.appendReplacement(out, java.util.regex.Matcher.quoteReplacement(m.group(0)));
            }
        }
        m.appendTail(out);
        return out.toString();
    }

    /** Ersetzt Custom-Emoji-Shortcodes im fertigen HTML durch <img class="emoji" src="..."> */
    private static String replaceCustomEmojisInHtml(String html, Message msg) {
        if (html == null || html.isEmpty()) return html;
        String result = html;
        for (CustomEmoji em : msg.getMentions().getCustomEmojis()) {
            String shortcode = ":" + em.getName() + ":";
            String url = getEmojiImageUrl(em);
            if (url != null) {
                String img = "<img class=\"emoji\" src=\"" + escapeHtml(url) + "\" alt=\"" + escapeHtml(shortcode) + "\"/>";
                result = result.replace(shortcode, img);
            }
        }
        return result;
    }

    /** Liefert die Emoji-Bild-URL. Für RichCustomEmoji direkt, sonst via CDN-Pattern. */
    private static String getEmojiImageUrl(CustomEmoji em) {
        if (em == null) return null;
        if (em instanceof RichCustomEmoji) {
            String url = ((RichCustomEmoji) em).getImageUrl();
            if (url != null && !url.isEmpty()) return url;
        }
        String id = em.getId();
        if (id == null || id.isEmpty()) return null;
        String ext = em.isAnimated() ? "gif" : "png";
        return "https://cdn.discordapp.com/emojis/" + id + "." + ext;
    }

    // --------------------------------------------------
    // URL / media helpers (Java 11 kompatibel)
    // --------------------------------------------------
    private static boolean isImageUrl(String url) {
        String ext = getUrlFileExtension(url);
        if (ext == null) return false;
        if (ext.equals("jpeg")) return true;
        if (ext.equals("jpg")) return true;
        if (ext.equals("png")) return true;
        if (ext.equals("svg")) return true;
        if (ext.equals("webp")) return true;
        if (ext.equals("gif")) return true;
        if (ext.equals("tiff")) return true;
        if (ext.equals("bmp")) return true;
        if (ext.equals("ico")) return true;
        if (ext.equals("apng")) return true;
        if (ext.equals("avif")) return true;
        return false;
    }

    private static boolean isVideoUrl(String url) {
        String ext = getUrlFileExtension(url);
        if (ext == null) return false;
        if (ext.equals("mp4")) return true;
        if (ext.equals("webm")) return true;
        if (ext.equals("mov")) return true;
        if (ext.equals("m4v")) return true;
        if (ext.equals("avi")) return true;
        if (ext.equals("mkv")) return true;
        if (ext.equals("gifv")) return true;
        return false;
    }

    private static String guessVideoMimeType(String url) {
        String ext = getUrlFileExtension(url);
        if (ext == null) return "video/mp4";
        if (ext.equals("webm")) return "video/webm";
        if (ext.equals("mov")) return "video/quicktime";
        if (ext.equals("m4v")) return "video/x-m4v";
        if (ext.equals("avi")) return "video/x-msvideo";
        if (ext.equals("mkv")) return "video/x-matroska";
        if (ext.equals("gifv")) return "video/mp4"; // häufig mp4 container
        return "video/mp4";
    }

    /** Dateiendung aus URL **ohne** Query-Params (…/file.jpg?ex=… → "jpg"). */
    private static String getUrlFileExtension(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            URI uri = new URI(url);
            String path = uri.getPath(); // ohne Query
            if (path == null) return null;
            int dot = path.lastIndexOf('.');
            if (dot == -1 || dot == path.length() - 1) return null;
            return path.substring(dot + 1).toLowerCase(Locale.ROOT);
        } catch (URISyntaxException e) {
            String path = url.split("\\?")[0];
            int dot = path.lastIndexOf('.');
            if (dot == -1 || dot == path.length() - 1) return null;
            return path.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
    }

    // --------------------------------------------------
    // Markdown rendering
    // --------------------------------------------------
    private static Parser buildMarkdownParser() {
        MutableDataSet opts = new MutableDataSet();
        opts.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create()
        ));
        return Parser.builder(opts).build();
    }

    private static HtmlRenderer buildMarkdownRenderer() {
        MutableDataSet opts = new MutableDataSet();
        // HTML aus User-Text wird escaped (sicher).
        // Custom-Emoji-<img> fügen wir erst NACH dem Rendern ein.
        return HtmlRenderer.builder(opts).escapeHtml(true).build();
    }

    private static String renderMarkdownToHtml(Parser parser, HtmlRenderer renderer, String markdown) {
        if (markdown == null || markdown.isEmpty()) return "";
        return renderer.render(parser.parse(markdown));
    }

    // --------------------------------------------------
    // Utils
    // --------------------------------------------------
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
