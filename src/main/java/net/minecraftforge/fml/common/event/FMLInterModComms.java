package net.minecraftforge.fml.common.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class FMLInterModComms {
    public static boolean sendMessage(String modId, String key, NbtCompound value) {
        return true;
    }

    public static boolean sendMessage(String modId, String key, ItemStack value) {
        return true;
    }

    public static boolean sendMessage(String modId, String key, String value) {
        return true;
    }

    public static void sendRuntimeMessage(Object sourceMod, String modId, String key, NbtCompound value) {
    }

    public static void sendRuntimeMessage(Object sourceMod, String modId, String key, ItemStack value) {
    }

    public static void sendRuntimeMessage(Object sourceMod, String modId, String key, String value) {
    }

    public static ImmutableList<IMCMessage> fetchRuntimeMessages(Object forMod) {
        return ImmutableList.of();
    }

    public static class IMCEvent extends FMLEvent {
        public ImmutableList<IMCMessage> getMessages() {
            return ImmutableList.of();
        }
    }

    public static final class IMCMessage {
        public final String key;
        private final Object value;
        private final String sender;

        IMCMessage(String key, Object value, String sender) {
            this.key = key;
            this.value = value;
            this.sender = sender;
        }

        public String getSender() {
            return sender;
        }

        public String getStringValue() {
            return (String) value;
        }

        public NbtCompound getNBTValue() {
            return (NbtCompound) value;
        }

        public ItemStack getItemStackValue() {
            return (ItemStack) value;
        }

        public Class<?> getMessageType() {
            return value.getClass();
        }

        public boolean isStringMessage() {
            return value instanceof String;
        }

        public boolean isNBTMessage() {
            return value instanceof NbtCompound;
        }

        public boolean isItemStackMessage() {
            return value instanceof ItemStack;
        }
    }
}
