package cx.ctt.skom.commands;

import cx.ctt.skom.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;

import java.util.*;

public class UnknownCommand {
    public static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j; // Deletion
                } else if (j == 0) {
                    dp[i][j] = i; // Insertion
                } else {
                    int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost); // Substitution
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    public static List<String> suggestCommands(String input, List<String> availableCommands) {
        List<String> suggestions = new ArrayList<>();
        int threshold = 2; // Set a distance threshold; adjust as necessary

        for (String command : availableCommands) {
            int distance = levenshteinDistance(input, command);
            if (distance <= threshold) {
                suggestions.add(command);
            }
        }
        return suggestions;
    }

    public static final Set<String> commands = new HashSet<>() {{
        MinecraftServer.getCommandManager().getCommands().forEach(c ->
                addAll(List.of(c.getNames()))
        );
    }};

    public static void register(CommandSender sender, String command) {
//        if (sender instanceof Player p){
//            Main.LOG.info("{}.{}/{}", p.getUuid(), p.getUsername(), command);
//        }
        List<String> similar = suggestCommands(
                Arrays.stream(command.replace("/", "").split(" ")).findFirst().get(),
                commands.stream().toList());
        if (similar.isEmpty()) {
            sender.sendMessage(
                    Component.text("Unknown command ", NamedTextColor.RED)
                            .append(Component.text("`", NamedTextColor.BLACK))
                            .append(Component.text(command, NamedTextColor.GRAY))
                            .append(Component.text("`", NamedTextColor.BLACK))
            );
        }
        else {
            sender.sendMessage(Component.text("Unknown command, did you mean:", NamedTextColor.RED));
            for (String s : similar) {
                sender.sendMessage(Component.text(" " + s, NamedTextColor.GRAY));
            }
        }
    }
}
