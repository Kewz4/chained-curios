package com.noem1s.chainedcurios.client;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "chainedcurios",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public class ClientForgeEvents {
   @SubscribeEvent(
      priority = EventPriority.LOWEST
   )
   public static void onItemTooltip(ItemTooltipEvent event) {
      List<Component> tooltips = event.getToolTip();

      for (int i = tooltips.size() - 1; i >= 0; i--) {
         String text = tooltips.get(i).getString();
         if (text.contains("curios.modifiers")) {
            if (i + 1 < tooltips.size()) {
               String nextLine = tooltips.get(i + 1).getString();
               if (nextLine.contains("Armor") && nextLine.contains("+")) {
                  tooltips.remove(i + 1);
               }
            }

            tooltips.remove(i);
            if (i - 1 >= 0) {
               String prevLine = tooltips.get(i - 1).getString();
               if (prevLine.trim().isEmpty()) {
                  tooltips.remove(i - 1);
                  i--;
               }
            }
         }
      }

      while (!tooltips.isEmpty()) {
         int lastIndex = tooltips.size() - 1;
         String lastLine = tooltips.get(lastIndex).getString();
         if (!lastLine.trim().isEmpty()) {
            break;
         }

         tooltips.remove(lastIndex);
      }
   }
}
