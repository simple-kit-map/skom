package cx.ctt.skom.events;

import cx.ctt.skom.Main;
import io.github.togar2.pvp.player.CombatPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityVelocityEvent;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.network.packet.server.play.EntityVelocityPacket;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static cx.ctt.skom.commands.DashCommand.roundDouble;
import static java.lang.Math.*;

public class Knockback {

    public static final Set<Entity> invulnerablePlayers = new HashSet<>();

    public static void scoreKb(LivingEntity damager, LivingEntity damagee) {
        KnockbackPreset a = new KnockbackPreset();


        double distanceX = damager.getPosition().x() - damagee.getPosition().x();
        double distanceZ = damager.getPosition().z() - damagee.getPosition().z();

        while (distanceX * distanceX + distanceZ * distanceZ < 1.0E-4) {
            distanceX = (Math.random() - Math.random()) * 0.01;
            distanceZ = (Math.random() - Math.random()) * 0.01;
        }

        var magnitude = sqrt(distanceX * distanceX + distanceZ * distanceZ);
        /*

        double lookWeight = 0.5;
        if (lookWeight > 0 && attacker instanceof Player) {
            double yaw = Math.toRadians(attacker.getPosition().yaw());
            double lookX = -Math.sin(yaw);
            double lookZ = Math.cos(yaw);

            dx = dx * (1 - lookWeight) + lookX * lookWeight;
            dz = dz * (1 - lookWeight) + lookZ * lookWeight;

            double finalDistance = Math.sqrt(dx * dx + dz * dz);
            if (finalDistance > MIN_KNOCKBACK_DISTANCE) {
                dx /= finalDistance;
                dz /= finalDistance;
            }
        }
        */

//        var playerVelocityBad = damagee.getVelocity(); // always return 0, -1.568, 0
//        assert playerVelocityBad.y() != -1.568;

        Pos prev = damagee.getPreviousPosition();
        Pos cur = damagee.getPosition();
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {

            printVec(player, Vec.fromPoint(damagee.getPreviousPosition()), "PREV");
            printVec(player, Vec.fromPoint(damagee.getPosition()), "CUR");
        });
//        cur = prev;
        Vec originalVelocity = new Vec(cur.x() - prev.x(), cur.y() - prev.y(), cur.z() - prev.z());

        Vec finalOriginalVelocity = originalVelocity;
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {
            printVec(player, Vec.fromPoint(finalOriginalVelocity), "DIFF");
        });
//        if (!originalVelocity.equals(Vec.ZERO))
//            originalVelocity = originalVelocity.mul(ServerFlag.SERVER_TICKS_PER_SECOND);

        Vec playerVelocity = originalVelocity;
        if (Double.isNaN(playerVelocity.x()) || Double.isNaN(playerVelocity.y()) || Double.isNaN(playerVelocity.z())) {
            if (damagee instanceof Player p) {
                printVec(p, originalVelocity, "<- Original, calculated was NaN");
            }
            if (damager instanceof Player p) {
                printVec(p, originalVelocity, "<- Original, calculated was NaN");
            }
            return;
        }
        playerVelocity = playerVelocity.withX((playerVelocity.x() / a.horizontalFriction) - (distanceX / magnitude * a.horizontal))
                .withY((playerVelocity.y() / a.verticalFriction) + a.vertical)
                .withZ((playerVelocity.z() / a.horizontalFriction) - (distanceZ / magnitude * a.horizontal));

        double bonusKnockback = 0F;

        if (!damager.getItemInMainHand().isAir()) {
            EnchantmentList enchants = damager.getItemInMainHand().get(DataComponents.ENCHANTMENTS);
            if (enchants != null) {
                if (enchants.has(Enchantment.KNOCKBACK)) {
                    bonusKnockback += enchants.level(Enchantment.KNOCKBACK);
                }
            }
        }

        if (damager instanceof CombatPlayer p) {
            bonusKnockback += p.isSprinting() ? 1 : 0;
//            ((Player)p).setSprinting(false); /// TODO: add proper wtap mechanicsijdiaowdiajnm
        }

        if (playerVelocity.y() > a.verticalLimit) playerVelocity = playerVelocity.withY(a.verticalLimit);

        if (bonusKnockback > 0) {
            playerVelocity.add(new Vec(-sin((damager.getPosition().yaw() * Math.PI / 180.0f)) * bonusKnockback * a.horizontalExtra, a.verticalExtra, cos((damager.getPosition().yaw() * Math.PI / 180.0f)) * bonusKnockback * a.horizontalExtra));
        }
//        if (damagee instanceof Player p) printVec(p, playerVelocity, "SKMVELO");
//        if (damager instanceof Player p) printVec(p, playerVelocity, "SKMVELO");
        damagee.damage(DamageType.PLAYER_ATTACK, 0);
        damagee.setVelocity(playerVelocity);
        if (damagee instanceof CombatPlayer player) {
            player.sendImmediateVelocityUpdate();
            ((Player) player).sendPacket(new EntityVelocityPacket(((Player) player).getEntityId(), playerVelocity));
        }
        invulnerablePlayers.add(damagee);
        damagee.scheduler().scheduleTask(() -> invulnerablePlayers.remove(damagee), TaskSchedule.tick(9), TaskSchedule.stop());
    }

    public static void register(GlobalEventHandler handler) {
        handler.addListener(EntityVelocityEvent.class, event -> {
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(p -> {
                printVec(p, event.getVelocity(), "VELO-EVENT");
            });
        });

        handler.addListener(EntityAttackEvent.class, event -> {
            if (!(event.getEntity() instanceof LivingEntity damager)) return;
            if (!(event.getTarget() instanceof LivingEntity damagee)) return;
            if (invulnerablePlayers.contains(damagee)) return;
            if (damagee.isInvulnerable()) return;

            scoreKb(damager, damagee);
        });

    }
    public static void printVec(CommandSender sender, Vec vec, @Nullable String haha) {
        sender.sendMessage(roundDouble(vec.x()) + ", " + roundDouble(vec.y()) + ", " + roundDouble(vec.z()) + " " + haha);
        Main.LOG.debug("SKM{}", vec);
    }
}
