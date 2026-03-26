package cx.ctt.skom.commands.admin;

import cx.ctt.skom.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;

import java.io.InputStream;

public class MotdCommand extends Command {
    public static final String MOTD_KEY = "skm:admin:motd";

    static String cacheMotd;
    static String cachedHeader;

    private static final byte[] FAVICON;

    static {
        InputStream inputStream = MotdCommand.class.getResourceAsStream("/server-icon.png");
        byte[] bytes;
        try {
            assert inputStream != null;
            bytes = inputStream.readAllBytes();
        } catch (Exception e) {
            bytes = null;
        }

        FAVICON = bytes;
    }

    static Status.Builder stat = Status.builder().enforcesSecureChat(false).favicon(FAVICON);

    static final String compatWidth = "1.7.2<->" + MinecraftServer.VERSION_NAME;

    public static void registerMotdListener() {
        MinecraftServer.setBrandName("github/simple-kit-map/skom");
        String motdDb = Main.JEDIS.hget(MOTD_KEY, "motd");
        String headerDb = Main.JEDIS.hget(MOTD_KEY, "header");
        cacheMotd = motdDb == null ? "" : motdDb;
        cachedHeader = headerDb == null ? "skm" : headerDb;
        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, event -> {

            int clientVersion = event.getConnection().getProtocolVersion();
            Main.LOG.info("pinged with version: {}", clientVersion);
            int serverVersion;
            if (clientVersion >= 4 && clientVersion <= MinecraftServer.PROTOCOL_VERSION) {
                serverVersion = clientVersion;
            } else {
                serverVersion = -1;
            }
            int maxPlayers = Main.JEDIS.hkeys("player_cache").size(); // how many players have previously joined
            event.setStatus(
                    stat
                            .description(LegacyComponentSerializer.legacyAmpersand().deserialize((cachedHeader + cacheMotd)
                            .replace("\\n", "\n")))
                            .playerInfo(
                                    MinecraftServer.getConnectionManager().getOnlinePlayerCount(),
                                    maxPlayers).versionInfo(new Status.VersionInfo(compatWidth, serverVersion)
                            ).build()
            );
        });
    }

    public MotdCommand() {
        super("motd");
        ArgumentStringArray motdArg = new ArgumentStringArray("motd");
        addSubcommand(new MotdHeaderCommand());
        setDefaultExecutor((sender, _) -> {
            String msg = Main.JEDIS.hget(MOTD_KEY, "motd");
            Component comp = Component.text(msg).clickEvent(ClickEvent.suggestCommand("/motd " + msg));
            sender.sendMessage(comp);
        });
        addSyntax((sender, context) -> {
            if (OpCommand.isNotOp(sender)) {
                sender.sendMessage("no permission");
                return;
            }
            String joinedMotd = String.join(" ", context.get(motdArg));
            Main.JEDIS.hset(MOTD_KEY, "motd", joinedMotd);
            cacheMotd = joinedMotd;
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize((cachedHeader + cacheMotd).replace("\\n", "\n")));
        }, motdArg);
    }

    private static class MotdHeaderCommand extends Command {
        MotdHeaderCommand() {
            super("header");
            ArgumentStringArray headerArg = new ArgumentStringArray("headerMsg");
            setDefaultExecutor((sender, _) -> {
                String msg = Main.JEDIS.hget(MOTD_KEY, "header");
                Component comp = Component.text(msg).clickEvent(ClickEvent.suggestCommand("/motd header " + msg));
                sender.sendMessage(comp);
            });
            addSyntax((sender, context) -> {
                if (OpCommand.isNotOp(sender)) {
                    sender.sendMessage("no permission");
                    return;
                }
                String headerJoined = String.join(" ", context.get(headerArg));
                Main.JEDIS.hset(MOTD_KEY, "header", headerJoined);
                cachedHeader = headerJoined;
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize((cachedHeader + cacheMotd).replace("\\n", "\n")));
            }, headerArg);
        }
    }
}
