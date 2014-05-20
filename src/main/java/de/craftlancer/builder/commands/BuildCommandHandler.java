package de.craftlancer.builder.commands;

import org.bukkit.plugin.Plugin;

import de.craftlancer.core.command.CommandHandler;

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
 */
public class BuildCommandHandler extends CommandHandler
{
    private static final String HELP_PERMISSION = "";
    private static final String LIST_PERMISSION = "";
    private static final String PREVIEW_PERMISSION = "";
    private static final String PLACE_PERMISSION = "";
    private static final String UNDO_PERMISSION = "";
    private static final String PROGRESS_PERMISSION = "";
    
    public BuildCommandHandler(Plugin plugin)
    {   
        super(plugin);
        this.registerSubCommand("help", new BuildHelpCommand(HELP_PERMISSION, plugin));
        this.registerSubCommand("list", new BuildListCommand(LIST_PERMISSION, plugin));
        this.registerSubCommand("preview", new BuildPreviewCommand(PREVIEW_PERMISSION, plugin));
        this.registerSubCommand("place", new BuildPlaceCommand(PLACE_PERMISSION, plugin));
        this.registerSubCommand("undo", new BuildUndoCommand(UNDO_PERMISSION, plugin));
        this.registerSubCommand("progress", new BuildProgressCommand(PROGRESS_PERMISSION, plugin));
    }
    
}
