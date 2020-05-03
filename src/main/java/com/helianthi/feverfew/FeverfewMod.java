package com.helianthi.feverfew;

import com.helianthi.feverfew.items.KeyItem;
import com.helianthi.feverfew.items.PadlockItem;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FeverfewMod implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "feverfew";
    public static final String MOD_NAME = "Feverfew";

    public static final PadlockItem PADLOCK_ITEM = new PadlockItem(new Item.Settings().group(ItemGroup.TOOLS));
    public static final KeyItem KEY_ITEM = new KeyItem(new Item.Settings().group(ItemGroup.TOOLS));

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing " + MOD_NAME);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "padlock_item"), PADLOCK_ITEM);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "key_item"), KEY_ITEM);
        PadlockItem.initialise();
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}