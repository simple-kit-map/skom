package cx.ctt.skom.commands.content;

import cx.ctt.skom.Creatable;
import cx.ctt.skom.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.event.player.PlayerSkinInitEvent;
import net.minestom.server.network.player.GameProfile;

public class SkinCommand extends Command {
    public static final String SKIN_KEY = "skm:feature:skin";
    void registerNickService(){
        MinecraftServer.getGlobalEventHandler().addListener(PlayerSkinInitEvent.class, event -> {
            var uuid = event.getPlayer().getUuid();
            String choseSkin = Main.JEDIS.hget(SKIN_KEY, uuid.toString());
            if (choseSkin != null) {
                event.setSkin(PlayerSkin.fromUsername(choseSkin));
            }
        });
    }
    public SkinCommand() {
        super("skin");
        registerNickService();

        var usernameArg = ArgumentType.Word("username");

        addSyntax((sender, commandContext) -> {
            if (sender instanceof Player p) {
                String username = commandContext.get(usernameArg);
                if (Creatable.isNotAlphaNumeric(username)) {
                    sender.sendMessage("invalid username");
                    return;
                }
                if (username.equalsIgnoreCase("reset")){
                    Main.JEDIS.hdel(SKIN_KEY, p.getUuid().toString());
                    return;
                }
                Main.JEDIS.hset(SKIN_KEY, p.getUuid().toString(), username);
            }
        }, usernameArg);
    }
}
