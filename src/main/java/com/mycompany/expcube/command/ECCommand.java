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

        if ( args.length > 0 ) {
            switch ( args[0] ) {
                case "reload":
                    instance.config = new Config( instance );
                    Tools.Prt( player, ChatColor.GREEN + "ExpCube Config Reloaded.", Tools.consoleMode.max, programCode );
                    return true;
                case "status":
                    Config.PrintStatus( player );
                    return true;
                case "mode":
                    Config.OrbMode = !Config.OrbMode;
                    Tools.Prt( player, ChatColor.GREEN + "Change Mode to " + ( Config.OrbMode ? "ExpOrb":"Direct" ), Tools.consoleMode.max, programCode );
                    return true;
                case "playerstatus":
                case "ps":
                    ExpCalc.PlayerStatus( player );
                    return true;
                case "console":
                    if ( args.length>1 ) {
                        Tools.setDebug( args[1], programCode );
                    } else Tools.Prt( player, "usage: ExpCube console [full/normal/none]", Tools.consoleMode.max, programCode );
                    Tools.Prt( player,
                        ChatColor.GREEN + "System Debug Mode is [ " +
                        ChatColor.RED + Tools.consoleFlag.get( programCode ).toString() +
                        ChatColor.GREEN + " ]",
                        ( ( player == null ) ? Tools.consoleMode.none:Tools.consoleMode.max ), programCode
                    );
                    return true;
                case "cubeget":
                    if ( player == null ) {
                        Tools.Prt( CubeTool.ReplaceString( player, Config.InsideErrorMsg ), programCode );
                        return false;
                    } else {
                        Tools.Prt( player, ChatColor.AQUA + "[ExpCube] ExpCube 1 Get !!", Tools.consoleMode.max, programCode );
                        ItemStack is = new ItemStack( Material.QUARTZ_BLOCK, 1 );
                        player.getInventory().addItem( CubeTool.ItemToInventory( is, 0 ) );
                    }
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }
}
