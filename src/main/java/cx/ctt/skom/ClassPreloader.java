package cx.ctt.skom;

import net.minestom.server.component.DataComponents;

import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.world.biome.BiomeEffects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * from fr.epicube.minestom.server.util
 * Parallel class preloader to accelerate JVM startup.
 * <p>
 * This utility pre-loads classes and initializes static fields in parallel
 * threads before they're needed, reducing latency on first use.
 * <p>
 */
public final class ClassPreloader {

    public static final ExecutorService EXECUTOR_SERVICE =
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
                    .factory());

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPreloader.class);

    // ═══════════════════════════════════════════════════════════════════════════
    // Classes to preload (by name, for reflection-based loading)
    // ═══════════════════════════════════════════════════════════════════════════

    private static final List<String> PRELOAD_CLASSES = List.of(
            // Minestom core classes with heavy static initialization
            "net.minestom.server.instance.block.BlockImpl",
            "net.minestom.server.item.ItemStack",
            "net.minestom.server.entity.EntityType",
            "net.minestom.server.instance.batch.ChunkBatch",
            "net.minestom.server.command.builder.Command"
    );

    // ═══════════════════════════════════════════════════════════════════════════
    // Static field accessors to trigger initialization
    // ═══════════════════════════════════════════════════════════════════════════

    private static final List<Runnable> PRELOAD_TASKS = List.of(

            // Minestom components
            () -> touch(DataComponents.ITEM_NAME, BiomeEffects.DEFAULT),

            // Packet parsing
            () -> touch(PacketVanilla.CLIENT_PACKET_PARSER),

            // Adventure translation
            GlobalTranslator::translator
    );

    private ClassPreloader() {
        // Static utility class
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Public API
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Starts parallel class preloading in the background.
     * <p>
     * This method returns immediately. Use the returned future to wait
     * for completion if needed.
     *
     * @return a future that completes when all preloading is done
     */
    public static CompletableFuture<Void> preloadAsync() {
        var startTime = Instant.now();
        var successCount = new AtomicInteger(0);
        var failureCount = new AtomicInteger(0);

        LOGGER.debug("Starting parallel class preloading...");

        // Preload classes by name
        var classFutures = PRELOAD_CLASSES.stream()
                .map(className -> CompletableFuture.runAsync(() -> {
                    if (loadClass(className)) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                }, EXECUTOR_SERVICE))
                .toArray(CompletableFuture[]::new);

        // Run preload tasks
        var taskFutures = PRELOAD_TASKS.stream()
                .map(task -> CompletableFuture.runAsync(() -> {
                    runTask(task);
                    successCount.incrementAndGet();
                }, EXECUTOR_SERVICE))
                .toArray(CompletableFuture[]::new);

        // Combine all futures
        return CompletableFuture.allOf(
                CompletableFuture.allOf(classFutures),
                CompletableFuture.allOf(taskFutures)
        ).whenComplete((_, error) -> {
            var elapsed = Duration.between(startTime, Instant.now());
            if (error != null) {
                LOGGER.warn("Class preloading completed with errors in {}ms", elapsed.toMillis(), error);
            } else if (failureCount.get() > 0) {
                LOGGER.debug("Class preloading completed in {}ms ({} success, {} failed)",
                        elapsed.toMillis(), successCount.get(), failureCount.get());
            } else {
                LOGGER.debug("Class preloading completed in {}ms ({} items)",
                        elapsed.toMillis(), successCount.get());
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Internal Helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private static boolean loadClass(String className) {
        try {
            var start = System.nanoTime();
            Class.forName(className);
            LOGGER.trace("Preloaded class {} in {}µs", className, (System.nanoTime() - start) / 1000);
            return true;
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Failed to preload class: {} (not found)", className);
            return false;
        } catch (Exception e) {
            LOGGER.warn("Failed to preload class: {} ({})", className, e.getMessage());
            return false;
        }
    }

    private static void runTask(Runnable task) {
        try {
            var start = System.nanoTime();
            task.run();
            LOGGER.trace("Preload task completed in {}µs", (System.nanoTime() - start) / 1000);
        } catch (Exception e) {
            LOGGER.warn("Preload task failed: {}", e.getMessage());
        }
    }

    /**
     * No-op method that "uses" objects to prevent dead code elimination.
     * The JIT compiler won't optimize away the class loading if we pass
     * the objects to a method.
     */
    @SuppressWarnings("unused")
    private static void touch(Object... objects) {
        // Intentionally empty - just forces class loading
    }
}