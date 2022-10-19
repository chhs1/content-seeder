package com.example.torrent;

import java.util.Random;

public class RandomUtils {
    private static final Random RANDOM = new Random();

    public static byte[] randomBytes(int length) {
        byte[] buffer = new byte[length];
        RANDOM.nextBytes(buffer);
        return buffer;
    }

    public static long randomLong() {
        return RANDOM.nextLong();
    }

    public static int randomInteger() {
        return RANDOM.nextInt();
    }
}
