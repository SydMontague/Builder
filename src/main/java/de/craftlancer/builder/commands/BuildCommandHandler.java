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
    private static final String HELP_PERMISSION = "builder.command.help";
    private static final String LIST_PERMISSION = "builder.command.list";
    private static final String PREVIEW_PERMISSION = "builder.command.preview";
    private static final String PLACE_PERMISSION = "builder.command.place";
    private static final String UNDO_PERMISSION = "builder.command.undo";
    private static final String PROGRESS_PERMISSION = "builder.command.progress";
    
    public BuildCommandHandler(Plugin plugin)
    {
        super(plugin);
        registerSubCommand("help", new BuildHelpCommand(HELP_PERMISSION, plugin, getCommands()));
        registerSubCommand("list", new BuildListCommand(LIST_PERMISSION, plugin));
        registerSubCommand("preview", new BuildPreviewCommand(PREVIEW_PERMISSION, plugin));
        registerSubCommand("place", new BuildPlaceCommand(PLACE_PERMISSION, plugin));
        registerSubCommand("undo", new BuildUndoCommand(UNDO_PERMISSION, plugin));
        registerSubCommand("progress", new BuildProgressCommand(PROGRESS_PERMISSION, plugin));
    }
    
}
