/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.expcube.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.mycompany.expcube.ExpCube;
import com.mycompany.expcube.config.Config;
import com.mycompany.expcube.tool.ExpCalc;
import com.mycompany.expcube.tool.CubeTool;
import com.mycompany.kumaisulibraries.Tools;
import static com.mycompany.expcube.config.Config.programCode;

/**
 *
 * @author sugichan
 */
public class ECCommand implements CommandExecutor {

     private final ExpCube instance;

     public ECCommand( ExpCube instance ) {
         this.instance = instance;
     }

    /**
     * コマンド入力があった場合に発生するイベント
     *
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args
     * @return 
     */
    @Override
    public boolean onCommand( CommandSender sender,Command cmd, String commandLabel, String[] args ) {
        Player player = ( ( sender instanceof Player ) ? (Player)sender:(Player)null );

        Tools.Prt( "ExpCube Command", Tools.consoleMode.max, programCode );

        if ( args.length > 0 ) {

            if ( args[0].equals( "cubeget" ) ) {
                if ( player == null ) {
                    Tools.Prt( CubeTool.ReplaceString( player, Config.InsideErrorMsg ), programCode );
                    return false;
                } else {
                    if ( player.hasPermission( "ExpCube.cubeget" ) ) {
                        Tools.Prt( player, ChatColor.AQUA + "[ExpCube] ExpCube 1 Get !!", Tools.consoleMode.max, programCode );
                        ItemStack is = new ItemStack( Material.QUARTZ_BLOCK, 1 );
                        player.getInventory().addItem( CubeTool.ItemToInventory( is, 0 ) );
                        return true;
                    } else {
                        Tools.Prt( player, "You do not have permission.", programCode );
                    }
                }
            }

            if ( player == null ) {
                if ( player.hasPermission( "ExpCube.admin" ) ) {
                    switch ( args[0] ) {
                        case "status":
                            Config.PrintStatus( player );
                            return true;
                        case "playerstatus":
                        case "ps":
                            ExpCalc.PlayerStatus( player );
                            return true;
                        default:
                    }
                } else {
                    Tools.Prt( player, "You do not have permission.", programCode );
                }
            }

            if ( player == null ) {
                if ( player.hasPermission( "ExpCube.console" ) ) {
                    switch ( args[0] ) {
                        case "Reload":
                            instance.config = new Config( instance );
                            Tools.Prt( player, ChatColor.GREEN + "ExpCube Config Reloaded.", Tools.consoleMode.none, programCode );
                            return true;
                        case "Mode":
                            Config.OrbMode = !Config.OrbMode;
                            Tools.Prt( player, ChatColor.GREEN + "Change Mode to " + ( Config.OrbMode ? "ExpOrb":"Direct" ), Tools.consoleMode.none, programCode );
                            return true;
                        case "Console":
                            if ( args.length>1 ) {
                                Tools.setDebug( args[1], programCode );
                            } else Tools.Prt( player, "usage: ExpCube console [max,full,normal,none]", Tools.consoleMode.none, programCode );
                            Tools.Prt( player,
                                ChatColor.GREEN + "System Debug Mode is [ " +
                                ChatColor.RED + Tools.consoleFlag.get( programCode ).toString() +
                                ChatColor.GREEN + " ]",
                                Tools.consoleMode.none, programCode
                            );
                            return true;
                        default:
                    }
                } else {
                    Tools.Prt( player, "You do not have permission.", programCode );
                }
            }
        }

        if ( ( player == null ) || player.hasPermission( "ExpCube.cubeget" ) ) {
            Tools.Prt( player, "ExpCube cubeget", programCode );
        }
        if ( ( player == null ) || player.hasPermission( "ExpCube.admin" ) ) {
            Tools.Prt( player, "ExpCube status", programCode );
            Tools.Prt( player, "ExpCube playerstatus", programCode );
        }
        if ( ( player == null ) || player.hasPermission( "ExpCube.console" ) ) {
            Tools.Prt( player, "ExpCube Reload", programCode );
            Tools.Prt( player, "ExpCube Mode", programCode );
            Tools.Prt( player, "ExpCube Console [max,full,normal,none]", programCode );
        }

        return false;
    }
}
