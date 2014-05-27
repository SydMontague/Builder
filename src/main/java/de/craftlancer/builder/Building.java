package de.craftlancer.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import com.sk89q.worldedit.Countable;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

import de.craftlancer.core.MassChestInventory;
import de.craftlancer.currencyhandler.CurrencyHandler;

/*
 *  Building:
 *      schematic: <FILE> # relative to #getPluginFolder()/schematics/
 *      costs: <int> (use Vault)
 *          <CurrencyHandler Map> (use CurrencyHandler)
 *      build-type: <INSTANT/PROCEDUAL>
 *      <IF build-type == PROCEDUAL>
 *      ticks-per-run: <INT>
 *      blocks-per-run: <INT>
 *      add-progress-sign: <BOOLEAN>
 *      require-blocks: <BOOLEAN>
 *      <ENDIF>
 *      checkSpace: <BOOLEAN>
 *      alias: <STRINGLIST> # for <building>
 *      description: <TEXT>
 *      facing <FACING> # help value //TODO remove help value
 */
public class Building
{
    private Builder plugin; //
    private final String name; //
    private final List<String> alias; //
    private File file; //
    private String description; //
    
    private Map<String, Object> costs;
    
    private BuildType buildtype;
    private boolean requiresBlocks; //
    private boolean addProgressSign; //
    private boolean checkSpace; //
    private int ticksPerRun;
    private int blockPerRun;
    
    private final BlockFace baseFacing;     // TODO remove
    private final int numBlocks; //
    private final int width; //
    private final int height; //
    private final int lenght; //
    
    private CuboidClipboard r0Clip;
    private CuboidClipboard r90Clip;
    private CuboidClipboard r180Clip;
    private CuboidClipboard r270Clip;
    
    @SuppressWarnings("deprecation")
    public Building(Builder plugin, String key, FileConfiguration config)
    {
        this.plugin = plugin;
        this.name = key;
        this.alias = config.getStringList("alias");
        this.alias.add(key);
        this.description = config.getString("description");
        this.file = new File(plugin.getDataFolder(), "schematics" + File.separator + file);
        this.setCheckSpace(config.getBoolean("checkSpace", false));
        this.addProgressSign = config.getBoolean("addProgressSign", false);
        this.requiresBlocks = config.getBoolean("requiresBlocks", false);
        
        CuboidClipboard clip = getClipboard();
        int blocks = 0;
        for (Countable<Integer> i : clip.getBlockDistribution())
        {
            if (i.getID() == Material.AIR.getId())
                continue;
            
            blocks += i.getAmount();
        }
        this.numBlocks = blocks;
        
        this.baseFacing = BlockFace.NORTH; // TODO remove
        this.width = clip.getWidth();
        this.height = clip.getHeight();
        this.lenght = clip.getLength();
    }
    
    @SuppressWarnings("deprecation")
    public void createPreview(Player player, Block initialBlock, long ticks)
    {
        if (player == null || initialBlock == null || ticks <= 0)
            throw new IllegalArgumentException(player + " " + initialBlock + " " + ticks);
        
        CuboidClipboard schematic = getClipboard();
        
        int facing = Math.abs((Math.round((player.getLocation().getYaw()) / 90)) % 4);
        
        switch (baseFacing)
        {
            case NORTH:
                facing += 2;
                break;
            case EAST:
                facing += 3;
                break;
            case SOUTH:
                facing += 0;
                break;
            case WEST:
                facing += 1;
                break;
            default:
        }
        schematic.rotate2D(facing * 90);
        
        initialBlock = initialBlock.getRelative(schematic.getOffset().getBlockX(), 0, schematic.getOffset().getBlockZ());
        int xmax = schematic.getWidth();
        int ymax = schematic.getHeight();
        int zmax = schematic.getLength();
        
        for (int x = 0; x < xmax; x++)
            for (int y = 0; y < ymax; y++)
                for (int z = 0; z < zmax; z++)
                {
                    BaseBlock b = schematic.getBlock(new Vector(x, y, z));
                    player.sendBlockChange(initialBlock.getRelative(x, y, z).getLocation(), b.getType(), (byte) b.getData());
                }
        
        new PreviewRemoveTask(player, initialBlock, xmax, ymax, zmax).runTaskLater(plugin, ticks);
    }
    
    public void createPreview(Player player)
    {
        createPreview(player, 100L);
    }
    
    public void createPreview(Player player, long tick)
    {
        createPreview(player, player.getLocation().getBlock(), tick);
    }
    
