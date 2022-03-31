package net.serversleepmod;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.world.GameRules;
import net.minecraft.world.level.ServerWorldProperties;

public class ServerSleep implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("serversleepmod");

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);
    }

    // method that checks for sleeping players
    private void onTick(MinecraftServer server) {
        server.getWorlds().forEach((world)->{
            List<ServerPlayerEntity> players = world.getPlayers();
            for(PlayerEntity p : players) {
                if(p.isSleeping()){
                    skipNight(world, players, p.getUuid());
                }
            }
        });
    }

    // method that actually skips the night
    private void skipNight(ServerWorld world, List<ServerPlayerEntity> players, UUID sleeper) {
        // set world stuff
        if(world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)){
            long l = world.getLevelProperties().getTimeOfDay() + 24000L;
            world.setTimeOfDay(l - l%24000);
        }

        // reset weather
        if(world.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)){
            ((ServerWorldProperties)world.getLevelProperties()).setRainTime(0);
            ((ServerWorldProperties)world.getLevelProperties()).setRaining(false);
            ((ServerWorldProperties)world.getLevelProperties()).setThunderTime(0);
            ((ServerWorldProperties)world.getLevelProperties()).setThundering(false);
        }

        //sleepers username
        PlayerEntity sleep = world.getPlayerByUuid(sleeper);

        // wake up sleeping players
        players.forEach(p->{
            if(p.isSleeping()){

                p.sendSystemMessage(new LiteralText(sleep.getEntityName() + " is sleeping"), sleeper);
                p.wakeUp(false, true);
            }
        });
    }
}