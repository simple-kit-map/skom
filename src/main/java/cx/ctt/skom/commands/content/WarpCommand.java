package cx.ctt.skom.commands.content;

import cx.ctt.skom.Creatable;
import cx.ctt.skom.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.SimpleCommand;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class WarpCommand extends SimpleCommand {

    public WarpCommand() {
        super("warp", "w");
    }

    public static @Nullable Path getMapPath(String mapName, CommandSender sender) {
        Path path = java.nio.file.Paths.get(Main.MAP_PATH.toString(), mapName);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            Main.LOG.error("{} was attempted to be warped to", path);
            sender.sendMessage("no warp found `" + mapName + "`, available:");
            for (File file : Objects.requireNonNull(new File(Main.MAP_PATH.toString()).listFiles())) {
                sender.sendMessage(file.getName());
            }
            return null;
        }
        return path;
    }

    public static @Nullable InstanceContainer loadWorld(@Nullable CommandSender sender, String mapName, Path mapPath){
        var al = new AnvilLoader(mapPath);
        InstanceContainer targetInstance = MinecraftServer.getInstanceManager().createInstanceContainer(
                DimensionType.OVERWORLD,
                al);
        targetInstance.setChunkSupplier(LightingChunk::new);
        Main.INSTANCES.put(mapName, targetInstance);

        return targetInstance;
    }

    @Override
    public boolean process(@NotNull CommandSender sender, @NotNull String command, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("be a user");
            return true;
        }

        if (args.length == 0 || Objects.equals(args[0], "list")) {
            sender.sendMessage("");
            sender.sendMessage(Component.text("loaded instances:").color(NamedTextColor.GREEN));
            for (String instanceName : Main.INSTANCES.keySet()){
                sender.sendMessage(Component.text(" - " + instanceName)
                        .clickEvent(ClickEvent.runCommand("warp " + instanceName))
                        .hoverEvent(HoverEvent.showText(Component.text("/warp " + instanceName)))
                );
            }
            sender.sendMessage(Component.text("in map folder:").color(NamedTextColor.GREEN));
            for (File mapFile : Objects.requireNonNull(Main.MAP_PATH.toFile().listFiles())){
                var mapName = mapFile.getName();
                sender.sendMessage(Component.text(" - " + mapName)
                        .clickEvent(ClickEvent.runCommand("warp " + mapName))
                        .hoverEvent(HoverEvent.showText(Component.text("/warp " + mapName)))
                );
            }
            return true;
        }
        if (Creatable.isNotAlphaNumeric(args[0])) {

            sender.sendMessage("Invalid warp '" + args[0] + "'");
            return false;
        }
        InstanceContainer targetInstance = Main.INSTANCES.get(args[0]);
        if (targetInstance == null) {
            String mapName = args[0];
            Path mPath = getMapPath(mapName, player);
            targetInstance = loadWorld(sender, mapName, mPath);
            if (targetInstance == null) return false;
            /*
            Path path = java.nio.file.Paths.get(Main.MAP_PATH.toString(), args[0]);
            if (Files.exists(path) && Files.isDirectory(path)) {
                sender.sendMessage(path.toAbsolutePath().normalize().toString());

                var al = new AnvilLoader(Paths.get(String.valueOf(Main.MAP_PATH), args[0]));
                targetInstance = this.instanceManager.createInstanceContainer(
                        DimensionType.OVERWORLD,
                        al);
                targetInstance.setChunkSupplier(LightingChunk::new);
                Main.INSTANCES.put(args[0], targetInstance);

            } else {
                Main.LOG.error("{} was attempted to be warped to", path);
                sender.sendMessage("no warp found " + args[0] + ", available:");
                for (File file : Objects.requireNonNull(new File(Main.MAP_PATH.toString()).listFiles())) {
                                        sender.sendMessage(file.getName());
                }
                return false;
            }*/
        }
        if (player.getInstance() != targetInstance) {
            player.setInstance(targetInstance);
        }
//        var spawn = targetInstance.tagHandler().asCompound().getCompound("Data").getCompound("spawn").getIntArray("pos");
//        ((Player) sender).teleport(new Pos(spawn[0], spawn[1], spawn[2]));
        return true;
    }

    @Override
    public boolean hasAccess(@NotNull CommandSender sender, @Nullable String commandString) {
        return true;
    }
}
