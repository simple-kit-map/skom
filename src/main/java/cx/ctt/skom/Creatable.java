package cx.ctt.skom;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
  *   Creatable is a feature of SKM that can be created and managed by players
  *   This includes kits, configs for combat (kb presets), gamerules, minigames
  **/

public abstract interface Creatable {

    static boolean isNotAlphaNumeric(@NotNull String input) {
        for (Character c : input.toCharArray()) {
            if ("_".contains(Character.toString(c))) {
                continue;
            }
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }

    static boolean isNotAlphaNumeric(@NotNull String input, String whitelist) {
        for (Character c : input.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && whitelist.indexOf(c) != 0) {
                return true;
            }
        }
        return false;
    }

    String featureName(); // e.g. kit

    default String getCreatorName(String cName){
        String creatorId = Main.JEDIS.get(featureName() + ':' + cName + ":creator");
        return Main.JEDIS.hget("player_cache", creatorId);
    }

    static boolean exists(String featureName, String name){
        return Main.JEDIS.exists("skm:"+ featureName + ":" + name);
    }

    default void use(CommandSender sender, String name) {
        if (exists(featureName(), name)) {
            sender.sendMessage(String.format("%s '%s' does not exist", featureName(), name));
            sender.sendMessage(featureName() + " '" + name + "' does not exist");
        }
    }

    default void create(Player player, String name){
        if (exists(featureName(), name)){
            player.sendMessage(featureName() + " '" + name + "' already exists");
            return;
        }
        Main.JEDIS.set(featureName() + ':' + name + ":creatorId", player.getUuid().toString());
        Main.JEDIS.set(featureName() + ':' + name + ":id", UUID.randomUUID().toString());
        Main.JEDIS.set(featureName() + ':' + name + ":createdAt", String.valueOf(System.currentTimeMillis()));
    }

}