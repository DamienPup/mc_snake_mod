package com.gmail.s154095g.dp_snake.snake;

import com.gmail.s154095g.dp_snake.SnakeMod;
import com.gmail.s154095g.dp_snake.Utils;
import com.gmail.s154095g.dp_snake.item.SnakeController;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

public class SnakeGame {
    record ItemPos(int x, int z) {
        public BlockPos asBlockPos(int y) {
                return new BlockPos(x, y, z);
        }

        public boolean equals(ItemPos other){
            return this.x == other.x && this.z == other.z;
        }
    }
    
    public final ServerPlayerEntity player;
    public final ServerWorld world;
    public final Random random;

    public final int xmin;
    public final int xmax;
    public final int zmin;
    public final int zmax;

    public final double xcenter;
    public final double zcenter;
    
    public final int y;
    
    final ArrayList<ItemPos> snake = new ArrayList<>();
    ItemPos food;

    int dirX = 0;
    int dirZ = 0;

    public SnakeGame(ServerPlayerEntity player, BlockPos corner1, BlockPos corner2){
        Objects.requireNonNull(player, "Missing player!");
        Objects.requireNonNull(corner1, "Missing corner 1!");
        Objects.requireNonNull(corner2, "Missing corner 2!");

        this.player = player;
        world = player.getWorld();
        random = Random.createLocal();
        xmin = Math.min(corner1.getX(), corner2.getX());
        xmax = Math.max(corner1.getX(), corner2.getX());
        zmin = Math.min(corner1.getZ(), corner2.getZ());
        zmax = Math.max(corner1.getZ(), corner2.getZ());
        xcenter = (xmin + xmax) / 2d + 0.5d;
        zcenter = (zmin + zmax) / 2d + 0.5d;
        y = corner1.getY() + 1;

        snake.add(new ItemPos((int) (xcenter - 0.5d), (int) (zcenter - 0.5d)));

        for (int x = xmin - 1; x <= xmax + 1; x++){
            for (int z = zmin - 1; z <= zmax + 1; z++){
                boolean isBorder = x == xmin - 1 || x == xmax + 1 || z == zmin - 1 || z == zmax + 1;
                world.setBlockState(new BlockPos(x, y - 1, z), Blocks.LIME_WOOL.getDefaultState());
                for (int oy = 0; oy <= 2; oy++) {
                    world.setBlockState(new BlockPos(x, y + oy, z), (isBorder ? Blocks.LIME_STAINED_GLASS : Blocks.AIR).getDefaultState());
                }
            }
        }
        world.setBlockState(snake.get(0).asBlockPos(y), Blocks.RED_CONCRETE.getDefaultState());
        randomizeFoodPosition();

        if (player.getInventory().main.stream().noneMatch(stack -> stack.isOf(SnakeMod.SNAKE_NORTH)))
            player.giveItemStack(new ItemStack(SnakeMod.SNAKE_NORTH));
        if (player.getInventory().main.stream().noneMatch(stack -> stack.isOf(SnakeMod.SNAKE_EAST)))
            player.giveItemStack(new ItemStack(SnakeMod.SNAKE_EAST));
        if (player.getInventory().main.stream().noneMatch(stack -> stack.isOf(SnakeMod.SNAKE_SOUTH)))
            player.giveItemStack(new ItemStack(SnakeMod.SNAKE_SOUTH));
        if (player.getInventory().main.stream().noneMatch(stack -> stack.isOf(SnakeMod.SNAKE_WEST)))
            player.giveItemStack(new ItemStack(SnakeMod.SNAKE_WEST));

        player.getAbilities().allowFlying = true;
        player.getAbilities().flying = true;
        player.sendAbilitiesUpdate();
        player.teleport(world, xcenter, y + 1, zcenter, 180, 90); //inital teleport to make the player face down
    }

