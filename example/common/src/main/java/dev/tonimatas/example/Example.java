package dev.tonimatas.example;

import com.mojang.logging.LogUtils;

public class Example {
    public static final String MOD_ID = "example";
    
    public static void init() {
        LogUtils.getLogger().info("Example has been initialized successfully");
    }
}
