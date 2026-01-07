package com.noem1s.chainedcurios.common.event;

import java.util.Collections;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@EventBusSubscriber(
   modid = "chainedcurios",
   bus = Bus.FORGE
)
public class ModForgeEvents {
   @SubscribeEvent
   public static void onLivingHurt(LivingHurtEvent event) {
      if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
         float damage = event.getAmount();
         if (damage <= 0.0F) {
            return;
         }

         float totalEnchantmentProtection = 0.0F;
         IItemHandlerModifiable equipped = (IItemHandlerModifiable)CuriosApi.getCuriosInventory(player).map(ICuriosItemHandler::getEquippedCurios).orElse(null);

         // Calculate enchantment protection from underlayer items
         if (equipped != null) {
            for (int i = 0; i < equipped.getSlots(); i++) {
               ItemStack stack = equipped.getStackInSlot(i);
               if (stack.getItem() instanceof ArmorItem) { // Applies to ALL armor in Curios slots
                  totalEnchantmentProtection += (float)EnchantmentHelper.getDamageProtection(Collections.singletonList(stack), event.getSource());
               }
            }
         }

         // Reduce damage based on enchantment protection
         // Formula: damage * (1 - protection / 25)
         float damageAfterEnchants = damage * (1.0F - Math.min(20.0F, totalEnchantmentProtection) / 25.0F);
         event.setAmount(damageAfterEnchants);

         float finalDamage = event.getAmount();
         int durabilityDamage = (int)Math.max(1.0F, finalDamage / 4.0F);

         // Apply durability damage to underlayer items
         if (equipped != null && durabilityDamage > 0) {
            for (int ix = 0; ix < equipped.getSlots(); ix++) {
               ItemStack stack = equipped.getStackInSlot(ix);
               if (stack.getItem() instanceof ArmorItem) {
                  EquipmentSlot vanillaSlot = getVanillaSlot(stack.getItem());
                  if (vanillaSlot != null) {
                      ItemStack vanillaArmor = player.getItemBySlot(vanillaSlot);
                      int finalDurabilityDamage = durabilityDamage;

                      // Smart Durability: 60% reduction if wearing outer armor
                      if (!vanillaArmor.isEmpty()) {
                         finalDurabilityDamage = (int)((float)durabilityDamage * 0.4F);
                      }

                      if (finalDurabilityDamage > 0) {
                         // broadcastBreakEvent needs a slot, but Curios items don't have a standard EquipmentSlot.
                         // Using the vanilla slot for the break sound/animation is an approximation.
                         stack.hurtAndBreak(finalDurabilityDamage, player, p -> p.broadcastBreakEvent(vanillaSlot));
                      }
                  }
               }
            }
         }
      }
   }

   private static EquipmentSlot getVanillaSlot(Item item) {
      return item instanceof ArmorItem armor ? armor.getEquipmentSlot() : null;
   }
}
