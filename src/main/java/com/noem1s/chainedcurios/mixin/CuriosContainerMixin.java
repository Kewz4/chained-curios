package com.noem1s.chainedcurios.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.theillusivec4.curios.common.inventory.container.CuriosContainerV2;

@Mixin({CuriosContainerV2.class})
public class CuriosContainerMixin {
   @Redirect(
      method = {"quickMoveStack"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/Mob;getEquipmentSlotForItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EquipmentSlot;"
      )
   )
   private EquipmentSlot chainedcurios$redirectEquipmentSlotCheck(ItemStack stack) {
      // Same as InventoryMenuMixin, we restore standard behavior.
      return Mob.getEquipmentSlotForItem(stack);
   }
}
