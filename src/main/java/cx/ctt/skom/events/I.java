package cx.ctt.skom.events;

import cx.ctt.skom.Main;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.GameMode;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class I extends Command {

    HashMap<String, Set<String>> Alias = new HashMap<>() {{
        put("material", Set.of("give"));
        put("enchant", Set.of("enc"));
        put("attribute", Set.of("att"));
        put("effect", Set.of("eff"));
        put("component", Set.of("com"));

        put("playerstatus", Set.of("ps"));
        put("kit", Set.of("k", "kits"));
        put("mechanic", Set.of("kb", "knockback", "knockbacks"));
        put("warp", Set.of("w", "map", "maps", "warps"));

    }};

    public static @Nullable String isTaken(@NonNull String Iname){
        if (Iname.length() < 3)
            return "Too short";
        for (Character c : Iname.toCharArray()) {
            if (c != '_' && !Character.isLetterOrDigit(c)) {
                return "Illegal character '" + c + "' in " + Iname;
            }
        }
        if (Material.values().toString().contains(Iname))
            return "Minecraft item " + Iname + " already exists";

        if (Arrays.toString(GameMode.values()).contains(Iname))
            return Iname + " is a gamemode";

        if (!Main.JEDIS.keys("*:" + Iname).isEmpty())
            return Iname + " already exists";

        return null;
    }

    public I() {
        super("i", "");

    }
}
