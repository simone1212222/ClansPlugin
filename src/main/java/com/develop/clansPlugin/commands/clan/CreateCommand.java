package com.develop.clansPlugin.commands.clan;

import com.develop.clansPlugin.ClansPlugin;
import com.develop.clansPlugin.commands.base.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CreateCommand extends BaseCommand {

    public CreateCommand(ClansPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean executeCommand(CommandSender sender, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabCompleteCommand(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
