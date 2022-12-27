package com.gmail.s154095g.dp_snake;

import com.gmail.s154095g.dp_snake.command.SnakeCommand;
import com.gmail.s154095g.dp_snake.item.SnakeController;
import com.gmail.s154095g.dp_snake.snake.SnakeGame;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;

public class SnakeMod implements ModInitializer {
	public static final String MOD_ID = "dp_snake";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Item SNAKE_NORTH = Registry.register(Registries.ITEM, Utils.identifier("snake_north"), new SnakeController(0, -1));
	public static final Item SNAKE_EAST = Registry.register(Registries.ITEM, Utils.identifier("snake_east"), new SnakeController(1, 0));
	public static final Item SNAKE_SOUTH = Registry.register(Registries.ITEM, Utils.identifier("snake_south"), new SnakeController(0, 1));
	public static final Item SNAKE_WEST = Registry.register(Registries.ITEM, Utils.identifier("snake_west"), new SnakeController(-1, 0));

	public static final HashMap<ServerPlayerEntity, SnakeGame> activeGames = new HashMap<>();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> SnakeCommand.register(dispatcher)));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> SnakeGame.removeControllerItems(handler.player));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> removeGame(handler.player));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (SnakeGame game : activeGames.values()){
				game.tick();
				if (server.getTicks() % 10 == 0){
					game.update();
				}
			}
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			for (SnakeGame game : activeGames.values()){
				game.stop();
			}
			activeGames.clear();
		});

		LOGGER.info("Successfully set up dp_snake mod!");
	}

	public static void addGame(ServerPlayerEntity player, BlockPos corner1, BlockPos corner2){
		Objects.requireNonNull(player, "Missing player!");
		Objects.requireNonNull(corner1, "Missing corner 1!");
		Objects.requireNonNull(corner2, "Missing corner 2!");

		if (activeGames.containsKey(player)){
			activeGames.get(player).stop();
		}

		activeGames.put(player, new SnakeGame(player, corner1, corner2));
	}

	public static boolean removeGame(ServerPlayerEntity player){
		if (player == null) return false;
		if (activeGames.containsKey(player))
			activeGames.get(player).stop();
		return activeGames.remove(player) != null;
	}
}