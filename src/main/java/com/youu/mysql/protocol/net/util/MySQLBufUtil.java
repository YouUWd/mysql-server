package com.youu.mysql.protocol.net.util;

import java.nio.charset.Charset;
import java.util.EnumSet;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CodecException;

/**
 * @Author Timmy
 * @Description MySQL Little Endian 小端字节序
 * @Date 2021/6/18
 */
public class MySQLBufUtil {
    private static final int NULL_VALUE = 0xfb;
    private static final int SHORT_VALUE = 0xfc;
    private static final int MEDIUM_VALUE = 0xfd;
    private static final int LONG_VALUE = 0xfe;

    public static int readUB2(ByteBuf buf) {
        return buf.readShortLE();
    }

    public static int readUB3(ByteBuf buf) {
        return buf.readUnsignedMediumLE();
    }

    public static long readUB4(ByteBuf buf) {
        return buf.readUnsignedIntLE();
    }

    public static long readLengthEncodedInteger(ByteBuf buf) {
        long firstByte = buf.readByte() & 0xff;
        if (firstByte < NULL_VALUE) {
            return firstByte;
        }
        if (firstByte == NULL_VALUE) {
            return -1;
        }
        if (firstByte == SHORT_VALUE) {
            return buf.readUnsignedShortLE();
        }
        if (firstByte == MEDIUM_VALUE) {
            return buf.readUnsignedMediumLE();
        }
        if (firstByte == LONG_VALUE) {
            final long length = buf.readLongLE();
            if (length < 0) {
                throw new CodecException("Received a length value too large to handle: " + Long.toHexString(length));
            }
            return length;
        }
        throw new CodecException("Received an invalid length value " + firstByte);
    }

    public static String readLenEncString(ByteBuf buf) {
        return readLenEncString(buf, Charset.defaultCharset());
    }

    public static String readLenEncString(ByteBuf buf, String charsetName) {
        return readLenEncString(buf, Charset.forName(charsetName));
    }

    public static String readLenEncString(ByteBuf buf, Charset charset) {
        long strLen = readLengthEncodedInteger(buf);
        String str = buf.toString(buf.readerIndex(), (int)strLen, charset);
        buf.skipBytes((int)strLen);
        return str;
    }

    public static String readNullTerminatedString(ByteBuf buf) {
        return readNullTerminatedString(buf, Charset.defaultCharset());
    }

    public static String readNullTerminatedString(ByteBuf buf, Charset charset) {
        int nullIndex = buf.indexOf(buf.readerIndex(), buf.capacity(), (byte)0);
        byte[] bytes = new byte[nullIndex - buf.readerIndex()];
        buf.readBytes(bytes);
        buf.skipBytes(1);//skip null
        return new String(bytes, charset);
    }

    public static void writeLengthEncodedInt(ByteBuf buf, Long n) {
        if (n == null) {
            buf.writeByte(NULL_VALUE);
        } else if (n < 0) {
            throw new IllegalArgumentException("Cannot encode a negative length: " + n);
        } else if (n < NULL_VALUE) {
            buf.writeByte(n.intValue());
        } else if (n < 0xffff) {
            buf.writeByte(SHORT_VALUE);
            buf.writeShortLE(n.intValue());
        } else if (n < 0xffffff) {
            buf.writeByte(MEDIUM_VALUE);
            buf.writeMediumLE(n.intValue());
        } else {
            buf.writeByte(LONG_VALUE);
            buf.writeLongLE(n);
        }
    }

    public static void writeLenEncString(ByteBuf buf, String str) {
        writeLenEncString(buf, str, Charset.defaultCharset());
    }

    public static void writeLenEncString(ByteBuf buf, String str, String charset) {
        writeLenEncString(buf, str, Charset.forName(charset));
    }

    public static void writeLenEncString(ByteBuf buf, String str, Charset charset) {
        byte[] data = str.getBytes(charset);
        long strLen = data.length;
        writeLengthEncodedInt(buf, strLen);
        buf.writeBytes(data);
    }

    public static <E extends Enum<E>> EnumSet<E> readIntEnumSet(ByteBuf buf, Class<E> enumClass) {
        return toEnumSet(enumClass, buf.readUnsignedIntLE());
    }

    public static <E extends Enum<E>> EnumSet<E> toEnumSet(Class<E> enumClass, long vector) {
        EnumSet<E> set = EnumSet.noneOf(enumClass);
        for (E e : enumClass.getEnumConstants()) {
            final long mask = 1 << e.ordinal();
            if ((mask & vector) != 0) {
                set.add(e);
            }
        }
        return set;
    }

    public static void writeNullTerminatedString(ByteBuf buf, String data) {
        writeNullTerminatedString(buf, data, Charset.defaultCharset());
    }

    public static void writeNullTerminatedString(ByteBuf buf, String data, Charset charset) {
        buf.writeCharSequence(data, charset);
        buf.writeByte(0);
    }
}
