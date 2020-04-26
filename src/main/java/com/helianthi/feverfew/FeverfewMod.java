package com.helianthi.feverfew;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.helianthi.feverfew.items.PadlockItem;
import com.helianthi.feverfew.items.KeyItem;

public class FeverfewMod implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "feverfew";
    public static final String MOD_NAME = "Feverfew";

    public static final PadlockItem PADLOCK_ITEM = new PadlockItem(new Item.Settings().group(ItemGroup.TOOLS));
    public static final KeyItem KEY_ITEM = new KeyItem(new Item.Settings().group(ItemGroup.TOOLS));

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing " + MOD_NAME);
        Registry.register(Registry.ITEM, new Identifier("feverfew", "padlock_item"), PADLOCK_ITEM);
        Registry.register(Registry.ITEM, new Identifier("feverfew", "key_item"), KEY_ITEM);

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!player.isSpectator()
                    && player.getMainHandStack().getItem() == PADLOCK_ITEM
                    && hitResult.getType() == HitResult.Type.BLOCK
                    && world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.CHEST)
            {
                // if the padlock has no name, display a failure message
                // if the chest is already locked, display failure message
                // else lock the chest and consume the lock
                ItemStack stack = player.getMainHandStack();
                if (!stack.hasCustomName()) {
                    player.sendMessage(new TranslatableText("padlock.unnamed"), true);
                    player.playSound(SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return ActionResult.FAIL;
                }
                if (world.isClient) {
                    return ActionResult.PASS;
                }
                BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
                if (!(blockEntity instanceof ChestBlockEntity)) {
                    log(Level.DEBUG, "Unexpected null or not chest block: " + blockEntity);
                    return ActionResult.PASS;
                }
                CompoundTag tag = new CompoundTag();
                blockEntity.toTag(tag);
                if (tag.contains("Lock")) {
                    player.sendMessage(new TranslatableText("padlock.already.locked"), true);
                    return ActionResult.FAIL;
                }
                String lockName = stack.getName().getString();
                tag.putString("Lock", lockName);
                blockEntity.fromTag(blockEntity.getCachedState(), tag);
                stack.decrement(1);
                player.sendMessage(new TranslatableText("padlock.success", lockName), true);
                player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!player.isSpectator()
                    && world.getBlockState(pos).getBlock() == Blocks.CHEST)
            {
                // if the chest is locked it cannot be broken, except with a diamond pickaxe
                if (world.isClient) {
                    return ActionResult.PASS;
                }
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (!(blockEntity instanceof ChestBlockEntity)) {
                    log(Level.DEBUG, "Unexpected null or not chest block: " + blockEntity);
                    return ActionResult.PASS;
                }
                if (world.isClient()) {
                    return ActionResult.PASS;
                }
                CompoundTag tag = new CompoundTag();
                blockEntity.toTag(tag);
                if (tag.contains("Lock")) {
                    player.sendMessage(new TranslatableText("break.locked.chest"), true);
                    player.damage(DamageSource.MAGIC, 5.0f);
                    player.setFireTicks(45);
                    player.addExhaustion(5.0f);
                    player.playSound(SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE, 1.0F, 1.0F);
                    if (player.getMainHandStack().getItem() != Items.DIAMOND_PICKAXE) {
                        return ActionResult.FAIL;
                    } else {
                        return ActionResult.PASS;
                    }
                }
            }
            return ActionResult.PASS;
        });


    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}