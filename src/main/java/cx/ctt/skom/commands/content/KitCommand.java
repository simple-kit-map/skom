package cx.ctt.skom.commands.content;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import cx.ctt.skom.Creatable;
import cx.ctt.skom.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlocksAttacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitCommand extends Command implements Creatable {

    public static final String featureName = "kit";

    @Override
    public String featureName() {
        return featureName;
    }

    public KitCommand() {
        super("kit", "k");
        setDefaultExecutor((sender, context) -> MinecraftServer.getCommandManager().execute(sender, "kit list"));
        var kitName = ArgumentType.String("kitName");
        addSyntax((sender, context) -> MinecraftServer.getCommandManager().execute(sender, "kit load " + context.get(kitName)), kitName);
        addSubcommand(new KitCreate());
        addSubcommand(new KitList());
        addSubcommand(new KitLoad());
    }

    public static class KitLoad extends Command {
        public KitLoad() {

            super("load");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("/kit load <name>");
                sender.sendMessage("/kit <name>");
            });
            var kitNameArg = ArgumentType.String("kitName");
            addSyntax((sender, context) -> {
                var kitName = context.get(kitNameArg);
                loadKit(sender, kitName.toLowerCase());
            }, kitNameArg);
        }

        static public void loadKit(CommandSender sender, String kitName) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("hi mr console, use: /sudo <target> /kit" + kitName);
                return;
            }
            if (Creatable.isNotAlphaNumeric(kitName)) {
                sender.sendMessage("kit names must be alphanumeric");
                return;
            }
            if (!Creatable.exists(featureName, kitName)) {
                sender.sendMessage("kit " + kitName + " does not exist");
                return;
            }

            String kitKey = "skm:kit:" + kitName;
            player.getInventory().clear();
            jsonToInventory(player.getInventory(), kitKey);
            player.refreshCommands();
            player.getInventory().update();
        }

        static void jsonToInventory(PlayerInventory inv, String kitKey) {
            Map<String, String> keys = Main.JEDIS.hgetAll(kitKey);
            Gson gson = new Gson();
            List<String> slots = keys.keySet().stream().filter(i -> i.startsWith("slot")).toList();
            for (String slot : slots) {
                String[] dedupedSlots = slot.substring("slot".length()).split("-");
                for (String dedupedSlot : dedupedSlots) {

                    int slotNo = Integer.parseInt(dedupedSlot);
                    // im using Path v1 to return the object without it in an array []
                    // https://redis.io/docs/latest/develop/data-types/json/path/#access-examples
                    String itemData = Main.JEDIS.hget(kitKey, slot);

                    ItemStack item = ItemStack.CODEC.decode(Transcoder.JSON, JsonParser.parseString(itemData)).orElseThrow();

                    if (item.material() == Material.DIAMOND_SWORD) {
                        item = item.with(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(0f, 0f, List.of(), BlocksAttacks.ItemDamageFunction.DEFAULT, null, null, null));
                    }
                    inv.setItemStack(slotNo, item);
                }
            }
        }

    }

    private static class KitList extends Command {
        public KitList() {
            super("list");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("");
                var kits = Main.JEDIS.keys("skm:kit:*");
                sender.sendMessage("list of kits (" + kits.size() + "):");
                for (String key : kits) {
                    sender.sendMessage(key.replace("skm:kit:", ""));
                }
            });
        }
    }

    private static class KitCreate extends Command {
        public KitCreate() {
            super("create");
            setDefaultExecutor((sender, context) -> {
                sender.sendMessage("/kit create <name>");
            });
            var kitNameArg = ArgumentType.String("kitName");
            addSyntax((sender, context) -> {
                CreateKit((Player) sender, context.get(kitNameArg).toLowerCase());
            }, kitNameArg);

        }
        private void CreateKit(Player player, String kitName) {
            if (Creatable.isNotAlphaNumeric(kitName)) {
                player.sendMessage("kit names can only contain numbers or letters");
                return;
            }
            if (Creatable.exists(featureName, kitName)) {
                player.sendMessage(kitName + " already exists, as a kit/warp/kb");
                return;
            }
            String kitKey = "skm:kit:" + kitName;
            inventoryToJson(player, kitKey);
            Main.JEDIS.hset(kitKey, "created", Long.toString(System.currentTimeMillis()));
            Main.JEDIS.hset(kitKey, "kitauthor", '"' + player.getUuid().toString() + '"');
            Main.JEDIS.hset(kitKey, "uses", "0");
            Main.JEDIS.hincrBy(kitKey, "uses", 1);
        }

        private void inventoryToJson(Player player, String kitName) {
            ItemStack[] items = player.getInventory().getItemStacks();
            if (items.length != 46) throw new IllegalStateException("Invalid inventory length");

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

                Main.JEDIS.hset(kitName, ("slot" + slots.stream().map(Object::toString).collect(Collectors.joining("-"))), element.orElseThrow().getAsJsonObject().toString());
            });
        }
    }
}
