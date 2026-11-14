package org.polyfrost.forgecompat;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entities;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.CreativeModeTab;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.resource.Identifier;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public final class Hooks {
    static final Set<String> FORGE_METHODS = Set.of(
            "canRiderInteract/0", "getDisplayNameString/0", "shouldSpreadItems/0", "getRegistryName/0",
            "getDigSpeed/2", "getMetadata/1", "getToolClasses/1", "isSideSolid/3"
    );

    private static int modifiers;
    private static final ByteBuffer mouseButtons = ByteBuffer.allocate(16);
    private static Object lookAndFeel;

    public static Class<?> forName(String name) throws ClassNotFoundException {
        return Class.forName(runtimeClassName(name), true, Hooks.class.getClassLoader());
    }

    public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        return Class.forName(runtimeClassName(name), initialize, loader);
    }

    private static String runtimeClassName(String name) {
        if (name == null || !name.startsWith("net.minecraft.")) return name;
        String mapped = ForgeCompat.mappings().mcpToRuntime(name.replace('.', '/'));
        return mapped == null ? name : mapped.replace('/', '.');
    }

    public static Field getDeclaredField(Class<?> owner, String name) throws NoSuchFieldException {
        try {
            return owner.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            String runtime = name.startsWith("field_") ? ForgeCompat.mappings().srgToRuntime(name) : null;
            if (runtime != null) return owner.getDeclaredField(runtime);
            if (owner == Field.class && name.equals("modifiers")) return Hooks.class.getDeclaredField("modifiers");
            if (owner == Mouse.class && name.equals("buttons")) return Hooks.class.getDeclaredField("mouseButtons");
            throw e;
        }
    }

    public static CodeSource getCodeSource(ProtectionDomain domain) {
        CodeSource source = domain.getCodeSource();
        if (source == null || source.getLocation() == null) return source;
        try {
            Path original = ForgeCompat.ORIGINAL_JARS.get(Path.of(source.getLocation().toURI()).toAbsolutePath().normalize());
            return original == null ? source : new CodeSource(original.toUri().toURL(), source.getCodeSigners());
        } catch (Exception e) {
            return source;
        }
    }

    public static void setLookAndFeel(LookAndFeel laf) throws UnsupportedLookAndFeelException {
        if (Display.isCreated()) UIManager.setLookAndFeel(laf);
        else lookAndFeel = laf;
    }

    public static void setLookAndFeel(String className) throws ReflectiveOperationException, UnsupportedLookAndFeelException {
        if (Display.isCreated()) UIManager.setLookAndFeel(className);
        else lookAndFeel = className;
    }

    public static void applyLookAndFeel() {
        Object laf = lookAndFeel;
        if (laf == null) return;
        lookAndFeel = null;
        try {
            if (laf instanceof LookAndFeel instance) UIManager.setLookAndFeel(instance);
            else UIManager.setLookAndFeel((String) laf);
        } catch (Exception e) {
            ForgeCompat.LOGGER.error("Failed to set look and feel {}", laf, e);
        }
    }

    public static int nextCreativeTabId() {
        CreativeModeTab[] tabs = CreativeModeTab.ALL;
        try {
            CreativeModeTab.class.getField(ForgeCompat.mappings().srgToRuntime("field_78032_a")).set(null, Arrays.copyOf(tabs, tabs.length + 1));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to grow the creative tab array", e);
        }
        return tabs.length;
    }

    public static boolean canRiderInteract(Object entity) {
        return false;
    }

    public static String getDisplayNameString(Object player) {
        return ((PlayerEntity) player).getName();
    }

    public static boolean shouldSpreadItems(Object renderer) {
        return true;
    }

    public static String getRegistryName(Object entry) {
        Identifier id = entry instanceof Block block ? Block.REGISTRY.getKey(block) : Item.REGISTRY.getKey((Item) entry);
        return id == null ? null : id.toString();
    }

    public static float getDigSpeed(Object item, ItemStack stack, BlockState state) {
        return ((Item) item).getMiningSpeed(stack, state.getBlock());
    }

    public static int getMetadata(Object item, ItemStack stack) {
        return stack.getMetadata();
    }

    public static Set<String> getToolClasses(Object item, ItemStack stack) {
        if (item instanceof PickaxeItem) return Set.of("pickaxe");
        if (item instanceof AxeItem) return Set.of("axe");
        if (item instanceof ShovelItem) return Set.of("shovel");
        return Collections.emptySet();
    }

    public static String getEntityName(ItemStack stack) {
        return Entities.getKey(stack.getMetadata());
    }

    private static Field buttonHeight;

    public static int buttonHeight(net.minecraft.client.gui.widget.ButtonWidget button) {
        try {
            if (buttonHeight == null) {
                buttonHeight = net.minecraft.client.gui.widget.ButtonWidget.class.getDeclaredField(ForgeCompat.mappings().srgToRuntime("field_146121_g"));
                buttonHeight.setAccessible(true);
            }
            return buttonHeight.getInt(button);
        } catch (ReflectiveOperationException e) {
            return 20;
        }
    }

    public static int nextPotionId() {
        net.minecraft.entity.living.effect.StatusEffect[] effects = net.minecraft.entity.living.effect.StatusEffect.BY_ID;
        for (int i = 1; i < effects.length; i++) if (effects[i] == null) return i;
        throw new IllegalStateException("No free potion ids left");
    }

    public static net.minecraftforge.fml.common.registry.RegistryDelegate<?> delegate(Object entry) {
        net.minecraft.resource.Identifier key = entry instanceof net.minecraft.block.Block block ? net.minecraft.block.Block.REGISTRY.getKey(block) : net.minecraft.item.Item.REGISTRY.getKey((net.minecraft.item.Item) entry);
        return new net.minecraftforge.fml.common.registry.RegistryDelegate<Object>() {
            public Object get() { return entry; }
            public String name() { return String.valueOf(key); }
            public net.minecraft.resource.Identifier getResourceName() { return key; }
            @SuppressWarnings("unchecked")
            public Class<Object> type() { return (Class<Object>) (entry instanceof net.minecraft.block.Block ? net.minecraft.block.Block.class : net.minecraft.item.Item.class); }
        };
    }

    public static float eyeHeight(Object entity) {
        return ((net.minecraft.entity.Entity) entity).getEyeHeight();
    }

    public static boolean isSideSolid(Object world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.math.Direction side, boolean defaultValue) {
        net.minecraft.block.state.BlockState state = ((net.minecraft.world.WorldView) world).getBlockState(pos);
        return state == null ? defaultValue : state.getBlock().isCube();
    }

    private static volatile net.minecraft.client.world.ClientWorld realWorld;

    public static boolean isSceneWorld(net.minecraft.world.World world) {
        return world != null && world.isClient && world != realWorld;
    }

    public static net.minecraft.client.world.ClientWorld realWorld() {
        return realWorld;
    }

    public static void setRealWorld(net.minecraft.client.world.ClientWorld world) {
        realWorld = world;
    }
}
