package de.craftlancer.builder.commands;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.HelpCommand;
import de.craftlancer.core.command.SubCommand;

public class BuildHelpCommand extends HelpCommand
{
    
    public BuildHelpCommand(String permission, Plugin plugin, Map<String, SubCommand> map)
    {
        super(permission, plugin, map);
    }
    
    @Override
    public void help(CommandSender sender)
    {
        // TODO help output
        
    }
    
}
