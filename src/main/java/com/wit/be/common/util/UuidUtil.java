package com.wit.be.common.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class UuidUtil {

    private UuidUtil() {}

    /**
     * UUID를 생성하고 16바이트 배열로 변환합니다.
     *
     * @return 16바이트 UUID 배열
     */
    public static byte[] generateUuidBytes() {
        UUID uuid = UUID.randomUUID();
        return uuidToBytes(uuid);
    }

    /**
     * UUID를 16바이트 배열로 변환합니다.
     *
     * @param uuid UUID 객체
     * @return 16바이트 배열
     */
    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * 16바이트 배열을 UUID로 변환합니다.
     *
     * @param bytes 16바이트 배열
     * @return UUID 객체
     */
    public static UUID bytesToUuid(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * 16바이트 배열을 UUID 문자열로 변환합니다.
     *
     * @param bytes 16바이트 배열
     * @return UUID 문자열 (예: "550e8400-e29b-41d4-a716-446655440000")
     */
    public static String bytesToUuidString(byte[] bytes) {
        return bytesToUuid(bytes).toString();
    }

    /**
     * UUID 문자열을 16바이트 배열로 변환합니다.
     *
     * @param uuidString UUID 문자열
     * @return 16바이트 배열
     */
    public static byte[] uuidStringToBytes(String uuidString) {
        UUID uuid = UUID.fromString(uuidString);
        return uuidToBytes(uuid);
    }
}
