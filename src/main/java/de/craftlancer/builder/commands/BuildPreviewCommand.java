package de.craftlancer.builder.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.craftlancer.builder.Builder;
import de.craftlancer.builder.Building;
import de.craftlancer.core.command.SubCommand;

public class BuildPreviewCommand extends SubCommand
{
    
    public BuildPreviewCommand(String permission, Plugin plugin)
    {
        super(permission, plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
            
        if (args.length < 2 || !getPlugin().hasBuilding(args[1]))
            return "You need to specify a valid building!"; // TODO externalise
            
        Building build = getPlugin().getBuilding(args[1]);
        build.createPreview((Player) sender);
        
        return "Showing preview for building \"" + build.getName() + "\""; // TODO externalise
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
