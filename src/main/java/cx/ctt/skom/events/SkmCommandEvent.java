package cx.ctt.skom.events;

import cx.ctt.skom.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerCommandEvent;

public class SkmCommandEvent {

    public static void register(GlobalEventHandler globalEventHandler) {
        globalEventHandler.addListener(PlayerCommandEvent.class, event -> {

            String command = event.getCommand();
            if (command.isEmpty() || command.equalsIgnoreCase("dash")) return;
            Player p = event.getPlayer();
            Main.LOG.info(ANSIComponentSerializer.ansi().serialize(
                    Component.text("PLAYER.COMMAND." + event.getPlayer().getUuid() + "." +  event.getPlayer().getUsername() + "/" + command)
                            .color(NamedTextColor.BLUE)
                    )
            );
        });
    }
}
