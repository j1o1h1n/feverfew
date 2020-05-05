package com.helianthi.feverfew.padlock;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.HitResult;
import org.apache.logging.log4j.Level;

import static com.helianthi.feverfew.FeverfewMod.log;

/**
 * A Flint and Steel may be used to lock a chest.  First, the Flint and Steel
 * must be named, using an Anvil. Then when the Flint and Steel is used on an
 * unlocked chest, the flint and steel will be consumed and the chest locked
 * with the name as a the secret key.
 *
 * To open a locked chest the player must be holding an item with the secret name.
 *
 * To take the lock off a chest, hit it with an item with the secret name.
 */
public abstract class Padlock {

    public static String PADLOCK_UNNAMED = "padlock.unnamed";
    public static String PADLOCK_REMOVED = "padlock.removed";
    public static String PADLOCK_LOCKED = "padlock.locked";
    public static String PADLOCK_SUCCESS = "padlock.success";
    public static String PADLOCK_BREAK = "padlock.break";

    public static void initialise() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!player.isSpectator()
                    && player.getMainHandStack().getItem() == Items.FLINT_AND_STEEL
                    && hitResult.getType() == HitResult.Type.BLOCK
                    && world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.CHEST)
            {
                // if the Flint and Steel has no name, display a failure message
                // if the chest is already locked, display failure message
                // else lock the chest and consume the lock
                ItemStack stack = player.getMainHandStack();
                if (!stack.hasCustomName()) {
                    player.sendMessage(new TranslatableText(PADLOCK_UNNAMED), true);
                    player.playSound(SoundEvents.BLOCK_DISPENSER_FAIL,
                            SoundCategory.BLOCKS, 1.0F, 1.0F);
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
                    player.sendMessage(new TranslatableText(PADLOCK_LOCKED), true);
                    return ActionResult.FAIL;
                }
                String lockName = stack.getName().getString();
                tag.putString("Lock", lockName);
                blockEntity.fromTag(blockEntity.getCachedState(), tag);
                stack.decrement(1);
                player.sendMessage(new TranslatableText(PADLOCK_SUCCESS, lockName), true);
                player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!player.isSpectator()
                    && world.getBlockState(pos).getBlock() == Blocks.CHEST)
            {
                // a locked chest cannot be broken, but if attacked with the key the lock will come off
                if (world.isClient) {
                    return ActionResult.PASS;
                }
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (!(blockEntity instanceof ChestBlockEntity)) {
                    // shouldn't happen
                    return ActionResult.PASS;
                }
                CompoundTag tag = new CompoundTag();
                blockEntity.toTag(tag);
                // check whether the chest is locked
                if (!tag.contains("Lock")) {
                    return ActionResult.PASS;
                }
                String lockName = tag.getString("Lock");

                // a padlocked chest cannot be broken, but if the player has the right key, the lock will come off
                ItemStack stack = player.getMainHandStack();
                if (stack.hasCustomName()
                        && stack.getName().asString().equals(lockName)) {
                    tag.remove("Lock");
                    player.sendMessage(new TranslatableText(PADLOCK_REMOVED), true);
                    blockEntity.fromTag(blockEntity.getCachedState(), tag);

                    // create Flint and Steel at the chest position
                    ItemStack padlock = new ItemStack(Items.FLINT_AND_STEEL);
                    ItemEntity itemEntity = new ItemEntity(player.world, pos.getX(), pos.getY(), pos.getZ(), padlock);
                    player.world.spawnEntity(itemEntity);
                    return ActionResult.FAIL;
                }

                player.sendMessage(new TranslatableText(PADLOCK_BREAK), true);
                player.damage(DamageSource.MAGIC, 5.0f);
                player.setFireTicks(30);
                player.playSound(SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE, 1.0F, 1.0F);

                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }
}
