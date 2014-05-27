package de.craftlancer.builder;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
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
    
    private boolean useCurrencyHandler = false;
    private Economy vault;
    
    //TODO cleaner economy implementation?
    @Override
    public void onEnable()
    {
        instance = this;
        
        if (this.getServer().getPluginManager().getPlugin("CurrencyHandler") != null)
            useCurrencyHandler = true;
        else
        {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null)
                vault = economyProvider.getProvider();
            else
            {
                getLogger().severe("Neither Vault nor CurrencyHandler have been found, but this plugin requires either one of these!");
                getLogger().severe("You can find CurrencyHandler here: http://dev.bukkit.org/bukkit-plugins/currencyhandler/");
                getLogger().severe("You can find Vault here: http://dev.bukkit.org/bukkit-plugins/vault/");
                throw new RuntimeException("Could not find dependencies!");
            }
        }
        
        loadManager();
        
    }
    
    @Override
    public void onDisable()
    {
        
    }
    
    public Economy getVault()
    {
        return vault;
    }
    
    public boolean useCurrencyHandler()
    {
        return useCurrencyHandler;
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
