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

        PadlockItem.initialise();

    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}