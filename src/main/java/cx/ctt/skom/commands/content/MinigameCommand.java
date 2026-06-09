package cx.ctt.skom.commands.content;

import cx.ctt.skom.Creatable;
import cx.ctt.skom.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class MinigameCommand extends Command {
    static final String PREFIX = "minigame:";

    final public static Argument<String> MinigameArg = ArgumentType.String("minigame")
            .setSuggestionCallback((sender, context, suggestion) -> {

        Main.JEDIS.keys(PREFIX + "*").stream().filter(s -> {
            String typed = context.get("minigame");
            if (typed.trim().isEmpty()) return true;
            return s.toLowerCase().replaceFirst(PREFIX, "").startsWith(typed.toLowerCase());
        }).forEach(s -> {
            suggestion.addEntry(new SuggestionEntry(s.replaceFirst(PREFIX, "")));
        });
    });

    public MinigameCommand() {
        super("minigame", "mi");
        setDefaultExecutor((sender, context) -> {

            Set<String> minigames = Main.JEDIS.keys(PREFIX + "*");
            sender.sendMessage("");
            sender.sendMessage("available minigames (" + minigames.size() + "):");

            // tl;dr: this pieces of spaghetti prints it pretty, title and key: value for each
            for (String minigameName : minigames) {
                sender.sendMessage("");
                sender.sendMessage("§b- §r§l" + minigameName.replaceFirst(PREFIX, "") + "§r");

                var gamerules = Main.JEDIS.hgetAll(minigameName);
                for (Map.Entry<String, String> gamerule : gamerules.entrySet()) {
                    Component msg = Component.text("§b" + gamerule.getKey() + "§r: " + gamerule.getValue());
                    String cleanGameRuleName = Arrays.stream(gamerule.getKey().split("\\.", 1)).findFirst().orElse(gamerule.getKey());
                    if (!Creatable.isNotAlphaNumeric(gamerule.getKey())) {
                        msg = msg.clickEvent(ClickEvent.openUrl(Main.skomSettings.get("gamerule_url").replace("{0}", cleanGameRuleName)));
                    }
                    sender.sendMessage(msg);
                }
            }
        });
        addSubcommand(new MinigameCreateCommand());
        addSubcommand(new MiniGameSetCommand());
        addSubcommand(new MinigameDelRuleCommand());
    }

    private static class MinigameDelRuleCommand extends Command {
        public MinigameDelRuleCommand() {
            super("delrule");

            var GameruleArg = ArgumentType.String("gamerule");

            addSyntax((sender, context) -> {
                var minigame = context.get(MinigameArg);
                if (!Main.JEDIS.exists(PREFIX + minigame)) {
                    sender.sendMessage("minigame " + minigame + "not found");
                    return;
                }
                var gamerule = context.get(GameruleArg);
                if (!Main.JEDIS.hexists(PREFIX + minigame, gamerule)) {
                    sender.sendMessage("gamerule " + gamerule + "not found");
                    return;
                }
                Main.JEDIS.hdel(PREFIX + minigame, gamerule);
            }, MinigameArg, GameruleArg);
        }
    }

    private static class MiniGameSetCommand extends Command {
        public MiniGameSetCommand() {
            super("set");
            var keyArg = ArgumentType.String("key");
            var valArg = ArgumentType.StringArray("val");

            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("empty set subcommand");
            });

            addSyntax((sender, context) -> {
                String name = context.get(MinigameArg);
                if (Creatable.isNotAlphaNumeric(name)) {
                    sender.sendMessage("non alpha-numeric minigame name");
                    return;
                }
                if (Main.JEDIS.keys(PREFIX + name).isEmpty()) {
                    sender.sendMessage(Component.text("minigame `" + name + "` does not exist, click this to create one:").appendNewline().append(Component.text("/mi create " + name).style(Style.style(NamedTextColor.BLUE)).clickEvent(ClickEvent.runCommand("/mi create " + name))));
                    return;
                }
                String key = context.get(keyArg);
                if (Creatable.isNotAlphaNumeric(key)) {
                    sender.sendMessage("non alpha-numeric minigame key");
                    return;
                }
                String val = String.join(" ", context.get(valArg));
                if (Creatable.isNotAlphaNumeric(val, " ")) {
                    sender.sendMessage("non alpha-numeric minigame val");
                    return;
                }
                Main.JEDIS.hset(PREFIX + name, key, val);
            }, MinigameArg, keyArg, valArg);
        }
    }

    private static class MinigameCreateCommand extends Command {
        public MinigameCreateCommand() {
            super("new");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("minigame create <name>");
            });
            var nameArg = ArgumentType.String("name");
            addSyntax((sender, context) -> {
                String name = context.get(nameArg);
                if (Creatable.isNotAlphaNumeric(name)) {
                    sender.sendMessage("minigame name must be alphanumeric");
                    return;
                }
                if (!Main.JEDIS.keys(PREFIX + name).isEmpty()) {
                    sender.sendMessage(name + " already exists");
                    return;
                }
                if (!(sender instanceof Player p)) {
                    sender.sendMessage("minigame must be cread by a player");
                    return;
                }
                Main.JEDIS.hset(PREFIX + name, "by", p.getUuid().toString());
            }, nameArg);
        }
    }
}
