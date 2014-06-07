package de.craftlancer.builder;

import java.util.UUID;

import org.bukkit.block.Block;

public interface BuildingProcess
{
    public void undo();
    
    public void prepareForShutdown();
    
    public int getBlocksSet();
    
    public UUID getOwner();
    
    public Building getBuilding();
    
    public BuildState getState();

    public boolean isProtected(Block block);

    public boolean changedFinished(Block block);
}
