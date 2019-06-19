/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.expcube.tool;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.mycompany.expcube.config.Config;

/**
 *
 * @author sugichan
 */
public final class CubeTool {

    /**
     * ExpCube の生成
     *
     * @param item
     * @param ench
     * @return 
     */
    public static ItemStack ItemToInventory( ItemStack item, int ench ) {
        List<String> lores = new ArrayList();
        String MSG;

        if ( ench>0 ) {
            MSG = "§f[";
            for ( int i=0; i<10; i++) { MSG += ( i<ench ) ? "§a§l■" : "§f§l□" ; }
            MSG += "§f]";
            lores.add( MSG );
            MSG = String.format( "§f[EXP] %4d/%4d", ench*100, Config.MaxExp );
            lores.add( MSG );
        } else {
            lores.add( ReplaceString( (Player)null, Config.ZeroCubeMsg.get( 1 ) ) );
            lores.add( ReplaceString( (Player)null, Config.ZeroCubeMsg.get( 2 ) ) );
            lores.add( ReplaceString( (Player)null, Config.ZeroCubeMsg.get( 3 ) ) );
        }

        ItemMeta im = item.getItemMeta();   //ItemStackから、ItemMetaを取得します。
        im.setDisplayName( "§aExpCube" );  //Item名を設定
        im.setLore( lores );                //loreを設定します。
        im.addItemFlags( ItemFlag.HIDE_ENCHANTS );
        item.setItemMeta(im);               //元のItemStackに、変更したItemMetaを設定します。

        item.addUnsafeEnchantment( Enchantment.PROTECTION_ENVIRONMENTAL, ench );

        return item;
    }

    /**
     * Configで設定された定型文言を差し替える
     *
     * @param p
     * @param Msg
     * @param nums
     * @return 
     */
    public static String ReplaceString( Player p, String Msg, int ... nums ) {
        if ( "".equals( Msg ) ) { return Config.ConfigErrorMsg; }

        if ( p != null ) {
            Msg = Msg.replace( "%player%", p.getName() );
            Msg = Msg.replace( "%TotalExp%", String.valueOf( ExpCalc.getNowTotalExp( p ) ) );
        }
        if ( nums.length>0 ) {
            Msg = Msg.replace( "%minExp%", String.valueOf( nums[0] ) );
            Msg = Msg.replace( "%maxExp%", String.valueOf( nums[1] ) );
        }
        Msg = Msg.replace( "%$", "§" );
        return Msg;
    }
    
}
