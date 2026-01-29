package cx.ctt.skom.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import cx.ctt.skom.Main;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.SimpleCommand;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlocksAttacks;
import net.minestom.server.item.component.EnchantmentList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.json.Path2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class KitCommand extends SimpleCommand {
    JedisPooled jedis = Main.JEDIS;

    public KitCommand() {
        super("kit", "k");
    }

    public static boolean isAlphaNumeric(@NotNull String input) {
        for (Character c : input.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean process(@NotNull CommandSender sender, @NotNull String command, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                for (String key : jedis.keys("*")) {
                    sender.sendMessage(key);
                }
            } else {
                String kitName = args[0];
                if (!isAlphaNumeric(kitName)) {
                    sender.sendMessage("kit names can only contain numbers or letters");
                }
                if (jedis.keys(kitName).isEmpty()){
                    sender.sendMessage("kit " + kitName + " found");
                    return false;
                }
                player.getInventory().clear();
                Gson gson = new Gson();

                List<List<String>> keys = jedis.jsonObjKeys(kitName, Path2.of("$"));
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
                        Object itemData = jedis.jsonGet(kitName, Path.of(slot));

                        ItemStack item = ItemStack.CODEC.decode(Transcoder.JSON,
                                gson.toJsonTree(itemData).getAsJsonObject()
                        ).orElseThrow();

                        if (item.material() == Material.DIAMOND_SWORD)
                        {
                            item = item.with(DataComponents.BLOCKS_ATTACKS,
                            new BlocksAttacks(0f, 0f, List.of(), BlocksAttacks.ItemDamageFunction.DEFAULT, null, null, null)
                            );
                        }
                        player.getInventory().setItemStack(slotNo, item);
                    }
                }
                player.getInventory().update();
            }
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            String kitName = args[1];
            if (!isAlphaNumeric(kitName)) {
                sender.sendMessage("kit names can only contain numbers or letters");
            }
            if (!jedis.keys(kitName).isEmpty()) {
                sender.sendMessage(kitName + " already exists, as a kit/warp/kb");
                return false;
            }
            ItemStack[] items = player.getInventory().getItemStacks();
            if (items.length != 46)
                throw new IllegalStateException("Invalid inventory length");
            jedis.jsonSet(kitName, Path2.ROOT_PATH, "{}");

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
//                (((DataComponentMapImpl) ((ItemStackImpl) item).components()).components()).get(13)


//                try {
                jedis.jsonSet(
                        kitName,
                        Path2.of(
                                "slot" + slots.stream().map(Object::toString).collect(Collectors.joining("-"))
                        ),
                        element.orElseThrow().getAsJsonObject()
                );
//                } catch (Exception e) {
//                    System.out.println("kit creation failed: \n" + e.getMessage());
//                    jedis.jsonDel(kitName);
//                }
            });

//            for (int i = 0; i < items.length; i++) {
//                ItemStack item = items[i];
//                if (item.isAir()) {i++; continue;}
//                Result<JsonElement> element = ItemStack.CODEC.encode(Transcoder.JSON, item);
//
//                try {
//                    jedis.jsonSet(
//                            kitName,
//                            Path2.of(Integer.toString(i)),
//                            element.orElseThrow().getAsJsonObject()
//                    );
//                } catch (Exception e) {
//                    System.out.println("kit creation failed: \n" + e.getMessage());
//                    jedis.jsonDel(kitName);
//                }
//            }
            jedis.jsonSet(kitName, Path2.of("$.created"), Long.toString(System.currentTimeMillis()));
            jedis.jsonSet(kitName, Path2.of("$.kitauthor"), '"' + player.getUuid().toString() + '"');
            jedis.jsonSet(kitName, Path2.of("$.uses"), 0);
            jedis.jsonNumIncrBy(kitName, Path2.of("$.uses"), 1);
            return true;
        }
        sender.sendMessage("unknown arguments");
        return false;
    }

    @Override
    public boolean hasAccess(@NotNull CommandSender sender, @Nullable String commandString) {
        return true;
    }
}
