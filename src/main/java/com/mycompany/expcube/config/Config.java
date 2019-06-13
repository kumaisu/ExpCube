/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.expcube.config;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
    
    public static boolean OnRecipe;
    public static boolean OrbMode;
    public static String ExpToCube;
    public static String ExpFromCube;
    public static String NoEnough;
    public static String CubeFull;
    public static String CubeEmpty;
    public static String Sneaking;
    public static long CoolTick;
    public static int CoolCount;

    public Config( Plugin plugin ) {
        this.plugin = plugin;
        Tools.entryDebugFlag( programCode, consoleMode.none );
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

        //  5tick(0.25秒)ごとにTimerクラスのrunメソッドを実行してね
        //  Timer 5tick×2回 = 0.5秒です
        CoolTick    = config.getLong( "CoolTick" );
        CoolCount   = config.getInt( "CoolCount" );
       
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
    
    public void PrintStatus( Player player ) {
        consoleMode consolePrintFlag = ( ( player == null ) ? consoleMode.none:consoleMode.max );
        Tools.Prt( player, ChatColor.AQUA + "=== ExpCube Config Status ===", consolePrintFlag, programCode );
        Tools.Prt( player, ChatColor.AQUA + "Degub Mode : " + ChatColor.WHITE + Tools.consoleFlag.get( programCode ).toString(), consolePrintFlag, programCode );
        Tools.Prt( player, ChatColor.AQUA + "Recipe Mode : " + ChatColor.WHITE + ( OnRecipe ? "true":"false" ), consolePrintFlag, programCode );
        Tools.Prt( player, ChatColor.AQUA + "ExpOrb Mode : " + ChatColor.WHITE + ( OrbMode ? "ExpOrg":"Direct"), consolePrintFlag, programCode );
        if ( player != null ) {
            Tools.Prt( player, ChatColor.AQUA + "ExpSet: " + ChatColor.WHITE + ( player.hasPermission( "ExpCube.set" ) ? "true":"false" ), consolePrintFlag, programCode );
            Tools.Prt( player, ChatColor.AQUA + "ExpGet: " + ChatColor.WHITE + ( player.hasPermission( "ExpCube.get" ) ? "true":"false" ), consolePrintFlag, programCode );
        }
        Tools.Prt( player, ChatColor.AQUA + "ExpToCube : " + ChatColor.WHITE + ExpToCube, consolePrintFlag, programCode );
        Tools.Prt( player, ChatColor.AQUA + "ExpfmCube : " + ChatColor.WHITE + ExpFromCube, consolePrintFlag, programCode );
        Tools.Prt( player, ChatColor.AQUA + "ExpEnough : " + ChatColor.WHITE + NoEnough, consolePrintFlag, programCode );
        Tools.Prt( player, ChatColor.AQUA + "Exp Empty : " + ChatColor.WHITE + CubeEmpty, consolePrintFlag, programCode );
        Tools.Prt( player, ChatColor.AQUA + "Exp Full  : " + ChatColor.WHITE + CubeFull, consolePrintFlag, programCode );
        Tools.Prt( player, ChatColor.AQUA + "UseExpCube: " + ChatColor.WHITE + Sneaking, consolePrintFlag, programCode );
    }

    /**
     * 
     *
     * @param line
     * @return 
     */
    public String ZeroCubeMsg( int line ) {
        switch ( line ) {
            case 1:
                return config.getString( "messages.ZeroCube1" );
            case 2:
                return config.getString( "messages.ZeroCube2" );
            case 3:
                return config.getString( "messages.ZeroCube3" );
            default:
                return config.getString( "messages.EC_ERROR" );
        }
    }
    
    /**
     * パーミッションが設定されていない時のメッセージ
     * NoPermission: "%$4貴方には権限がありません"
     *
     * @return 
     */
    public String getNoPermission() {
        return config.getString( "messages.NoPermission" );
    }
    
    /**
     * コンフィグの指定が間違った場合のメッセージ
     * EC_ERROR: "%$4コンフィグの指定に誤りがあります"
     *
     * @return 
     */
    public String ConfigErrorMsg() {
        return config.getString( "messages.EC_ERROR" );
    }

    /**
     * コンソール操作不可なものへのメッセージ
     * insideErr: "%$4このコマンドはゲーム内から実行してください"
     *
     * @return 
     */
    public String InsideErrorMsg() {
        return config.getString( "messages.insideErr" );
    }
    
    /**
     * プレイヤーインベントリがmaxの時のメッセージ
     * InvFull: "%$a[ExpCube] %$4インベントリに空きがありません"
     *
     * @return 
     */
    public String InventoryFullMsg() {
        return config.getString( "messages.InvFull" );
    }
}
