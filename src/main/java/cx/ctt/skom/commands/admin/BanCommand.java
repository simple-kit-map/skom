package cx.ctt.skom.commands.admin;

import cx.ctt.skom.Main;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.schemafields.NumericField;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.search.schemafields.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BanCommand extends Command {
    private static final String INDEX = "idx:bans";
    private static final String PREFIX = "skm:bans:";
    private final static RedisClient jedis = Main.JEDIS;

    ArgumentEntity targetArg = ArgumentType.Entity("target").onlyPlayers(true);

    public BanCommand() {
        super("ban");
        setCondition((sender, _) -> !OpCommand.isNotOp(sender));
        setDefaultExecutor((sender, _) -> {
            status("*");
        });
        addSubcommand(new BanAddCommand());
        addSubcommand(new BanRevokeCommand());
        addSubcommand(new BanStatusCommand());
    }

    class BanAddCommand extends Command {
        public BanAddCommand() {
            createIndexIfMissing();
            super("add");
            var reasonArg = ArgumentType.String("reason");
            addSyntax((sender, context) -> {
                var player = context.get(targetArg).findFirstPlayer(sender);
                if (player == null) {
                    sender.sendMessage("Player not found");
                    return;
                }
                String reason = context.get(reasonArg);
                String bannerName;
                if (sender instanceof Player p){
                    bannerName = p.getUsername();
                } else bannerName = "console";
                ban(player.getUuid(), player.getUsername(), bannerName, reason);
                sender.sendMessage("banned " + player.getUsername() + " for reason " + reason);
                player.kick("Timed out");
                player.getPlayerConnection().disconnect();
            }, targetArg, reasonArg);
        }
    }

    class BanStatusCommand extends Command {
        public BanStatusCommand() {
            super("status");
            var bannedPlayerArg = ArgumentType.String("player");
            addSyntax((sender, context) -> {
                var player = context.get(bannedPlayerArg);
                sender.sendMessage(status(player));

            }, bannedPlayerArg);
        }
    }

    class BanRevokeCommand extends Command {
        public BanRevokeCommand() {
            super("revoke");
            var bannedPlayerArg = ArgumentType.String("player");
            addSyntax((sender, context) -> {
                var player = context.get(bannedPlayerArg);
                var pastBans = findByUsername(player);
                if (pastBans == null || pastBans.isEmpty()) {
                    sender.sendMessage("Player not found in ban list");
                    return;
                }
                pastBans.forEach(stringStringMap -> {
                    var uuid =  stringStringMap.get("uuid");
                    if (uuid == null) {
                        sender.sendMessage("player uuid not found in map");
                    } else {
                        sender.sendMessage("");
                        sender.sendMessage("revoking the following ban:");
                        stringStringMap.forEach((s, s2) -> {
                            sender.sendMessage(s + ": " + s2);
                        });
                        Main.JEDIS.del(PREFIX + uuid);
                    }
                });
            }, bannedPlayerArg);
        }
    }

    private void createIndexIfMissing() {
        try {
            jedis.ftInfo(INDEX);
        } catch (Exception ignored) {
            jedis.ftCreate(INDEX,
                    FTCreateParams.createParams()
                            .on(IndexDataType.HASH)
                            .prefix(PREFIX),
                    TagField.of("uuid"),
                    TextField.of("bannedBy"),
                    TextField.of("username"),
                    TextField.of("reason"),
                    NumericField.of("date").sortable());
        }
    }

    public void ban(UUID uuid, String username, String bannedBy, String reason) {
        Map<String, String> fields = Map.of(
                "uuid", uuid.toString(),
                "username", username,
                "reason", reason,
                "bannedBy", bannedBy,
                "date", String.valueOf(System.currentTimeMillis())
        );
        jedis.hset(PREFIX + uuid, fields);
    }


    public String status(String player) {
        var sb = new StringBuilder();
        AtomicInteger found = new AtomicInteger();
        var bans = findByUsername(player);

        bans.forEach(stringStringMap -> {
            found.getAndIncrement();
            if (stringStringMap != null) {
                stringStringMap.forEach((s, s2) -> {
                    if (s.equals("date"))
                        s2 += " (" + UptimeCommand.formatRelativeTime(Long.parseLong(s2)) + " ago)";
                    sb.append(s + ": " + s2 + "\n");
                });
            } else {
                sb.append("player is not banned");
            }
        });
        if (found.get() == 0) {
            sb.append("found no ban status for player " + player);
        }
        return sb.toString();
    }

    public static boolean isBanned(UUID uuid) {
        return jedis.exists(PREFIX + uuid);
    }

    public List<Map<String, String>> findByUuid(UUID uuid) {
        return search("@uuid:{" + escapeTag(uuid.toString()) + "}");
    }

    public List<Map<String, String>> findByUsername(String username) {
        String safe = escapeText(username);
        if (username.equals("*")) safe = "*";
        return search("@username:" + safe);
    }

    private List<Map<String, String>> search(String query) {
        SearchResult result = jedis.ftSearch(INDEX, new Query(query).setSortBy("date", true));
        return result.getDocuments().stream()
                .map(d -> {
                    Map<String, String> map = new HashMap<>();
                    d.getProperties().forEach(e -> map.put(e.getKey(), String.valueOf(e.getValue())));
                    return map;
                })
                .collect(Collectors.toList());
    }

    private static String escapeTag(String value) {
        return value.replaceAll("([\\-@.<>{}\\[\\]\"':;!,?*\\s])", "\\\\$1");
    }

    private static String escapeText(String value) {
        return value.replaceAll("([@.<>{}\\[\\]\"':;!,?*\\s])", "\\\\$1");
    }
}
