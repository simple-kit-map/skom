package cx.ctt.skom.commands.content;

import cx.ctt.skom.Main;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HexFormat;
import java.util.UUID;
import java.util.logging.Level;

public class LoadPackCommand extends Command {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public LoadPackCommand() {
        super("loadpack");

        ArgumentString urlArg = ArgumentType.String("url");

        addSyntax(this::executeLoadPack, urlArg);
    }

    private void executeLoadPack(CommandSender sender, CommandContext ctx) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        String urlStr = ctx.get("url");
        URI url;
        try {
            url = URI.create(urlStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid URL.");
            return;
        }

        player.sendMessage("Downloading resource pack...");

        String hash = downloadAndHash(sender, url);
        if (hash == null) {
            player.sendMessage("Failed to download resource pack.");
            return;
        }
        ResourcePackInfo packInfo = ResourcePackInfo.resourcePackInfo(UUID.nameUUIDFromBytes(hash.getBytes()), url, hash);

        player.sendResourcePacks(packInfo);
        player.sendMessage("Resource pack set! Hash: " + hash);
    }

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static String downloadAndHash(CommandSender sender, URI url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                sender.sendMessage("Failed to download resource pack: response " + response.statusCode());
                return null;
            }

            byte[] content = response.body();
            return SHAsum(content);
        } catch (Exception e) {
            Main.LOG.error("Failed to download resource pack. ", e);
            return null;
        }
    }
    public static String SHAsum(byte[] convertme) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(convertme));
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}