package com.noem1s.chainedcurios.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({InventoryMenu.class})
public abstract class InventoryMenuMixin {

   @Redirect(
      method = {"quickMoveStack"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/Mob;getEquipmentSlotForItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EquipmentSlot;"
      )
   )
   private EquipmentSlot chainedcurios$redirectEquipmentCheck(ItemStack stack) {
      // Return the actual slot so standard shift-click works (goes to armor slots)
      return Mob.getEquipmentSlotForItem(stack);
   }
}
