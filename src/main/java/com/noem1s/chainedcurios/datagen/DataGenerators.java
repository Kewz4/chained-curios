package com.noem1s.chainedcurios.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(
   modid = "chainedcurios",
   bus = Bus.MOD
)
public class DataGenerators {
   @SubscribeEvent
   public static void onGatherData(GatherDataEvent event) {
      DataGenerator generator = event.getGenerator();
      PackOutput packOutput = generator.getPackOutput();
      CompletableFuture<Provider> lookupProvider = event.getLookupProvider();
      ExistingFileHelper fileHelper = event.getExistingFileHelper();
      BlockTagsProvider blockTagsProvider = new BlockTagsProvider(packOutput, lookupProvider, "chainedcurios", fileHelper) {
         @Override
         protected void addTags(@NotNull Provider pProvider) {
         }
      };
      generator.addProvider(event.includeServer(), blockTagsProvider);
      generator.addProvider(event.includeServer(), new ModItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), fileHelper));
      generator.addProvider(event.includeServer(), new CuriosSlotProvider(packOutput, lookupProvider, fileHelper));
   }
}
