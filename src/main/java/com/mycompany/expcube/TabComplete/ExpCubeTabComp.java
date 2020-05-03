/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.expcube.TabComplete;

import java.util.List;
import java.util.ArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 *  Copyright (c) 2019 sugichan. All rights reserved.
 *
 * @author sugichan
 */
public class ExpCubeTabComp implements TabCompleter {

    /**
     * Admin Command TAB Complete list
     *
     * @param sender
     * @param command
     * @param alias
     * @param args
     * @return 
     */
    @Override
    public List<String> onTabComplete( CommandSender sender, Command command, String alias, String[] args ) {
        Player player = ( ( sender instanceof Player ) ? (Player)sender:(Player)null );

        List< String > list = new ArrayList<>();
        switch ( args.length ) {
            case 1:
                if ( ( player == null ) || ( player.hasPermission( "ExpCube.cubeget" ) ) ) {
                    list.add( "cubeget" );
                }
                if ( ( player == null ) || ( player.hasPermission( "ExpCube.admin" ) ) ) {
                    list.add( "status" );
                    list.add( "messages" );
                    list.add( "playerstatus" );
                    list.add( "ps" );
                }
                if ( ( player == null ) || ( player.hasPermission( "ExpCube.console" ) ) ) {
                    list.add( "Reload" );
                    list.add( "Mode" );
                    list.add( "Console" );
                }
                break;
            case 2:
                if ( args[0].equals( "Console" ) ) {
                    list.add( "full" );
                    list.add( "max" );
                    list.add( "normal" );
                    list.add( "stop" );
                }
                break;
        }
        return list;
    }
}
