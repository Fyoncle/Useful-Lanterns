package net.usefullanterns;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            UsefulLanternsConfig config = UsefulLanternsConfig.get();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("Useful Lanterns"))
                    .setSavingRunnable(config::save);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

            general.addEntry(entryBuilder
                    .startBooleanToggle(Text.literal("Flip Lantern"), config.flipLantern)
                    .setDefaultValue(false)
                    .setTooltip(Text.literal("Moves the lantern to the right side of the body instead of the left."))
                    .setSaveConsumer(val -> config.flipLantern = val)
                    .build()
            );

            return builder.build();
        };
    }
}