package net.usefullanterns;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.util.Identifier;

public class UsefulLanternsConfig extends Config {
    public static UsefulLanternsConfig config = ConfigApiJava.registerAndLoadConfig(UsefulLanternsConfig::new, RegisterType.CLIENT);
    public boolean lanternOnRightSide = false;

    public UsefulLanternsConfig() {
        super(Identifier.of(UsefulLanterns.MOD_ID, "config"));
    }

    public static void init() {
    }
}