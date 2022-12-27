package com.gmail.s154095g.dp_snake.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;

public class SnakeController extends Item {
    public final int x;
    public final int z;

    public SnakeController(int x, int z) {
        super(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE));
        this.x = x;
        this.z = z;
    }
}
