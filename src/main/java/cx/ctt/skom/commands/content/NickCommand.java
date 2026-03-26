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

public class NickCommand extends Command {
    public static final String NICK_KEY = "skm:admin:nick";
    void registerNickService(){
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerPreLoginEvent.class, event -> {
            var uuid = event.getGameProfile().uuid();
            String choseNick = Main.JEDIS.hget(NICK_KEY, uuid.toString());
            if (choseNick != null) {
                event.setGameProfile(new GameProfile(uuid, choseNick));
            }
        });
    }
    public NickCommand() {
        super("nick");
        registerNickService();

        var usernameArg = ArgumentType.Word("username");

        addSyntax((sender, commandContext) -> {
            if (sender instanceof Player p) {
                String username = commandContext.get(usernameArg);
                if (Creatable.isNotAlphaNumeric(username)) {
                    sender.sendMessage("invalid nick");
                    return;
                }
                if (username.equalsIgnoreCase("reset")){
                    Main.JEDIS.hdel(NICK_KEY, p.getUuid().toString());
                    return;
                }
                Main.JEDIS.hset(NICK_KEY, p.getUuid().toString(), username);
            }
        }, usernameArg);
    }
}
