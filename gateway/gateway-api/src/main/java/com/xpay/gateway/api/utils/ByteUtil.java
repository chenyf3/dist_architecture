package com.xpay.gateway.api.utils;

import io.netty.buffer.ByteBufAllocator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteUtil {

    public static byte[] getBytes(String content){
        return content.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] addBytes(byte[] first, byte[] second) {
        final byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static DataBuffer toDataBuffer(byte[] bytes) {
        final NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        final DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }
}
