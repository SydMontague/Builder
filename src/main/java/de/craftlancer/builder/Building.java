package de.craftlancer.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import com.sk89q.worldedit.Countable;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

import de.craftlancer.core.Direction;
import de.craftlancer.core.MassChestInventory;
import de.craftlancer.core.Utils;
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
 *      facing <FACING>
 */
public class Building
{
    private Builder plugin; //
    private final String name; //
    private File file; //
    private String description; //
    
    private Map<String, Object> costs = new HashMap<String, Object>();
    
    private BuildType buildtype; //
    private boolean requiresBlocks; //
    private boolean addProgressSign; //
    private boolean checkSpace; //
    private int ticksPerRun;
    private int blockPerRun;
    
    private final Direction baseFacing; //
    private final int numBlocks; //
    private final int width; //
    private final int height; //
    private final int lenght; //
    
    private CuboidClipboard r0Clip; //
    private CuboidClipboard r90Clip; //
    private CuboidClipboard r180Clip; //
    private CuboidClipboard r270Clip; //
    
    @SuppressWarnings("deprecation")
    public Building(Builder plugin, String key, ConfigurationSection config)
    {
        this.plugin = plugin;
        this.name = key;
        this.description = config.getString("description");
        this.file = new File(plugin.getDataFolder(), "schematics" + File.separator + config.getString("schematic", "Gasthaus.schematic"));
        this.checkSpace = config.getBoolean("checkSpace", false);
        this.addProgressSign = config.getBoolean("addProgressSign", false);
        this.requiresBlocks = config.getBoolean("requiresBlocks", false);
        
        this.buildtype = BuildType.valueOf(config.getString("buildType", "INSTANT"));
        
        r0Clip = getClipboard();
        
        r90Clip = getClipboard();
        r90Clip.rotate2D(90);
        
        r180Clip = getClipboard();
        r180Clip.rotate2D(180);
        
        r270Clip = getClipboard();
        r270Clip.rotate2D(270);
        
        int blocks = 0;
        for (Countable<Integer> i : r0Clip.getBlockDistribution())
        {
            if (i.getID() == Material.AIR.getId())
                continue;
            
            blocks += i.getAmount();
        }
        this.numBlocks = blocks;
        
        this.baseFacing = Direction.valueOf(config.getString("facing", "SOUTH"));
        this.width = r0Clip.getWidth();
        this.height = r0Clip.getHeight();
        this.lenght = r0Clip.getLength();
    }
    
    @SuppressWarnings("deprecation")
    public void createPreview(Player player, Block initialBlock, long ticks)
    {
        if (player == null || initialBlock == null || ticks <= 0)
            throw new IllegalArgumentException(player + " " + initialBlock + " " + ticks);
        
        CuboidClipboard schematic = getRotatedClipboard(player);
        
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
            {
                InstantBuildingProcess process = new InstantBuildingProcess(this, player);
                getPlugin().addProcess(process);
                break;
            }
            case PROCEDUAL:
            {
                Direction facing = Utils.getPlayerDirection(player);
                
                int xFacing = 0;
                int zFacing = 0;
                BlockFace signFacing = null;
                switch (facing)
                {
                    case SOUTH:
                        xFacing = -1;
                        zFacing = 0;
                        signFacing = BlockFace.NORTH;
                        break;
                    case WEST:
                        xFacing = 0;
                        zFacing = -1;
                        signFacing = BlockFace.EAST;
                        break;
                    case NORTH:
                        xFacing = 1;
                        zFacing = 0;
                        signFacing = BlockFace.SOUTH;
                        break;
                    case EAST:
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
                
                // TODO we don't need a MassChestInventory when we only have a double chest
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
                
                ProcedualBuildingProcess process = new ProcedualBuildingProcess(this, player, inventory, s);
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
        
        CuboidClipboard schematic = getRotatedClipboard(player);
        
        Block initialBlock = player.getLocation().getBlock().getRelative(schematic.getOffset().getBlockX(), 0, schematic.getOffset().getBlockZ());
        int xmax = schematic.getWidth();
        int ymax = schematic.getHeight();
        int zmax = schematic.getLength();
        
        for (int x = 0; x < xmax; x++)
            for (int y = 0; y < ymax; y++)
                for (int z = 0; z < zmax; z++)
                {
                    Material mat = initialBlock.getRelative(x, y, z).getType();
                    if (mat != null && mat != Material.AIR && !ignoredMaterial.contains(mat))
                        return false;
                }
        
        return true;
    }
    
    public boolean checkCosts(Player player)
    {
        if (getPlugin().useCurrencyHandler())
            return CurrencyHandler.hasCurrencies(player, getCosts());
        else if (getPlugin().getVault() != null && getCosts().containsKey("money"))
            return getPlugin().getVault().has(player, ((Number) getCosts().get("money")).doubleValue()); // TOTEST check if this type conversion works properly
            
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
    
    public CuboidClipboard getRotatedClipboard(Player player)
    {
        return getRotatedClipboard(Utils.getPlayerDirection(player));
    }
    
    public CuboidClipboard getRotatedClipboard(Direction facing)
    {
        int base;
        
        switch (baseFacing)
        {
            case NORTH:
                base = 2;
                break;
            case EAST:
                base = 3;
                break;
            case SOUTH:
                base = 0;
                break;
            case WEST:
                base = 1;
                break;
            default:
                base = 0;
        }
        
        switch ((facing.getIntValue() - base + 4) % 4)
        {
            case 0:
                return r0Clip;
            case 1:
                return r90Clip;
            case 2:
                return r180Clip;
            case 3:
                return r270Clip;
            default:
                return r0Clip;
        }
    }
    
    public Builder getPlugin()
    {
        return plugin;
    }
    
    public String getName()
    {
        return name;
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
    
    public Direction getFacing()
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
