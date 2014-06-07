package de.craftlancer.builder.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.craftlancer.builder.Builder;
import de.craftlancer.builder.BuildingProcess;
import de.craftlancer.core.command.SubCommand;

public class BuildUndoCommand extends SubCommand
{
    
    public BuildUndoCommand(String permission, Plugin plugin)
    {
        super(permission, plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
            
        if (args.length < 2)
            return "You need to specify an index!";
        
        int index = 0;
        
        try
        {
            index = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            return "You need to specify an index!";// TODO externalise
        }
        
        BuildingProcess process = getPlugin().getProcesses().get(index);
        
        if (process == null)
            return "There is no process with the specified index!";
        
        if (!((Player) sender).getUniqueId().equals(process.getOwner()))
            return "This isn't your building process!";
        
        process.undo();
        
        return "Undoed building process!"; // TODO externalise
    }
    
    @Override
    public void help(CommandSender sender)
    {
        // TODO help output
        
    }
    
    @Override
    public Builder getPlugin()
    {
        return (Builder) super.getPlugin();
    }
}
