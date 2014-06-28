package de.craftlancer.builder.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.craftlancer.builder.Builder;
import de.craftlancer.builder.Building;
import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.currencyhandler.CurrencyHandler;

public class BuildPlaceCommand extends SubCommand
{
    
    public BuildPlaceCommand(String permission, Plugin plugin)
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
        
        Player player = (Player) sender;
        
        if (build.isCheckSpace() && !build.checkSpace(player))
            return "Not enough space!";
        
        if (!build.checkCosts(player))
            return "Not enough money!";
        
        if (!build.checkProtection(player))
            return "You can't build here!";
        
        CurrencyHandler.withdrawCurrencies(player, build.getCosts());
        build.startBuilding(player);
        
        return "Building started!";
    }
    
    public List<String> onTabComplete(CommandSender sender, String[] args)
    {
        switch (args.length)
        {
            case 2:
                return Utils.getMatches(args[1], getPlugin().getBuildingNames());
            default:
                return null;
        }
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
