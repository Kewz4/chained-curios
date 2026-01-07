package com.noem1s.chainedcurios.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider.TagLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModItemTagProvider extends ItemTagsProvider {
   public static final TagKey<Item> UNDERLAYER_HEAD = TagKey.create(Registries.ITEM, new ResourceLocation("curios", "underlayer_head"));
   public static final TagKey<Item> UNDERLAYER_CHEST = TagKey.create(Registries.ITEM, new ResourceLocation("curios", "underlayer_chest"));
   public static final TagKey<Item> UNDERLAYER_LEGS = TagKey.create(Registries.ITEM, new ResourceLocation("curios", "underlayer_legs"));
   public static final TagKey<Item> UNDERLAYER_FEET = TagKey.create(Registries.ITEM, new ResourceLocation("curios", "underlayer_feet"));

   public ModItemTagProvider(
      PackOutput output,
      CompletableFuture<Provider> lookupProvider,
      CompletableFuture<TagLookup<Block>> blockTagProvider,
      @Nullable ExistingFileHelper existingFileHelper
   ) {
      super(output, lookupProvider, blockTagProvider, "chainedcurios", existingFileHelper);
   }

   @Override
   protected void addTags(Provider pProvider) {
      this.tag(UNDERLAYER_HEAD).add(Items.CHAINMAIL_HELMET);
      this.tag(UNDERLAYER_CHEST).add(Items.CHAINMAIL_CHESTPLATE);
      this.tag(UNDERLAYER_LEGS).add(Items.CHAINMAIL_LEGGINGS);
      this.tag(UNDERLAYER_FEET).add(Items.CHAINMAIL_BOOTS);
   }
}
