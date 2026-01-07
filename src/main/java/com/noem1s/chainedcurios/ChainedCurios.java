package com.noem1s.chainedcurios;

import com.noem1s.chainedcurios.common.capability.CapabilityAttacher;
import com.noem1s.chainedcurios.common.event.ModForgeEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("chainedcurios")
public class ChainedCurios {
   public static final String MOD_ID = "chainedcurios";
   public static final Logger LOGGER = LoggerFactory.getLogger(ChainedCurios.class);

   public ChainedCurios() {
      LOGGER.info("ChainedCurios Mod Constructor loading...");
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

      ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ChainedCuriosConfig.SPEC);

      MinecraftForge.EVENT_BUS.register(new ModForgeEvents());
      MinecraftForge.EVENT_BUS.register(CapabilityAttacher.class);
      LOGGER.info("Event handlers registered.");
   }
}
