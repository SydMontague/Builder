package de.craftlancer.builder;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

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
 *      <IF build-type == PROCEDUAL>
 *      ticks-per-run: <INT>
 *      blocks-per-run: <INT>
 *      <ENDIF>
 *      check-space: <BOOLEAN>
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
    private Map<Integer, BuildingProcess> processes = new HashMap<Integer, BuildingProcess>();

    private int buildingIndex = 1;
    
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
        int maxId = 0;
        for (String key : processConfig.getKeys(false))
        {
            int id = Integer.parseInt(key);
            BuildingProcess process = (BuildingProcess) processConfig.get(key);
            
            processes.put(id, process);
            process.runTaskTimer(this, process.getBuilding().getTicksPerRun(), process.getBuilding().getTicksPerRun());
            if (maxId < id)
                maxId = id;
        }

        buildingIndex = maxId + 1;
    }
    
    public static Builder getInstance()
    {
        return instance;
    }
    
    public Building getBuilding(String string)
    {
        return buildings.get(string);
    }
    
    public Collection<Building> getBuildings()
    {
        return buildings.values();
    }    
    
    public boolean hasBuilding(String name)
    {
        for (String b : buildings.keySet())
            if (b.equalsIgnoreCase(name))
                return true;
        
        return false;
    }

    public Map<Integer, BuildingProcess> getProcesses()
    {
        return processes;
    }

    public void addProcess(BuildingProcess process)
    {
        processes.put(buildingIndex, process);
        buildingIndex++;
    }
}
