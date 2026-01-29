package cx.ctt.skom;

import cx.ctt.skom.Main;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

import java.util.UUID;

/**
  *   Creatable is a features of SKM that can be created and managed by players
  *   This includes kits, configs for combat (kb presets), gamerules, minigames
  *
  *
  **/

public abstract class Creatable extends Command {

    public static String featureName; // e.g. Kit

    public String getCreatorName(String cName){
        String creatorId = Main.JEDIS.get(featureName + ':' + cName + ":creator");
        return Main.JEDIS.hget("player_cache", creatorId);
    }

    public boolean exists(String name){
        return !Main.JEDIS.keys(featureName + ':' + name).isEmpty();
    }

    public void use(CommandSender sender, String name) {
        if (exists(name)) {
            sender.sendMessage(String.format("%s '%s' does not exist", featureName, name));
            sender.sendMessage(featureName + " '" + name + "' does not exist");
        }
    }

    public void create(Player player, String name){
        if (exists(name)){
            player.sendMessage(featureName + " '" + name + "' already exists");
            return;
        }
        Main.JEDIS.set(featureName + ':' + name + ":creatorId", player.getUuid().toString());
        Main.JEDIS.set(featureName + ':' + name + ":id", UUID.randomUUID().toString());
        Main.JEDIS.set(featureName + ':' + name + ":createdAt", String.valueOf(System.currentTimeMillis()));
    }

    public Creatable(String name, String... aliases) {
        super(name, aliases);
        Creatable.featureName = name;
    }
}