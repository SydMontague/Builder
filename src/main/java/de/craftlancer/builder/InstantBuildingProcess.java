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
    private Block block;
    private Building building;
    private UUID owner;
    private BuildState buildState;
    
    private int xmax;
    private int ymax;
    private int zmax;
    
    private List<BlockState> undoList = new ArrayList<BlockState>();
    
    public InstantBuildingProcess(Building building, Player player)
    {
        owner = player.getUniqueId();
        this.building = building;
        
        CuboidClipboard clip = building.getRotatedClipboard(player);
        Block start = player.getLocation().getBlock().getRelative(clip.getOffset().getBlockX(), clip.getOffset().getBlockY(), clip.getOffset().getBlockZ());
        
        block = start;
        xmax = clip.getWidth() - 1;
        ymax = clip.getHeight() - 1;
        zmax = clip.getLength() - 1;
        
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
        
        buildState = BuildState.FINISHED;
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
        // do nothing
    }

    @Override
    public boolean isProtected(Block b)
    {
        return false;
    }
    
    @Override
    public boolean changedFinished(Block b)
    {
        if(buildState != BuildState.FINISHED)
            return false;
        
        if (b.getX() >= block.getX() && b.getX() <= block.getX() + xmax)
            if (b.getY() >= block.getY() && b.getY() <= block.getY() + ymax)
                if (b.getZ() >= block.getZ() && b.getZ() <= block.getZ() + zmax)
                    return true;
        
        return false;
    }
}
