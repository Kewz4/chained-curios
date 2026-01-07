package com.noem1s.chainedcurios.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;
import org.jetbrains.annotations.NotNull;

public class CuriosSlotProvider extends CuriosDataProvider {
   public CuriosSlotProvider(PackOutput output, CompletableFuture<Provider> registries, ExistingFileHelper fileHelper) {
      super("chainedcurios", output, fileHelper, registries);
   }

   @Override
   public void generate(Provider registries, ExistingFileHelper fileHelper) {
      this.createSlot("underlayer_head")
         .order(10)
         .icon(new ResourceLocation("minecraft", "item/empty_armor_slot_helmet"))
         .renderToggle(false)
         .size(1);
      this.createSlot("underlayer_chest")
         .order(20)
         .icon(new ResourceLocation("minecraft", "item/empty_armor_slot_chestplate"))
         .renderToggle(false)
         .size(1);
      this.createSlot("underlayer_legs")
         .order(30)
         .icon(new ResourceLocation("minecraft", "item/empty_armor_slot_leggings"))
         .renderToggle(false)
         .size(1);
      this.createSlot("underlayer_feet")
         .order(40)
         .icon(new ResourceLocation("minecraft", "item/empty_armor_slot_boots"))
         .renderToggle(false)
         .size(1);
      this.createEntities("player").addPlayer().addSlots(new String[]{"underlayer_head", "underlayer_chest", "underlayer_legs", "underlayer_feet"});
   }

   @Override
   @NotNull
   public String getName() {
      return "Chained Curios Slots";
   }
}
