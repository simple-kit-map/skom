package cx.ctt.skom;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.lan.OpenToLAN;

public class Main {
    public static void main(String[] args) {
        long timestamp = System.currentTimeMillis();
        MinecraftServer minecraftServer = MinecraftServer.init(new Auth.Offline());
        OpenToLAN.open();
        minecraftServer.start(t "0.0.0.0", 8888);
        System.out.println("started in " + (System.currentTimeMillis() - timestamp) + "ms");
    }
}