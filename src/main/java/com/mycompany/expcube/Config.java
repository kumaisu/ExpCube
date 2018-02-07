/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.expcube;

import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 設定をまとめて取り扱う構造体
 * @author sugichan
 */
public class Config {
    
    private final Plugin plugin;
    private FileConfiguration config = null;
    
    private boolean OnRecipe;
    private boolean OrbMode;
    private int DebugFlag;
    private String ExpToCube;
    private String ExpFromCube;
    private String NoEnough;
    private String CubeFull;
    private String CubeEmpty;
    private String Sneaking;
    
    public Config(Plugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info( "Config Loading now..." );
        load();
    }
    
    /*
     * 設定をロードします
     */
    public void load() {
        // 設定ファイルを保存
        plugin.saveDefaultConfig();
        if (config != null) { // configが非null == リロードで呼び出された
            plugin.reloadConfig();
        }
        config = plugin.getConfig();

        /*
        if (!config.contains("Message")) { // 存在チェック
            plugin.getLogger().info("config.ymlがなんか変です！！");
        } else if (!config.isString("Message")) {
            plugin.getLogger().info("config.ymlのMessageがStringじゃないです！！");
        }
        */

        OnRecipe = config.getBoolean( "CubeRecipe" );
        OrbMode = config.getBoolean( "OrbMode" );
        ExpToCube = config.getString( "messages.ExpToCube" );
        ExpFromCube = config.getString( "messages.ExpFromCube" );
        NoEnough = config.getString( "messages.NoEnough" );
        CubeFull = config.getString( "messages.CubeFull" );
        CubeEmpty = config.getString( "messages.CubeEmpty" );
        Sneaking = config.getString( "messages.Sneaking" );
        
        switch ( config.getString( "Debug" ) ) {
        case "full":
            DebugFlag = 2;
            break;
        case "normal":
            DebugFlag = 1;
            break;
        case "none":
            DebugFlag = 0;
            break;
        default:
            DebugFlag = 0;
        }
        
        config.options().header("Comment1\nComment2");
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
    
    public int getDebug() {
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
    
    /*
    public Map<String, String> getMapKeys() {
        return mapKeys;
    }
    */

}
