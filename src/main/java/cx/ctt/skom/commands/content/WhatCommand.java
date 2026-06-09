package cx.ctt.skom.commands.content;

import cx.ctt.skom.Main;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public class WhatCommand extends Command {
    public WhatCommand() {
        super("what");
        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player p) {
                sender.sendMessage("\nyou:");
                p.sendMessage(Component.text("name: ").append(p.getName()));
                p.sendMessage(Component.text("username: ").append(Component.text(p.getUsername())));
                p.sendMessage(Component.text("uuid: ").append(Component.text(p.getUuid().toString())));
                p.sendMessage("health: " + p.getHealth());

                var i = p.getInstance();
                p.sendMessage("\ninstance:");
                p.sendMessage("dimension name: " + i.getDimensionName());
                p.sendMessage("skm given name: " + Main.getInstanceName(i));
                p.sendMessage("instance id: " + i.getUuid());
            }
            sender.sendMessage("\navailable instances:");
            for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
                sender.sendMessage(Main.getInstanceName(instance));
            }

        });
    }
}
