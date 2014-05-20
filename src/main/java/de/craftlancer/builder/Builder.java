package de.craftlancer.builder;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import de.craftlancer.builder.commands.BuildCommandHandler;

/*
 * Commands:
 *  /build list
 *  /build help [command]
 *  /build preview <building>
 *  /build place <building>
 *  /build undo
 *  /build progress
 *  
 *  AdminCommands? (create, set, remove)
 *  
 *  Building:
 *      schematic: <FILE> # relative to #getPluginFolder()/schematics/
 *      costs: <int> (use Vault)
 *          <CurrencyHandler Map> (use CurrencyHandler)
 *      build-type: <INSTANT/PROCEDUAL>
 *      require-blocks: <BOOLEAN>
 *      use-inventory: <PLAYER/CHEST>
 *      add-progress-sign: <BOOLEAN>
 *      alias: <STRINGLIST> # for <building>
 *      description: <TEXT>
 *      facing <FACING> # help value //TODO remove help value
 *      
 *  Events:
 *      BuildingStartEvent
 *      BuildingProgressEvent?
 *      BuildingFinishEvent
 *      
 *  CLCore Modules?:
 *      CraftItYourself
 *      MassChestInventory
 *      CommandHandler
 *      
 */
public class Builder extends JavaPlugin implements Listener
{
    private static Builder instance;
    
    private File configFile;
    private FileConfiguration config;
    
    private File processFile;
    private FileConfiguration processConfig;
    
    private Map<String, Building> buildings = new HashMap<String, Building>();
    private List<BuildingProcess> processes = new LinkedList<BuildingProcess>();
    
    @Override
    public void onEnable()
    {
        instance = this;
        loadManager();
    }
    
    @Override
    public void onDisable()
    {
        
    }
    
    private void loadManager()
    {
        ConfigurationSerialization.registerClass(BuildingProcess.class);
        getServer().getPluginManager().registerEvents(this, this);
        
        configFile = new File(getDataFolder(), "buildings.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        loadBuildings();
        
        processFile = new File(getDataFolder(), "processes.yml");
        processConfig = YamlConfiguration.loadConfiguration(processFile);
        loadProcesses();
        
        getCommand("building").setExecutor(new BuildCommandHandler(this));
    }
    
    private void loadBuildings()
    {
        for (String key : config.getKeys(false))
        {
            // TODO
        }
    }
    
    private void loadProcesses()
    {
        for (String key : processConfig.getKeys(false))
        {
            BuildingProcess process = (BuildingProcess) processConfig.get(key);
            
            processes.add(process);
            process.runTaskTimer(this, process.getBuilding().getTicksPerRun(), process.getBuilding().getTicksPerRun());
        }
    }
    
    public static Builder getInstance()
    {
        return instance;
    }
    
    public Building getBuilding(String string)
    {
        return buildings.get(string);
    }
}
