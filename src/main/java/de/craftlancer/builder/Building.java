package de.craftlancer.builder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

/*
 *  Building:
 *      schematic: <FILE> # relative to #getPluginFolder()/schematics/
 *      costs: <int> (use Vault)
 *          <CurrencyHandler Map> (use CurrencyHandler)
 *      build-type: <INSTANT/PROCEDUAL>
 *      <IF build-type == PROCEDUAL>
 *      ticks-per-run: <INT>
 *      blocks-per-run: <INT>
 *      <ENDIF>
 *      require-blocks: <BOOLEAN>
 *      use-inventory: <PLAYER/CHEST>
 *      add-progress-sign: <BOOLEAN>
 *      alias: <STRINGLIST> # for <building>
 *      description: <TEXT>
 *      facing <FACING> # help value //TODO remove help value
 */
public class Building
{
    private Builder plugin;
    private final String name;
    private final List<String> alias;
    private File file;
    private String description;
    
    private Map<String, Object> costs;
    
    private BuildType buildtype;
    private boolean requiresBlocks;
    private boolean addProgressSign;
    private boolean useChest;
    private int ticksPerRun;
    private int blockPerRun;
    
    private final BlockFace baseFacing;     // TODO remove
    private final int numBlocks;
    private final int width;
    private final int height;
    private final int lenght;
    
    public Building()
    {
        // TODO Auto-generated constructor stub
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
    
        public String getName()
    {
        return name;
    }
    
    public boolean addAlias(String alias)
    {
        return this.alias.add(alias);
    }
    
    public boolean removeAlias(String alias)
    {
        return this.alias.remove(alias);
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
    
    public boolean isUseChest()
    {
        return useChest;
    }
    
    public void setUseChest(boolean useChest)
    {
        this.useChest = useChest;
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
}
