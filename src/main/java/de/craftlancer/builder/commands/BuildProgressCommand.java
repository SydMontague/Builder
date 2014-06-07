package de.craftlancer.builder.commands;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.craftlancer.builder.Builder;
import de.craftlancer.builder.BuildingProcess;
import de.craftlancer.core.command.SubCommand;

public class BuildProgressCommand extends SubCommand
{
    
    public BuildProgressCommand(String permission, Plugin plugin)
    {
        super(permission, plugin, false);
    }
    
    @Override
    protected String execute(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!checkSender(sender))
            return "You don't have the permission for this command!"; // TODO externalise
            
        // Index | Name | [==========] 0% 0/6666
        // 1 | Inn | [==========] 10% 667/6666
        
        for (Entry<Integer, BuildingProcess> entry : getPlugin().getProcesses().entrySet())
        {
            BuildingProcess process = entry.getValue();
            
            if (!process.getOwner().equals(((Player) sender).getUniqueId()))
                continue;
            
            int blockSet = process.getBlocksSet();
            int blockTotal = process.getBuilding().getVolume();
            double ratio = (double) blockSet / blockTotal;
            
            int i = 0;
            int BARS = 15;
            
            StringBuilder message = new StringBuilder();
            message.append(entry.getKey()).append(" | ").append(process.getBuilding().getName()).append(" [").append(ChatColor.GREEN);
            for (; i < ratio * BARS; i++)
                message.append("=");
            message.append(ChatColor.WHITE);
            for (; i < BARS; i++)
                message.append("=");
            message.append("] ").append((int) (ratio * 100)).append("% ").append(blockSet).append("/").append(blockTotal);
            
            sender.sendMessage(message.toString());
        }
        
        return null;
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
