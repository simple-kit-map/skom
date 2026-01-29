package cx.ctt.skom.events;

import cx.ctt.skom.Main;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerCommandEvent;

public class SkmCommandEvent {

    public static void register(GlobalEventHandler globalEventHandler) {
        globalEventHandler.addListener(PlayerCommandEvent.class, event -> {

            Player p = event.getPlayer();
            String command = event.getCommand();
            Main.LOG.info("{}.{}/{}", p.getUuid(), p.getUsername(), command);
        });
    }
}
