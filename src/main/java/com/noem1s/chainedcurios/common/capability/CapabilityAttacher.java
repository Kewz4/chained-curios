package com.noem1s.chainedcurios.common.capability;

import com.noem1s.chainedcurios.common.curio.UnderlayerCurioProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "chainedcurios",
   bus = Bus.FORGE
)
public class CapabilityAttacher {
   public static final ResourceLocation CURIOS_CAP_LOC = new ResourceLocation("chainedcurios", "curio");

   @SubscribeEvent
   public static void attachItemCaps(AttachCapabilitiesEvent<ItemStack> event) {
      ItemStack stack = (ItemStack)event.getObject();
      // Allow any ArmorItem to have the capability
      if (stack.getItem() instanceof ArmorItem) {
         UnderlayerCurioProvider provider = new UnderlayerCurioProvider(stack);
         event.addCapability(CURIOS_CAP_LOC, provider);
      }
   }
}
