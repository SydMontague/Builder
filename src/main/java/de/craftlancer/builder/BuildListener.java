package de.craftlancer.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BuildListener implements Listener
{
    private Builder plugin;
    
    public BuildListener(Builder plugin)
    {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e)
    {
        handleBlockEvent(e);
        List<Integer> remove = new ArrayList<Integer>();
        for (Entry<Integer, BuildingProcess> entry : plugin.getProcesses().entrySet())
            if (entry.getValue().changedFinished(e.getBlock()))
                remove.add(entry.getKey());
        
        for (int i : remove)
            plugin.getProcesses().remove(i);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockPlaceEvent e)
    {
        handleBlockEvent(e);
    }
    
    public <T extends BlockEvent & Cancellable> void handleBlockEvent(T event)
    {
        for (BuildingProcess pro : plugin.getProcesses().values())
            if (pro.isProtected(event.getBlock()))
                event.setCancelled(true);
    }
}
