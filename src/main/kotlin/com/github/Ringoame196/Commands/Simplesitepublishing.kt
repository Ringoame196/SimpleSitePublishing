package com.github.Ringoame196.Commands

import com.github.Ringoame196.WebServer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.plugin.Plugin

class Simplesitepublishing(private val plugin: Plugin) : CommandExecutor, TabExecutor {
    private val webServer = WebServer(plugin)
    private val subCommandMap = mapOf<String, (sender: CommandSender) -> Unit>(
        "start" to { sender: CommandSender -> webServer.start(sender) },
        "stop" to { sender: CommandSender -> webServer.stop(sender) },
        "check" to { sender: CommandSender -> webServer.check(sender) }
    )
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val subCommand = args[0]
        return if (subCommandMap.keys.contains(subCommand)) {
            subCommandMap[subCommand]?.invoke(sender)
            true
        } else {
            false
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        return when (args.size) {
            1 -> subCommandMap.keys.toMutableList()
            else -> mutableListOf()
        }
    }
}
