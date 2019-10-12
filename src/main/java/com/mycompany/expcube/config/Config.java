/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.expcube.config;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.mycompany.kumaisulibraries.Tools;
import static com.mycompany.kumaisulibraries.Tools.consoleMode;

/**
 * 設定をまとめて取り扱う構造体
 * @author Kumaisu
 */
public class Config {

    public static String programCode = "EC";

    private final Plugin plugin;
    private FileConfiguration config = null;

    public static int MiniExp = 100;
    public static int MaxExp = 1000;
    
    public static boolean OnRecipe;
    public static boolean OrbMode;
    public static String ExpToCube;
    public static String ExpFromCube;
    public static String NoEnough;
    public static String CubeFull;
    public static String CubeEmpty;
    public static String Sneaking;

    public static String NoPermission;
    public static String ConfigErrorMsg;
    public static String InsideErrorMsg;
    public static String InventoryFullMsg;
    public static List<String> ZeroCubeMsg = new ArrayList<>();

    public Config( Plugin plugin ) {
        this.plugin = plugin;
        Tools.entryDebugFlag( programCode, consoleMode.print );
        Tools.Prt( "Config Loading now...", programCode );
        load();
    }
    
    /**
     * 設定をロードします
     *
     */
    public void load() {
        // 設定ファイルを保存
        plugin.saveDefaultConfig();
        if ( config != null ) { // configが非null == リロードで呼び出された
            Tools.Prt( "Config Reloading now...", programCode );
            plugin.reloadConfig();
        }
        config = plugin.getConfig();

        OnRecipe    = config.getBoolean( "CubeRecipe" );
        OrbMode     = config.getBoolean( "OrbMode" );
        ExpToCube   = config.getString( "messages.ExpToCube" );
        ExpFromCube = config.getString( "messages.ExpFromCube" );
        NoEnough    = config.getString( "messages.NoEnough" );
        CubeFull    = config.getString( "messages.CubeFull" );
        CubeEmpty   = config.getString( "messages.CubeEmpty" );
        Sneaking    = config.getString( "messages.Sneaking" );

        NoPermission        = config.getString( "messages.NoPermission" );
        ConfigErrorMsg      = config.getString( "messages.EC_ERROR" );
        InsideErrorMsg      = config.getString( "messages.insideErr" );
        InventoryFullMsg    = config.getString( "messages.InvFull" );

        ZeroCubeMsg = new ArrayList<>( Arrays.asList(
            config.getString( "messages.ZeroCube1" ),
            config.getString( "messages.ZeroCube2" ),
            config.getString( "messages.ZeroCube3" )
        ) );
        
        consoleMode DebugFlag;
        try {
            DebugFlag = consoleMode.valueOf( config.getString( "Debug" ) );
        } catch( IllegalArgumentException e ) {
            Tools.Prt( ChatColor.RED + "Config Debugモードの指定値が不正なので、normal設定にしました", programCode );
            DebugFlag = consoleMode.normal;
        }
        Tools.entryDebugFlag( programCode, DebugFlag );

        config.options().header("Comment1\nComment2");
    }
    
    public static void PrintStatus( Player player ) {
        Tools.Prt( player, ChatColor.GREEN + "=== ExpCube Config Status ===", programCode );
        Tools.Prt( player, ChatColor.WHITE + "Degub Mode : " + ChatColor.YELLOW + Tools.consoleFlag.get( programCode ).toString(), programCode );
        Tools.Prt( player, ChatColor.WHITE + "Recipe Mode : " + ChatColor.YELLOW + ( OnRecipe ? "true":"false" ), programCode );
        Tools.Prt( player, ChatColor.WHITE + "ExpOrb Mode : " + ChatColor.YELLOW + ( OrbMode ? "ExpOrg":"Direct"), programCode );
        if ( player != null ) {
            Tools.Prt( player, ChatColor.WHITE + "ExpSet: " + ChatColor.YELLOW + ( player.hasPermission( "ExpCube.set" ) ? "true":"false" ), programCode );
            Tools.Prt( player, ChatColor.WHITE + "ExpGet: " + ChatColor.YELLOW + ( player.hasPermission( "ExpCube.get" ) ? "true":"false" ), programCode );
        }
        Tools.Prt( player, ChatColor.WHITE + "ExpToCube : " + ChatColor.YELLOW + ExpToCube, programCode );
        Tools.Prt( player, ChatColor.WHITE + "ExpfmCube : " + ChatColor.YELLOW + ExpFromCube, programCode );
        Tools.Prt( player, ChatColor.WHITE + "ExpEnough : " + ChatColor.YELLOW + NoEnough, programCode );
        Tools.Prt( player, ChatColor.WHITE + "Exp Empty : " + ChatColor.YELLOW + CubeEmpty, programCode );
        Tools.Prt( player, ChatColor.WHITE + "Exp Full  : " + ChatColor.YELLOW + CubeFull, programCode );
        Tools.Prt( player, ChatColor.WHITE + "UseExpCube: " + ChatColor.YELLOW + Sneaking, programCode );
        Tools.Prt( player, ChatColor.WHITE + "ZeroMsg(1): " + ChatColor.YELLOW + ZeroCubeMsg.get( 0 ), programCode );
        Tools.Prt( player, ChatColor.WHITE + "ZeroMsg(2): " + ChatColor.YELLOW + ZeroCubeMsg.get( 1 ), programCode );
        Tools.Prt( player, ChatColor.WHITE + "ZeroMsg(3): " + ChatColor.YELLOW + ZeroCubeMsg.get( 2 ), programCode );
        Tools.Prt( player, ChatColor.GREEN + "=============================", programCode );
    }
}