    public void startBuilding(Player player)
    {
        switch (buildtype)
        {
            case INSTANT:
            case PROCEDUAL:
            {
                int facing = Math.abs((Math.round((player.getLocation().getYaw()) / 90)) % 4);
                
                int xFacing = 0;
                int zFacing = 0;
                BlockFace signFacing = null;
                switch (facing)
                {
                    case 0: // SOUTH
                        xFacing = -1;
                        zFacing = 0;
                        signFacing = BlockFace.NORTH;
                        break;
                    case 1: // WEST
                        xFacing = 0;
                        zFacing = -1;
                        signFacing = BlockFace.EAST;
                        break;
                    case 2: // NORTH
                        xFacing = 1;
                        zFacing = 0;
                        signFacing = BlockFace.SOUTH;
                        break;
                    case 3: // EAST
                        xFacing = 0;
                        zFacing = 1;
                        signFacing = BlockFace.WEST;
                        break;
                }
                
                MassChestInventory inventory = null;
                
                Block block = player.getLocation().getBlock().getRelative(xFacing, 0, zFacing);
                block.setType(Material.CHEST);
                Block block2 = player.getLocation().getBlock().getRelative(xFacing * 2, 0, zFacing * 2);
                block2.setType(Material.CHEST);
                
                // TODO we don't need a MassChestInventory when we only have a
                // double chest
                if (isRequiresBlocks())
                    inventory = new MassChestInventory(getName(), getName(), ((Chest) block.getState()).getInventory(), ((Chest) block2.getState()).getInventory());
                
                Sign s = null;
                if (isAddProgressSign())
                {
                    Block sign = player.getLocation().getBlock().getRelative(-xFacing, 0, -zFacing);
                    sign.setType(Material.SIGN_POST);
                    
                    s = (Sign) sign.getState();
                    MaterialData data = s.getData();
                    ((org.bukkit.material.Sign) data).setFacingDirection(signFacing);
                    s.setData(data);
                    s.update();
                }
                
                BuildingProcess process = new BuildingProcess(this, player, inventory, s);
                process.runTaskTimer(getPlugin(), getTicksPerRun(), getTicksPerRun());
                getPlugin().addProcess(process);
                break;
            }
        }
    }
    
    public boolean checkSpace(Player player)
    {
        if (!isCheckSpace())
            return true;
        
        List<Material> ignoredMaterial = new ArrayList<Material>();
        
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean checkCosts(Player player)
    {
        if (getPlugin().useCurrencyHandler())
            return CurrencyHandler.hasCurrencies(player, getCosts());
        else if (getPlugin().getVault() != null && getCosts().containsKey("money"))
            return getPlugin().getVault().has(player, (Integer) getCosts().get("money")); // TOTEST check if this type conversion works properly
            
        return getCosts().isEmpty();
    }
    
    // getter and setter part
    public CuboidClipboard getClipboard()
    {
        try
        {
            return SchematicFormat.MCEDIT.load(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (DataException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public Builder getPlugin()
    {
        return plugin;
    }
    
    public String getName()
    {
        return name;
    }
    
    public boolean addAlias(String string)
    {
        return this.alias.add(string);
    }
    
    public boolean removeAlias(String string)
    {
        return this.alias.remove(string);
    }
    
    public File getFile()
    {
        return file;
    }
    
    public void setFile(File file)
    {
        this.file = file;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public Map<String, Object> getCosts()
    {
        return costs;
    }
    
    public void setCosts(Map<String, Object> costs)
    {
        this.costs = costs;
    }
    
    public BuildType getBuildtype()
    {
        return buildtype;
    }
    
    public void setBuildtype(BuildType buildtype)
    {
        this.buildtype = buildtype;
    }
    
    public boolean isRequiresBlocks()
    {
        return requiresBlocks;
    }
    
    public void setRequiresBlocks(boolean requiresBlocks)
    {
        this.requiresBlocks = requiresBlocks;
    }
    
    public boolean isAddProgressSign()
    {
        return addProgressSign;
    }
    
    public void setAddProgressSign(boolean addProgressSign)
    {
        this.addProgressSign = addProgressSign;
    }
    
    public int getTicksPerRun()
    {
        return ticksPerRun;
    }
    
    public void setTicksPerRun(int ticksPerRun)
    {
        this.ticksPerRun = ticksPerRun;
    }
    
    public int getBlockPerRun()
    {
        return blockPerRun;
    }
    
    public void setBlockPerRun(int blockPerRun)
    {
        this.blockPerRun = blockPerRun;
    }
    
    public BlockFace getFacing()
    {
        return baseFacing;
    }
    
    public int getNumBlocks()
    {
        return numBlocks;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public int getHeight()
    {
        return height;
    }
    
    public int getLenght()
    {
        return lenght;
    }
    
    public boolean isCheckSpace()
    {
        return checkSpace;
    }
    
    public void setCheckSpace(boolean checkSpace)
    {
        this.checkSpace = checkSpace;
    }
    
    public String getSizeString()
    {
        return width + "x" + height + "x" + lenght;
    }
}
