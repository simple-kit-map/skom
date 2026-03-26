package cx.ctt.skom.commands.content;

import cx.ctt.skom.Creatable;
import cx.ctt.skom.Main;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.Map;
import java.util.Set;

public class MinigameCommand extends Command {
    public MinigameCommand() {
        super("minigame", "mi");
        setDefaultExecutor((sender, commandContext) -> {
            Set<String> minigames = Main.JEDIS.keys("minigame:*");
            sender.sendMessage("");
            sender.sendMessage("available minigames (" + minigames.size() + "):");

            for (String gameruleName : minigames) {
                sender.sendMessage("");
                sender.sendMessage(gameruleName);

                var gamerules = Main.JEDIS.hgetAll(gameruleName);
                for (Map.Entry<String, String> entry : gamerules.entrySet()) {
                    sender.sendMessage(" - " + entry.getKey() + ": " + entry.getValue());
                }
            }
        });
        addSubcommand(new MinigameCreateCommand());
        addSubcommand(new MiniGameSetCommand());
    }

    private static class MiniGameSetCommand extends Command {
        public MiniGameSetCommand() {
            super("set");
            var nameArg = ArgumentType.String("name");
            var keyArg = ArgumentType.String("key");
            var valArg = ArgumentType.String("val");

            addSyntax((sender, ctx) -> {
                String name = ctx.get(nameArg);
                if (Creatable.isNotAlphaNumeric(name)) {
                    sender.sendMessage("non alpha-numeric minigame name");
                    return;
                }
                if (!Main.JEDIS.keys(name).isEmpty()) {
                    sender.sendMessage("minigame does not exist");
                    return;
                }
                String key = ctx.get(keyArg);
                if (Creatable.isNotAlphaNumeric(key)) {
                    sender.sendMessage("non alpha-numeric minigame key");
                    return;
                }
                String val = ctx.get(valArg);
                if (Creatable.isNotAlphaNumeric(val)) {
                    sender.sendMessage("non alpha-numeric minigame val");
                    return;
                }
                Main.JEDIS.hset("minigame:" + name, key, val);
            }, nameArg, keyArg, valArg);
        }
    }

    private static class MinigameCreateCommand extends Command {
        public MinigameCreateCommand() {
            super("new");
            setDefaultExecutor((sender, ctx) -> {
                sender.sendMessage("minigame create <name>");
            });
            var nameArg = ArgumentType.String("name");
            addSyntax((sender, commandContext) -> {
                String name = commandContext.get(nameArg);
                if (Creatable.isNotAlphaNumeric(name)) {
                    sender.sendMessage("minigame name must be alphanumeric");
                    return;
                }
                if (!Main.JEDIS.keys("minigame:" + name).isEmpty()) {
                    sender.sendMessage(name + " already exists");
                    return;
                }
                if (!(sender instanceof Player p)) {
                    sender.sendMessage("minigame must be cread by a player");
                    return;
                }
                Main.JEDIS.hset("minigame:" + name, "by", p.getUuid().toString());
            }, nameArg);
        }
    }
}
