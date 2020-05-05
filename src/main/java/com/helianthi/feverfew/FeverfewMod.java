package com.helianthi.feverfew;

import com.helianthi.feverfew.padlock.Padlock;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FeverfewMod implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "feverfew";
    public static final String MOD_NAME = "Feverfew";


    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing " + MOD_NAME);
        Padlock.initialise();
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}