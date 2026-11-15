package net.minecraftforge.fml.common.network;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.apache.commons.lang3.Validate;

public class ByteBufUtils {
    public static int varIntByteCount(int toCount) {
        return (toCount & -128) == 0 ? 1 : (toCount & -16384) == 0 ? 2 : (toCount & -2097152) == 0 ? 3 : (toCount & -268435456) == 0 ? 4 : 5;
    }

    public static int readVarInt(ByteBuf buf, int maxSize) {
        Validate.isTrue(maxSize < 6 && maxSize > 0, "Varint length is between 1 and 5, not %d", maxSize);
        int i = 0, j = 0;
        byte b;
        do {
            b = buf.readByte();
            i |= (b & 127) << j++ * 7;
            if (j > maxSize) throw new RuntimeException("VarInt too big");
        } while ((b & 128) == 128);
        return i;
    }

    public static int readVarShort(ByteBuf buf) {
        int low = buf.readUnsignedShort();
        int high = 0;
        if ((low & 0x8000) != 0) {
            low &= 0x7FFF;
            high = buf.readUnsignedByte();
        }
        return (high & 0xFF) << 15 | low;
    }

    public static void writeVarShort(ByteBuf buf, int toWrite) {
        int low = toWrite & 0x7FFF;
        int high = (toWrite & 0x7F8000) >> 15;
        if (high != 0) low |= 0x8000;
        buf.writeShort(low);
        if (high != 0) buf.writeByte(high);
    }

    public static void writeVarInt(ByteBuf to, int toWrite, int maxSize) {
        Validate.isTrue(varIntByteCount(toWrite) <= maxSize, "Integer is too big for %d bytes", maxSize);
        while ((toWrite & -128) != 0) {
            to.writeByte(toWrite & 127 | 128);
            toWrite >>>= 7;
        }
        to.writeByte(toWrite);
    }

    public static String readUTF8String(ByteBuf from) {
        int len = readVarInt(from, 2);
        String str = from.toString(from.readerIndex(), len, StandardCharsets.UTF_8);
        from.readerIndex(from.readerIndex() + len);
        return str;
    }

    public static void writeUTF8String(ByteBuf to, String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        Validate.isTrue(varIntByteCount(bytes.length) < 3, "The string is too long for this encoding.");
        writeVarInt(to, bytes.length, 2);
        to.writeBytes(bytes);
    }

    public static void writeItemStack(ByteBuf to, ItemStack stack) {
        new PacketByteBuf(to).writeItem(stack);
    }

    public static ItemStack readItemStack(ByteBuf from) {
        try {
            return new PacketByteBuf(from).readItem();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeTag(ByteBuf to, NbtCompound tag) {
        new PacketByteBuf(to).writeNbtCompound(tag);
    }

    public static NbtCompound readTag(ByteBuf from) {
        try {
            return new PacketByteBuf(from).readNbtCompound();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String getContentDump(ByteBuf buffer) {
        return ByteBufUtil.hexDump(buffer);
    }
}
