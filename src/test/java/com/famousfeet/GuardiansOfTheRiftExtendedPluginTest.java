package com.famousfeet;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GuardiansOfTheRiftExtendedPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(GuardiansOfTheRiftExtendedPlugin.class);
        RuneLite.main(args);
    }
}