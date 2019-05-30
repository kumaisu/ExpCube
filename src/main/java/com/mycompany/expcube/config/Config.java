/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.expcube.config;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import com.mycompany.kumaisulibraries.Utility;

/**
 * 設定をまとめて取り扱う構造体
 * @author Kumaisu
 */
public class Config {
    
    private final Plugin plugin;
    private FileConfiguration config = null;
    
    private boolean OnRecipe;
    private boolean OrbMode;
    private String ExpToCube;
    private String ExpFromCube;
    private String NoEnough;
    private String CubeFull;
    private String CubeEmpty;
    private String Sneaking;
    private Utility.consoleMode DebugFlag;
    private long CoolTick;
    private int CoolCount;
    
    public Config( Plugin plugin ) {
        this.plugin = plugin;
        Bukkit.getServer().getConsoleSender().sendMessage( "Config Loading now..." );
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
       
        try {
            DebugFlag = Utility.consoleMode.valueOf( config.getString( "Debug" ) );
        } catch( IllegalArgumentException e ) {
            Utility.Prt( null, ChatColor.RED + "Config Debugモードの指定値が不正なので、normal設定にしました", true );
            DebugFlag = Utility.consoleMode.normal;
        }

        config.options().header("Comment1\nComment2");
    }
    
    public void PrintStatus( CommandSender sender ) {
        sender.sendMessage( ChatColor.AQUA + "=== ExpCube Config Status ===" );
        sender.sendMessage( ChatColor.AQUA + "Degub Mode : " + ChatColor.WHITE + DebugFlag.toString() );
        sender.sendMessage( ChatColor.AQUA + "Recipe Mode : " + ChatColor.WHITE + ( OnRecipe ? "true":"false" ) );
        sender.sendMessage( ChatColor.AQUA + "ExpOrb Mode : " + ChatColor.WHITE + ( OrbMode ? "ExpOrg":"Direct") );
        sender.sendMessage( ChatColor.AQUA + "ExpSet: " + ChatColor.WHITE + ( sender.hasPermission( "ExpCube.set" ) ? "true":"false" ) );
        sender.sendMessage( ChatColor.AQUA + "ExpGet: " + ChatColor.WHITE + ( sender.hasPermission( "ExpCube.get" ) ? "true":"false" ) );
        sender.sendMessage( ChatColor.AQUA + "ExpToCube : " + ChatColor.WHITE + ExpToCube );
        sender.sendMessage( ChatColor.AQUA + "ExpfmCube : " + ChatColor.WHITE + ExpFromCube );
        sender.sendMessage( ChatColor.AQUA + "ExpEnough : " + ChatColor.WHITE + NoEnough );
        sender.sendMessage( ChatColor.AQUA + "Exp Empty : " + ChatColor.WHITE + CubeEmpty );
        sender.sendMessage( ChatColor.AQUA + "Exp Full  : " + ChatColor.WHITE + CubeFull );
        sender.sendMessage( ChatColor.AQUA + "UseExpCube: " + ChatColor.WHITE + Sneaking );
    }

    /**
     * 一時的にDebugModeを設定しなおす
     * ただし、Config.ymlには反映しない
     *
     * @param key 
     */
    public void setDebug( String key ) {
        try {
            DebugFlag = Utility.consoleMode.valueOf( key );
        } catch( IllegalArgumentException e ) {
            DebugFlag = Utility.consoleMode.none;
        }
    }

    /**
     * keyに対して、設定されているDebugMode下での可否判定を返す
     *
     * @param key
     * @return 
     */
    public boolean isDebugFlag( Utility.consoleMode key ) {
        return ( DebugFlag.ordinal() >= key.ordinal() );
    }

    public String getNoPermission() {
        return config.getString( "messages.NoPermission" );
    }
    
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
    
    public String ConfigErrorMsg() {
        return config.getString( "messages.EC_ERROR" );
    }
    
    public String InsideErrorMsg() {
        return config.getString( "messages.insideErr" );
    }
    
    public String InventoryFullMsg() {
        return config.getString( "messages.InvFull" );
    }

    public boolean getRecipe() {
        return OnRecipe;
    }
    
    public void setOrbMode( boolean flag ) {
        OrbMode = flag;
    }
    
    public boolean getOrbMode() {
        return OrbMode;
    }
    
    /**
     * DebugMode を数値で受け取る
     *
     * @return 
     */
    public Utility.consoleMode getDebug() {
        return DebugFlag;
    }


    public String getExpToCube() {
        return ExpToCube;
    }
    
    public String getExpFromCube() {
        return ExpFromCube;
    }
    
    public String getNoEnough() {
        return NoEnough;
    }
    
    public String getCubeFull() {
        return CubeFull;
    }
    
    public String getCubeEmpty() {
        return CubeEmpty;
    }

    public String getSneaking() {
        return Sneaking;
    }
    
    //  5tick(0.25秒)ごとにTimerクラスのrunメソッドを実行してね
    //  Timer 5tick×2回 = 0.5秒です
    public long CoolTick() {
        return CoolTick;
    }
    
    public int CoolCount() {
        return CoolCount;
    }
}
