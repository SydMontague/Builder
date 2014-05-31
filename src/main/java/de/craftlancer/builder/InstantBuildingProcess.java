package de.craftlancer.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;

public class InstantBuildingProcess implements BuildingProcess
{
    private Building building;
    private UUID owner;
    private BuildState buildState;
    
    private List<BlockState> undoList = new ArrayList<BlockState>();
    
    public InstantBuildingProcess(Building building, Player player)
    {
        this.owner = player.getUniqueId();
        this.building = building;
        
        CuboidClipboard clip = building.getRotatedClipboard(player);
        Block start = player.getLocation().getBlock().getRelative(clip.getOffset().getBlockX(), clip.getOffset().getBlockY(), clip.getOffset().getBlockZ());
        
        LocalWorld world = null;
        for (LocalWorld w : WorldEdit.getInstance().getServer().getWorlds())
            if (w.getName().equals(start.getWorld().getName()))
            {
                world = w;
                break;
            }
        
        if (world == null)
            throw new NullPointerException("This world should never be null!");
        
        for (int x = 0; x < clip.getWidth(); x++)
            for (int y = 0; y < clip.getHeight(); y++)
                for (int z = 0; z < clip.getLength(); z++)
                {
                    BlockState orgiBlock = start.getWorld().getBlockAt(start.getX() + x, start.getY() + y, start.getZ() + z).getState();
                    undoList.add(orgiBlock);
                    
                    BaseBlock b = clip.getBlock(new Vector(x, y, z));
                    world.setBlock(new Vector(start.getX() + x, start.getY() + y, start.getZ() + z), b, false);
                }
        
        this.buildState = BuildState.FINISHED;
    }
    
    @Override
    public UUID getOwner()
    {
        return owner;
    }
    
    @Override
    public int getBlocksSet()
    {
        return building.getNumBlocks();
    }
    
    @Override
    public Building getBuilding()
    {
        return building;
    }
    
    @Override
    public BuildState getState()
    {
        return buildState;
    }
    
    @Override
    public void undo()
    {
        for (BlockState state : undoList)
            state.update(true);
        
        undoList.clear();
        buildState = BuildState.REMOVED;
    }

    @Override
    public void prepareForShutdown()
    {
        //do nothing
    }
}
