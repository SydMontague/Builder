package de.craftlancer.builder.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.SubCommand;

public class BuildPreviewCommand extends SubCommand
{
    
    public BuildPreviewCommand(String permission, Plugin plugin)
    {
        super(permission, plugin, false);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void help(CommandSender sender)
    {
        // TODO Auto-generated method stub
        
    }
    
}
