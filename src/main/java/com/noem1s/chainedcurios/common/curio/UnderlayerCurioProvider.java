package com.noem1s.chainedcurios.common.curio;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurio.SoundInfo;

public class UnderlayerCurioProvider implements ICapabilityProvider {
   private final LazyOptional<ICurio> curioOptional;

   public UnderlayerCurioProvider(ItemStack stack) {
      this.curioOptional = LazyOptional.of(
         () -> new ICurio() {
               public ItemStack getStack() {
                  return stack;
               }

               public boolean canEquipFromUse(SlotContext slotContext) {
                  return true;
               }

               public boolean canEquip(SlotContext slotContext) {
                  if (stack.getItem() instanceof ArmorItem armorItem) {
                     EquipmentSlot slot = armorItem.getEquipmentSlot();
                     String slotIdentifier = slotContext.identifier();

                     return switch (slot) {
                        case HEAD -> slotIdentifier.equals("underlayer_head");
                        case CHEST -> slotIdentifier.equals("underlayer_chest");
                        case LEGS -> slotIdentifier.equals("underlayer_legs");
                        case FEET -> slotIdentifier.equals("underlayer_feet");
                        default -> false;
                     };
                  } else {
                     return false;
                  }
               }

               public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
                  Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                  if (stack.getItem() instanceof ArmorItem armorItem) {
                     EquipmentSlot slot = armorItem.getEquipmentSlot();
                     Multimap<Attribute, AttributeModifier> defaultModifiers = stack.getAttributeModifiers(slot);

                     for (Entry<Attribute, AttributeModifier> entry : defaultModifiers.entries()) {
                        AttributeModifier originalModifier = entry.getValue();
                        builder.put(
                           entry.getKey(), new AttributeModifier(uuid, originalModifier.getName(), originalModifier.getAmount(), originalModifier.getOperation())
                        );
                     }
                  }

                  return builder.build();
               }

               @Nonnull
               public SoundInfo getEquipSound(SlotContext slotContext) {
                  if (stack.getItem() instanceof ArmorItem armorItem) {
                      return new SoundInfo(armorItem.getEquipSound(), 1.0F, 1.0F);
                  }
                  return new SoundInfo(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
               }
            }
      );
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      return CuriosCapability.ITEM.orEmpty(cap, this.curioOptional);
   }
}
