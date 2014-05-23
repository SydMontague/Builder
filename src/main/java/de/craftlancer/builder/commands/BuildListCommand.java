package de.craftlancer.builder.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import de.craftlancer.builder.Builder;
import de.craftlancer.builder.Building;
import de.craftlancer.core.command.SubCommand;

public class BuildListCommand extends SubCommand
{
    private static int ITEMS_PER_PAGE = 5; // TODO make configable
    
    public BuildListCommand(String permission, Plugin plugin)
    {
        super(permission, plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
            
        int page = 1;
        
        // parse input to page
        switch (args.length)
        {
            case 0:
            case 1:
                page = 0;
                break;
            case 2:
                try
                {
                    page = Integer.parseInt(args[1]) - 1;
                }
                catch (NumberFormatException e)
                {
                    page = 0;
                }
                break;
            default:
                page = Integer.parseInt(args[1]);
                break;
        }
                
        if (page < 0)
            page = 0;
        
        List<Building> buildings = new ArrayList<Building>(getPlugin().getBuildings());
        
        for (int i = 0; i < ITEMS_PER_PAGE; i++)
        {
            if (buildings.size() <= page * ITEMS_PER_PAGE + i)
                break;
            
            // Name || # Blocks || x*y*z || Feature
            // Blacksmith | Blocks: 55555 | Size: 10x10x10 | Feature: Schmiede
            
            Building build = buildings.get(page * ITEMS_PER_PAGE + i);
            
            String message = build.getName() + " | Blöcke: " + build.getNumBlocks() + " | Größe: " + build.getSizeString(); // TODO externalize
            
            sender.sendMessage(message);
        }
        
        return null;
    }
    
    @Override
    public void help(CommandSender sender)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public Builder getPlugin()
    {
        return (Builder) super.getPlugin();
    }
}
