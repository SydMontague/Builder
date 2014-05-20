package de.craftlancer.builder;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

class PreviewRemoveTask extends BukkitRunnable
{
    private Player player;
    private Block initialBlock;
    
    private int xmax;
    private int ymax;
    private int zmax;
    
    public PreviewRemoveTask(Player player, Block initialBlock, int xmax, int ymax, int zmax)
    {
        this.player = player;
        this.initialBlock = initialBlock;
        this.xmax = xmax;
        this.ymax = ymax;
        this.zmax = zmax;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void run()
    {
        for (int x = 0; x <= xmax; x++)
            for (int y = 0; y <= ymax; y++)
                for (int z = 0; z <= zmax; z++)
                {
                    Block b = initialBlock.getRelative(x, y, z);
                    
                    player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
                }
    }
}