    public void tick(){
        if (!player.getAbilities().allowFlying){
            player.getAbilities().allowFlying = true;
            player.sendAbilitiesUpdate();
        }

        if (!isInbounds(player.getPos())){
            player.teleport(world, Utils.clamp(player.getX(), xmin, xmax), player.getY(), Utils.clamp(player.getZ(), zmin, zmax),
                            player.getYaw(), player.getPitch());
        } //stop player from leaving the play area
    }

    public void update(){
        ItemStack stack = player.getMainHandStack();
        if (!stack.isEmpty() && stack.getItem() instanceof SnakeController controller){
            dirX = controller.x;
            dirZ = controller.z;
        }

        if (dirX != 0 || dirZ != 0) {
            ItemPos head = snake.get(0);
            ItemPos nextPos = new ItemPos(head.x + dirX, head.z + dirZ);
            if (snake.size() > 1 && snake.get(1).equals(nextPos)) {
                dirX *= -1;
                dirZ *= -1;
                nextPos = new ItemPos(head.x + dirX, head.z + dirZ);
            }
            final ItemPos finalNextPos = nextPos;
            if (!isInbounds(nextPos) || snake.stream().anyMatch(pos -> pos.equals(finalNextPos))) {
                SnakeMod.removeGame(player); //stops this game
                return;
            }
            snake.add(0, nextPos);
            world.setBlockState(snake.get(0).asBlockPos(y), Blocks.RED_CONCRETE.getDefaultState());
            for (int i = 1; i < snake.size(); i++) {
                world.setBlockState(snake.get(i).asBlockPos(y),
                        (i % 2 == 0 ? Blocks.GREEN_CONCRETE : Blocks.LIME_CONCRETE).getDefaultState());
            }

            if (snake.get(0).equals(food)) {
                randomizeFoodPosition();
            } else {
                ItemPos tail = snake.remove(snake.size() - 1);
                world.setBlockState(tail.asBlockPos(y), Blocks.AIR.getDefaultState());
            }
        }
    }

    public void stop(){
        world.setBlockState(snake.get(0).asBlockPos(y), Blocks.LIGHT_GRAY_CONCRETE.getDefaultState());
        for (int i = 1; i < snake.size(); i++) {
            world.setBlockState(snake.get(i).asBlockPos(y), Blocks.GRAY_CONCRETE.getDefaultState());
        }
        
        player.getAbilities().allowFlying = (player.isCreative() || player.isSpectator());
        player.getAbilities().flying = player.getAbilities().allowFlying && player.getAbilities().flying;
        player.sendAbilitiesUpdate();
        removeControllerItems(player);
    }

    public static void removeControllerItems(ServerPlayerEntity player){
        Stream<ItemStack> stacks = player.getInventory().main.stream().filter(stack ->
                !stack.isEmpty() && stack.getItem() instanceof SnakeController);

        for (ItemStack stack : stacks.toList()){
            player.getInventory().removeOne(stack);
        }
    }

    boolean isInbounds(Vec3d position){
        double x = position.getX(), z = position.getZ();
        return (xmin <= x && x <= xmax) && (zmin <= z && z <= zmax);
    }

    boolean isInbounds(ItemPos position){
        double x = position.x, z = position.z;
        return (xmin <= x && x <= xmax) && (zmin <= z && z <= zmax);
    }

    void randomizeFoodPosition(){
        var ref = new Object() {
            ItemPos foodPos;
        }; //this is like this beuacse it's an error to change a variable used in lambda expressiotn (???)
        int attemptLimit = 1000000;
        do {
            ref.foodPos = new ItemPos(random.nextBetween(xmin, xmax), random.nextBetween(zmin, zmax));
            attemptLimit -= 1;
        } while (snake.stream().anyMatch(pos -> pos.equals(ref.foodPos)) && attemptLimit > 0);
        food = ref.foodPos;
        world.setBlockState(food.asBlockPos(y), Blocks.CARVED_PUMPKIN.getDefaultState());
    }
}
