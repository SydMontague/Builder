package de.craftlancer.builder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
 *      facing <FACING> # help value
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
    
    private File buildingFile;
    private FileConfiguration buildingConfig;
    
    private File processFile;
    private FileConfiguration processConfig;
    
    private Map<String, Building> buildings = new HashMap<String, Building>();
    private Map<Integer, BuildingProcess> processes = new HashMap<Integer, BuildingProcess>();
    
    private int buildingIndex = 1;
    
    private boolean useCurrencyHandler = false;
    private Economy vault;
    
    // TODO cleaner economy implementation?
    @Override
    public void onEnable()
    {
        instance = this;
        
        if (getServer().getPluginManager().getPlugin("CurrencyHandler") != null)
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
        saveProcesses(true);
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
        ConfigurationSerialization.registerClass(ProcedualBuildingProcess.class);
        getServer().getPluginManager().registerEvents(new BuildListener(this), this);
        
        buildingFile = new File(getDataFolder(), "buildings.yml");
        buildingConfig = YamlConfiguration.loadConfiguration(buildingFile);
        loadBuildings();
        
        processFile = new File(getDataFolder(), "processes.yml");
        processConfig = YamlConfiguration.loadConfiguration(processFile);
        loadProcesses();
        
        getCommand("build").setExecutor(new BuildCommandHandler(this));
    }
    
    private void loadBuildings()
    {
        for (String key : buildingConfig.getKeys(false))
        {
            Building build = new Building(this, key, buildingConfig.getConfigurationSection(key));
            buildings.put(key, build);
        }
    }
    
    public void saveProcesses(boolean isShutdown)
    {
        for (String key : processConfig.getKeys(false))
            processConfig.set(key, null);
        
        for (Entry<Integer, BuildingProcess> entry : processes.entrySet())
            if (entry.getValue().getState() == BuildState.BUILDING)
            {
                if (isShutdown)
                    entry.getValue().prepareForShutdown();
                processConfig.set(entry.getKey().toString(), entry.getValue());
            }
        
        try
        {
            processConfig.save(processFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private void loadProcesses()
    {
        int maxId = 0;
        for (String key : processConfig.getKeys(false))
        {
            int id = Integer.parseInt(key);
            ProcedualBuildingProcess process = (ProcedualBuildingProcess) processConfig.get(key);
            
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
    
    public Collection<String> getBuildingNames()
    {
        return buildings.keySet();
    }
    
    public boolean hasBuilding(String name)
    {
        return buildings.containsKey(name);
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
