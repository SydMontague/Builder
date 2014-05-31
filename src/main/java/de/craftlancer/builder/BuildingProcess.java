package de.craftlancer.builder;

import java.util.UUID;

public interface BuildingProcess
{
    public void undo();
    
    public void prepareForShutdown();
    
    public int getBlocksSet();
    
    public UUID getOwner();
    
    public Building getBuilding();
    
    public BuildState getState();
}
