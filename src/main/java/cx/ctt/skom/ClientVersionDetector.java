package cx.ctt.skom;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.tag.Tag;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <a href="https://github.com/Term4/minestom-mechanics-lib/blob/main/minestom-mechanics-lib/src/main/java/com/minestom/mechanics/systems/compatibility/ClientVersionDetector.java">...</a>>
 * @see <a href="https://github.com/ViaVersion/ViaVersion/wiki/Server-and-Player-Details-Protocol">ViaVersion Server and Player Details Protocol</a>
 */
public class ClientVersionDetector {

    // from com.viaversion.viaversion.connection.ConnectionDetails
    public static final String PROXY_CHANNEL = "vv:proxy_details"; // Used for multi server proxies like Velocity
    public static final String SERVER_CHANNEL = "vv:server_details"; // Used for backend servers like Paper
    public static final String MOD_CHANNEL = "vv:mod_details"; // Used for clientside mods like ViaFabric
    public static final String APP_CHANNEL = "vv:app_details"; // Used for standalone applications
    public static final Tag<Integer> tag = Tag.Integer("protocolVer");

    private static final Gson GSON = new Gson();

    /** Protocol version below this is treated as legacy (1.8.x = 47; 1.9 = 107). */
    private static final int LEGACY_PROTOCOL_THRESHOLD = 107;

    private static ClientVersionDetector instance;
    private static final Logger log = Main.LOG;

    private ClientVersionDetector() {}

    public static ClientVersionDetector getInstance() {
        if (instance == null) {
            instance = new ClientVersionDetector();
            instance.initialize();
        }
        return instance;
    }

    private void initialize() {
        var handler = MinecraftServer.getGlobalEventHandler();

        handler.addListener(PlayerPluginMessageEvent.class, event -> {
            String channel = event.getIdentifier();
            if (channel.equals(PROXY_CHANNEL) || channel.equals(SERVER_CHANNEL) ||  channel.equals(MOD_CHANNEL) || channel.equals(APP_CHANNEL)) {
                handleViaVersionMessage(event.getPlayer(), event.getMessage(), channel);
            } else if (channel.equals("minecraft:brand")) {
                handleViaVersionMessage(event.getPlayer(), event.getMessage(), channel);
            }
        });
    }

    /**
     * Handle protocol version from ViaVersion (vv:mod_details or vv:proxy_details).
     * Payload: JSON with {@code version} (int) and {@code versionName} (string).
     */
    private void handleViaVersionMessage(Player player, byte[] data, String pluginMessageName) {
        if (data == null || data.length == 0) {
            log.debug("Invalid ViaVersion message from {}: empty payload", player.getUsername());
            return;
        }
        try {
            String json = new String(data, StandardCharsets.UTF_8);

            Main.LOG.info(pluginMessageName);
            Main.LOG.info(json);

            if (pluginMessageName.equals("minecraft:brand")) { return; }
            JsonObject payload = GSON.fromJson(json, JsonObject.class);
            if (payload == null || !payload.has("version")) {
                log.debug("Invalid ViaVersion message from {}: missing version", player.getUsername());
                return;
            }
            int protocolVersion = payload.get("version").getAsInt();
            String versionName = payload.has("versionName") ? sanitizeVersionName(payload.get("versionName").getAsString()) : null;

            player.setTag(tag, protocolVersion);
            ClientVersion version = protocolVersion < LEGACY_PROTOCOL_THRESHOLD ? ClientVersion.LEGACY : ClientVersion.MODERN;
            String label = versionName != null ? versionName : ("protocol " + protocolVersion);
            log.debug("{} | client version: {} (protocol {}, {})", player.getUsername(), label, protocolVersion, version);
        } catch (Exception e) {
            log.warn("Failed to parse ViaVersion message from {}: {}", player.getUsername(), e.getMessage());
        }
    }

    /** Trim and strip trailing non-alphanumeric/dot characters from version name for display (e.g. "1.8.x]" -> "1.8.x"). */
    private static String sanitizeVersionName(String versionName) {
        if (versionName == null) return null;
        String s = versionName.trim();
        int end = s.length();
        while (end > 0 && !Character.isLetterOrDigit(s.charAt(end - 1)) && s.charAt(end - 1) != '.') {
            end--;
        }
        return end == s.length() ? s : s.substring(0, end);
    }

    /**
     * Get client version for a player. Only protocol version from ViaVersion is used.
     * Returns {@link ClientVersion#UNKNOWN} if no protocol version has been received.
     */
    public ClientVersion getClientVersion(Player player) {
        Integer protocol = player.getTag(tag);
        if (protocol == null) {
            return ClientVersion.UNKNOWN;
        }
        return protocol < LEGACY_PROTOCOL_THRESHOLD ? ClientVersion.LEGACY : ClientVersion.MODERN;
    }

    public boolean isLegacy(Player player) {
        return getClientVersion(player) == ClientVersion.LEGACY;
    }

    /**
     * Raw protocol version when provided by ViaVersion.
     * e.g. 47 = 1.8.x, 107 = 1.9. Returns null if ViaVersion has not sent details yet.
     */
    public Integer getProtocolVersion(Player player) {
        return player.getTag(tag);
    }

    /**
     * Whether the player needs frequent animation updates (legacy clients only).
     * False when version is unknown (no protocol from ViaVersion).
     */
    public boolean needsFrequentUpdates(Player player) {
        return getClientVersion(player) == ClientVersion.LEGACY;
    }

    public enum ClientVersion {
        LEGACY,   // protocol < 107 (e.g. 1.7.10, 1.8.x)
        MODERN,   // protocol >= 107 (e.g. 1.21.x)
        UNKNOWN   // No protocol version received from ViaVersion
    }
}