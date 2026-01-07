package com.noem1s.chainedcurios.util;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class ChainedCuriosHelper {
   public static boolean tryMoveToCuriosSlot(
      Player pPlayer, ItemStack stackToMove, Slot sourceSlot, CallbackInfoReturnable<ItemStack> cir, boolean isCreativeTab
   ) {
      AtomicBoolean movedToCurio = new AtomicBoolean(false);
      CuriosApi.getCuriosInventory(pPlayer)
         .ifPresent(
            curiosInv -> {
               for (Entry<String, ICurioStacksHandler> entry : curiosInv.getCurios().entrySet()) {
                  ICurioStacksHandler handler = entry.getValue();
                  IDynamicStackHandler stacks = handler.getStacks();
                  int i = 0;

                  while (true) {
                     if (i < stacks.getSlots()) {
                        if (!stacks.isItemValid(i, stackToMove) || !stacks.getStackInSlot(i).isEmpty()) {
                           i++;
                           continue;
                        }

                        if (stackToMove.getItem() instanceof Equipable equipable) {
                           pPlayer.level()
                              .playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), equipable.getEquipSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        }

                        ItemStack stackToInsert = isCreativeTab ? stackToMove.copy() : stackToMove;
                        stacks.insertItem(i, stackToInsert, false);
                        if (!isCreativeTab) {
                           sourceSlot.set(ItemStack.EMPTY);
                        }

                        cir.setReturnValue(ItemStack.EMPTY);
                        movedToCurio.set(true);
                     }

                     if (movedToCurio.get()) {
                        return;
                     }
                     break;
                  }
               }
            }
         );
      return movedToCurio.get();
   }
}
