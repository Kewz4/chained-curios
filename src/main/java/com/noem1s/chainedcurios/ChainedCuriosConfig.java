package com.noem1s.chainedcurios;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = "chainedcurios", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChainedCuriosConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue RENDER_UNDERLAYER;

    static {
        BUILDER.push("Client Settings");
        RENDER_UNDERLAYER = BUILDER.comment("Whether to render the underlayer armor visuals. If false, only stats will be applied.")
                .define("renderUnderlayer", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        // config loaded
    }
}
