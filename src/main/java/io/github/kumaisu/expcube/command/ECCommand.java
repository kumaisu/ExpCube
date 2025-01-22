/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package io.github.kumaisu.expcube.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import io.github.kumaisu.expcube.ExpCube;
import io.github.kumaisu.expcube.config.Config;
import io.github.kumaisu.expcube.tool.ExpCalc;
import io.github.kumaisu.expcube.tool.CubeTool;
import io.github.kumaisu.expcube.Lib.Tools;
import static io.github.kumaisu.expcube.config.Config.programCode;

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

        boolean hasCubegetPerm = (player == null || player.hasPermission("ExpCube.cubeget"));
        boolean hasConsolePerm = (player == null || player.hasPermission("ExpCube.console"));
        boolean hasAdminPerm = (player == null || player.hasPermission("ExpCube.admin"));

        if ( args.length > 0 ) {

            if ( args[0].equals( "cubegive" ) ) {
                if ( player == null ) {
                    if ( args.length > 1 ) {
                        Tools.Prt("Exp Cube Give for " + args[1], programCode );
                        return ( GiveExpCube( args[1] ) );
                    } else {
                        Tools.Prt( player, CubeTool.ReplaceString( player, Config.SpecifyPlayer ), programCode );
                        return false;
                    }
                } else {
                    Tools.Prt( player, CubeTool.ReplaceString( player, Config.OutsideErrorMsg ), programCode );
                    return false;
                }

            }
            if ( args[0].equals( "cubeget" ) ) {
                if ( player == null ) {
                    Tools.Prt( CubeTool.ReplaceString( player, Config.InsideErrorMsg ), programCode );
                    return false;
                } else {
                    if ( hasCubegetPerm ) {
                        Tools.Prt( player, ChatColor.AQUA + "[ExpCube] ExpCube 1 Get !!", Tools.consoleMode.max, programCode );
                        ItemStack is = new ItemStack( Material.QUARTZ_BLOCK, 1 );
                        player.getInventory().addItem( CubeTool.ItemToInventory( is, 0 ) );
                        return true;
                    } else {
                        Tools.Prt( player, CubeTool.ReplaceString( player, Config.noPermission ), programCode );
                    }
                }
            }

            if ( hasAdminPerm ) {
                switch ( args[0] ) {
                    case "status":
                        Config.PrintStatus( player );
                        return true;
                    case "messages":
                        Config.PrintMessages( player );
                        return true;
                    case "playerstatus":
                    case "ps":
                        ExpCalc.PlayerStatus( player );
                        return true;
                    default:
                }
            } else {
                Tools.Prt( player, CubeTool.ReplaceString( player, Config.noPermission ), programCode );
            }

            if ( hasConsolePerm ) {
                switch ( args[0] ) {
                    case "Reload":
                        instance.config = new Config( instance );
                        Tools.Prt( player, ChatColor.GREEN + "ExpCube Config Reloaded.", Tools.consoleMode.print, programCode );
                        return true;
                    case "Mode":
                        Config.OrbMode = !Config.OrbMode;
                        Tools.Prt( player, ChatColor.GREEN + "Change Mode to " + ( Config.OrbMode ? "ExpOrb":"Direct" ), Tools.consoleMode.print, programCode );
                        return true;
                    case "Console":
                        if ( args.length>1 ) {
                            if ( !Tools.setDebug( args[1], programCode ) ) {
                                Tools.entryDebugFlag( programCode, Tools.consoleMode.normal );
                                Tools.Prt( ChatColor.RED + "The Config Debug mode setting is invalid, so it has been set to normal.", programCode );
                            }
                        } else Tools.Prt( player, "usage: ExpCube console [max,full,normal,stop]", Tools.consoleMode.print, programCode );
                        Tools.Prt( player,
                            ChatColor.GREEN + "System Debug Mode is [ " +
                            ChatColor.RED + Tools.consoleFlag.get( programCode ).toString() +
                            ChatColor.GREEN + " ]",
                            Tools.consoleMode.print, programCode
                        );
                        return true;
                    default:
                }
            } else {
                Tools.Prt( player, CubeTool.ReplaceString( player, Config.noPermission ), programCode );
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
            Tools.Prt( player, "ExpCube Console [max,full,normal,stop]", programCode );
        }

        return false;
    }

    /**
     * PlayerにExpCubeを渡す
     *
     * @param targetPlayerName
     * @return
     */
    private boolean GiveExpCube( String targetPlayerName ) {
        Player givePlayer = Bukkit.getServer().getPlayer( targetPlayerName );
        boolean retStat;

        if ( givePlayer == null ) {
            Tools.Prt( "Player does not exist", programCode );
            retStat = false;
        } else {
            Tools.Prt( givePlayer, ChatColor.AQUA + "[ExpCube] One ExpCube has been given !!", Tools.consoleMode.max, programCode );
            ItemStack is = new ItemStack( Material.QUARTZ_BLOCK, 1 );
            givePlayer.getInventory().addItem( CubeTool.ItemToInventory( is, 0 ) );
            retStat = true;
        }
        return retStat;
    }

}
