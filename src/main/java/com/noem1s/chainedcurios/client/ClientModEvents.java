package com.noem1s.chainedcurios.client;

import com.noem1s.chainedcurios.ChainedCurios;
import com.noem1s.chainedcurios.client.renderer.UnderlayerRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(
   modid = "chainedcurios",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class ClientModEvents {

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent event) {
      ChainedCurios.LOGGER.info("Registering Curios renderers...");

      for (Item item : ForgeRegistries.ITEMS) {
         if (item instanceof ArmorItem armorItem) {
            EquipmentSlot slot = armorItem.getEquipmentSlot();
            CuriosRendererRegistry.register(item, () -> new UnderlayerRenderer(slot));
         }
      }

      ChainedCurios.LOGGER.info("Curios renderers successfully registered.");
   }
}
