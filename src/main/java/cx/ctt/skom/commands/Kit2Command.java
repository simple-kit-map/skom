package cx.ctt.skom.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cx.ctt.skom.Main;
import cx.ctt.skom.events.I;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlocksAttacks;
import net.minestom.server.registry.RegistryTranscoder;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.json.Path2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Kit2Command extends Command {

    private static final Transcoder<JsonElement> REGISTRY_JSON_TRANSCODER = new RegistryTranscoder<>(Transcoder.JSON, MinecraftServer.process());

    public static @NotNull JsonObject serializeItem(ItemStack itemStack) {
        final var itemData = new JsonObject();
        itemData.addProperty("type", itemStack.material().key().asMinimalString());
        itemData.addProperty("count", itemStack.amount());

        final var components = new JsonObject();
        for (final var entry : itemStack.componentPatch().entrySet()) {
            final var component = entry.component();
            final var value = entry.value();
            final var result = ((DataComponent) component).encode(REGISTRY_JSON_TRANSCODER, value);

            if (result instanceof Result.Ok<?>(var encodedValue)) {
                final var key = component.key().asMinimalString();
                components.add(key, (JsonElement) encodedValue);
                Main.LOG.debug("Serialized component {} -> {}", key, encodedValue);
            } else if (result instanceof Result.Error<?>(String message)) {
                Main.LOG.error("Failed to serialize component {}: {}", component.key(), message);
            }
        }

        if (!components.isEmpty()) {
            itemData.add("components", components);
        }

        return itemData;
    }

    void createKit(CommandSender sender, String kitName) {
        if (!(sender instanceof Player player)){
            sender.sendMessage("Only players can use this command");
            return;
        }

        String reason = I.isTaken(kitName);
        if (I.isTaken(kitName) != null) {
            player.sendMessage("failed to create kit: " + reason);
            return;
        }

        String kitKey = "skm:kit:"+kitName;
        if (!Main.JEDIS.keys(kitKey).isEmpty()) {
            player.sendMessage(kitName + " is a kit that already exists");
            return;
        }
        ItemStack[] items = player.getInventory().getItemStacks();
        assert items.length == 46 : "Invalid inventory length";
        Main.JEDIS.jsonSet(kitKey, Path2.ROOT_PATH, "{}");

        HashMap<ItemStack, List<Integer>> dedupMap = new HashMap<>();
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack item = items[slot];
            if (item == null || item.isAir()) {
                continue;
            }
            if (!dedupMap.containsKey(item)) {
                List<Integer> newSlot = new ArrayList<>();
                newSlot.add(slot);
                dedupMap.put(item, newSlot);
            } else {
                dedupMap.get(item).add(slot);
            }
        }
        dedupMap.forEach((item, slots) -> {
            Result<JsonElement> element = ItemStack.CODEC.encode(Transcoder.JSON, item);
            Main.JEDIS.jsonSet(kitKey, Path2.of("$.slot" + slots.stream().map(Object::toString).collect(Collectors.joining("-"))), element.orElseThrow().getAsJsonObject());
        });
        Main.JEDIS.jsonSet(kitKey, Path2.of("$.created"), Long.toString(System.currentTimeMillis()));
        Main.JEDIS.jsonSet(kitKey, Path2.of("$.kitauthor"), '"' + player.getUuid().toString() + '"');

    }

    static boolean kitExists(String kitName){
        return !Main.JEDIS.keys("skm:kit:"+kitName).isEmpty();
    }



    static public void loadKit(CommandSender sender, String kitName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players gets kits!! you dont have an inventory mr console!!");
            return;
        }

        if (!kitExists(kitName)){
            player.sendMessage("kit " + kitName + " does not exist");
            return;
        }
        String kitKey = "skm:kit:"+kitName;
        player.getInventory().clear();

        List<List<String>> keys = Main.JEDIS.jsonObjKeys(kitKey, Path2.of("$"));
        deresializeToInv(keys, player.getInventory(), kitKey);
        player.refreshCommands();
        player.getInventory().update();
    }

    static void deresializeToInv(List<List<String>> keys, PlayerInventory inv, String kitKey){
        Gson gson = new Gson();
        if (keys.size() != 1) {
            throw new IllegalStateException("Multiple object keys found");
        }
        List<String> slots = keys.getFirst().stream().filter(i -> i.startsWith("slot")).toList();
        for (String slot : slots) {
            String[] dedupedSlots = slot.substring("slot".length()).split("-");
            for (String dedupedSlot : dedupedSlots) {

                int slotNo = Integer.parseInt(dedupedSlot);
                // im using Path v1 to return the object without it in an array []
                // https://redis.io/docs/latest/develop/data-types/json/path/#access-examples
                Object itemData = Main.JEDIS.jsonGet(kitKey, Path.of(slot));

                ItemStack item = ItemStack.CODEC.decode(Transcoder.JSON, gson.toJsonTree(itemData).getAsJsonObject()).orElseThrow();

                if (item.material() == Material.DIAMOND_SWORD) {
                    item = item.with(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(0f, 0f, List.of(), BlocksAttacks.ItemDamageFunction.DEFAULT, null, null, null));
                }
                inv.setItemStack(slotNo, item);
            }
        }

    }

    public Kit2Command() {
        super("k2");
        ArgumentWord create = ArgumentType.Word("create").from("set", "create", "new");
        ArgumentWord kitName = ArgumentType.Word("kitName");

        addSyntax((sender, context) -> {
            createKit(sender, context.get(kitName));
        }, create, kitName);

        addSyntax((sender, context) -> {
            loadKit(sender, context.get(kitName));
        }, kitName);

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("default executor");
        });
    }
}
