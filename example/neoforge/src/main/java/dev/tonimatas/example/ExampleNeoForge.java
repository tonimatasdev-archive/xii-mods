package dev.tonimatas.example;

import net.neoforged.fml.common.Mod;

@Mod(Example.MOD_ID)
public class ExampleNeoForge {
    public ExampleNeoForge() {
        Example.init();
    }
}
