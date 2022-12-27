package com.gmail.s154095g.dp_snake.command;

import com.gmail.s154095g.dp_snake.SnakeMod;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.*;

public class SnakeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(literal("snake")
                .then(literal("stop")
                    .executes(SnakeCommand::stop)
                )
                .then(argument("pos1", BlockPosArgumentType.blockPos())
                        .then(argument("pos2", BlockPosArgumentType.blockPos())
                                .executes(SnakeCommand::start)
                )
        ));
    }

    public static int start(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        BlockPos pos1 = BlockPosArgumentType.getLoadedBlockPos(context, "pos1");
        BlockPos pos2 = BlockPosArgumentType.getLoadedBlockPos(context, "pos2");

        SnakeMod.addGame(source.getPlayerOrThrow(), pos1, pos2);

        return Command.SINGLE_SUCCESS;
    }

    public static int stop(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        if (!SnakeMod.removeGame(source.getPlayerOrThrow())){
            source.sendError(Text.translatable("command.dp_snake.snake.stop.failed"));
        }

        return Command.SINGLE_SUCCESS;
    }
}
