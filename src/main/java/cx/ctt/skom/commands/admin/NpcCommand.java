package cx.ctt.skom.commands.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NpcCommand extends Command {

    enum NPC_PRESETS {
        leather,
        iron,
        chainmail,
        diamond,
        netherite,
        copper,
        gold,
        naked
    }

    private static final Map<String, Material[]> TIER_MATERIALS = Map.of(
            "leather", new Material[]{Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.BOW},
            "iron", new Material[]{Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.IRON_SWORD},
            "chainmail", new Material[]{Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS, Material.IRON_SWORD},
            "diamond", new Material[]{Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.DIAMOND_SWORD},
            "netherite", new Material[]{Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS, Material.NETHERITE_SWORD},
            "copper", new Material[]{Material.COPPER_HELMET, Material.COPPER_CHESTPLATE, Material.COPPER_LEGGINGS, Material.COPPER_BOOTS, Material.COPPER_SWORD},
            "gold", new Material[]{Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS, Material.GOLDEN_SWORD}
    );

    public NpcCommand() {
        super("npc");
        setDefaultExecutor((sender, ignored) -> {
            if (!(sender instanceof Player p)) return;
            Npc npc = createNpc(p, "diamond", p.getUsername(), NamedTextColor.DARK_AQUA, false);
            npc.setInstance(p.getInstance(), p.getPosition());
        });
        var tierArg = ArgumentType.Enum("tier", NPC_PRESETS.class);
        var usernameArg = ArgumentType.String("username");
        var colorArg = ArgumentType.Color("color");
        var lookarg = ArgumentType.Boolean("lookat");

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player p)) return;
            String tier = context.get(tierArg).toString();
            String username = context.get(usernameArg);
            var color = (NamedTextColor) context.get(colorArg).color();
            boolean lookAt = context.get(lookarg);
            Npc npc = createNpc(p, tier, username, color, lookAt);
            npc.setInstance(p.getInstance(), p.getPosition());

        }, tierArg, usernameArg, colorArg, lookarg);
    }

    private Npc createNpc(Player p, String tier, String username, NamedTextColor color, boolean lookAt) {
        Npc npc = new Npc(
                username,
                PlayerSkin.fromUsername(username),
                (_, npc1) -> npc1.sendPacketToViewers(new EntityAnimationPacket(npc1.getEntityId(), EntityAnimationPacket.Animation.MAGICAL_CRITICAL_EFFECT)),
                color,
                lookAt
        );
        Material[] mats = TIER_MATERIALS.get(tier.toLowerCase());
        if (mats != null) {
            EnchantmentList unbr = new EnchantmentList(Map.of(Enchantment.UNBREAKING, 1));
            npc.setHelmet(ItemStack.of(mats[0]).with(DataComponents.ENCHANTMENTS, unbr));
            npc.setChestplate(ItemStack.of(mats[1]).with(DataComponents.ENCHANTMENTS, unbr));
            npc.setLeggings(ItemStack.of(mats[2]).with(DataComponents.ENCHANTMENTS, unbr));
            npc.setBoots(ItemStack.of(mats[3]).with(DataComponents.ENCHANTMENTS, unbr));
            npc.setItemInMainHand(ItemStack.of(mats[4]).with(DataComponents.ENCHANTMENTS, unbr));
        }
        return npc;
    }

    public final class Npc extends EntityCreature {
        private static final double LOOK_DISTANCE = 20.0D;
        private static Team npcTeam;

        private final String username;
        private final @Nullable PlayerSkin skin;
        private final Entity nameTag;
        private final BiConsumer<Player, Npc> action;
        private final boolean lookAt;

        public Npc(String name, @Nullable PlayerSkin skin, BiConsumer<Player, Npc> action, NamedTextColor color, boolean lookAt) {
            super(EntityType.PLAYER, UUID.randomUUID());
//        this.username = UUID.randomUUID().toString().substring(0, 6);
            this.username = name;
            this.skin = skin;
            this.action = action;
            this.lookAt = lookAt;

            this.setNoGravity(true);
            this.hasPhysics = false;
            this.setSynchronizationTicks(Integer.MAX_VALUE);

            this.nameTag = new Entity(EntityType.TEXT_DISPLAY);
            this.nameTag.editEntityMeta(TextDisplayMeta.class, this.editNameTagMeta(Component.text(name).color(color)));


            if (npcTeam == null) {
                npcTeam = MinecraftServer.getTeamManager().createBuilder("npcs")
                        .nameTagVisibility(TeamsPacket.NameTagVisibility.NEVER)
                        .teamColor(color)
                        .build();
            }

            // setTeam does not work, as it will use uuid instead of username
//        this.setTeam(npcTeam);
            npcTeam.addMember(this.username);

        }

        @Override
        public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
            var future = super.setInstance(instance, spawnPosition);
            if (this.lookAt) {
                instance.scheduler().submitTask(this::lookTask);
            }
            instance.eventNode().addListener(EntityAttackEvent.class, this::handleAttack);
            instance.eventNode().addListener(PlayerEntityInteractEvent.class, this::handleInteraction);
            return nameTag.setInstance(instance, spawnPosition).thenComposeAsync(o -> future)
                    .whenComplete((_, _) -> this.addPassenger(this.nameTag));
        }

        @Override
        public void updateNewViewer(@NotNull Player player) {
            var properties = new ArrayList<PlayerInfoUpdatePacket.Property>();

            if (this.skin != null && this.skin.textures() != null && this.skin.signature() != null) {
                properties.add(new PlayerInfoUpdatePacket.Property("textures", this.skin.textures(), this.skin.signature()));
            }

            var entry = new PlayerInfoUpdatePacket.Entry(
                    this.getUuid(), this.username, properties,
                    false, 0, GameMode.SURVIVAL,
                    null, null, 0, true);

            player.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry));
            super.updateNewViewer(player);
            this.nameTag.addViewer(player);

            player.sendPacket(new EntityMetaDataPacket(
                    this.getEntityId(),
                    Map.of(17, Metadata.Float(127))));
        }

        @Override
        public void updateOldViewer(@NotNull Player player) {
            super.updateOldViewer(player);
            player.sendPacket(new PlayerInfoRemovePacket(this.getUuid()));
            this.nameTag.removeViewer(player);
        }

        @Override
        protected void remove(boolean permanent) {
            super.remove(permanent);
            this.nameTag.remove();
        }

        public void setName(Component name) {
            this.nameTag.editEntityMeta(TextDisplayMeta.class, this.editNameTagMeta(name));
        }

        private Consumer<TextDisplayMeta> editNameTagMeta(Component name) {
            return meta -> {
                meta.setTranslation(new Vec(0.0D, 0.3D, 0.0D));
                meta.setBillboardRenderConstraints(AbstractDisplayMeta.BillboardConstraints.VERTICAL);
                meta.setBackgroundColor(0x00000000);
                meta.setShadow(true);
                meta.setText(name);
            };
        }

        private void handleAttack(EntityAttackEvent event) {
            if (event.getTarget() == this && event.getEntity() instanceof Player player) {
                this.action.accept(player, this);
            }
        }

        private void handleInteraction(PlayerEntityInteractEvent event) {
            if (event.getPlayer().isSneaking()) {
                this.remove();
            }
        }

        private TaskSchedule lookTask() {
            for (var player : this.getInstance().getPlayers()) {
                var position = player.getDistance(this) > LOOK_DISTANCE
                        ? this.position : this.position.withLookAt(player.getPosition());

                player.sendPackets(
                        new EntityHeadLookPacket(this.getEntityId(), position.yaw()),
                        new EntityRotationPacket(this.getEntityId(), position.yaw(), position.pitch(), this.onGround));
            }

            return TaskSchedule.nextTick();
        }
    }


}
