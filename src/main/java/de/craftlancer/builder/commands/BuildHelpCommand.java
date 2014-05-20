package de.craftlancer.builder.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.SubCommand;

public class BuildHelpCommand extends SubCommand
{
    
    public BuildHelpCommand(String permission, Plugin plugin, boolean console)
    {
        super(permission, plugin, console);
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
