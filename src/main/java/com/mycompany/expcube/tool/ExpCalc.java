/*
 *  Copyright (c) 2019 sugichan. All rights reserved.
 */
package com.mycompany.expcube.tool;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.kumaisulibraries.Tools;
import static com.mycompany.expcube.config.Config.programCode;
import static com.mycompany.kumaisulibraries.Tools.consoleMode;

/**
 *
 * @author sugichan
 */
public final class ExpCalc {

    /**
     * プレイヤーEXPの計算ルーチン
     *
     * @param p
     * @param TotalExp 
     */
    public static void setNewExp( Player p, int TotalExp ) {
        Tools.Prt( "setNewExp", consoleMode.full, programCode );
        p.setLevel( 0 );
        p.setExp( 0 );

        for( ;TotalExp > p.getExpToLevel(); )
        {
            Tools.Prt(
                Utility.StringBuild(
                    "Level = ", String.valueOf( p.getLevel() ),
                    " RemainExp = ", String.valueOf( TotalExp ),
                    " getExpToLevel = ", String.valueOf( p.getExpToLevel() ),
                    " Calc Exp = ", String.valueOf( getExpNeededToLevelUp( p.getLevel() ) )
                ),
                consoleMode.full,
                programCode
            );

            TotalExp -= p.getExpToLevel();
            p.setLevel( p.getLevel() + 1 );
        }

        float xp = ( float ) TotalExp / ( float ) p.getExpToLevel();
        Tools.Prt(
            Utility.StringBuild(
                "Level = ", String.valueOf( p.getLevel() ),
                " RemainExp = ", String.valueOf( TotalExp ),
                " getExpToLevel = ", String.valueOf( p.getExpToLevel() ),
                " Calc Exp = ", String.valueOf( getExpNeededToLevelUp( p.getLevel() ) ),
                " SetExpBar = ", String.valueOf( xp )
            ),
            consoleMode.full,
            programCode
        );

        p.setExp( xp );
    }

    /**
     * 現在のプレイヤーEXPを取得する
     *
     * @param p
     * @return 
     */
    public static int getNowTotalExp( Player p ) {
        //  Debug( "getNowTotalExp", 2, null );
        int lvl = p.getLevel();
        int exp = convertExpPercentageToExp( lvl, p.getExp() );

        for( ; lvl>0; ) {
            //  現在のレベルになるのに必要な経験値なので、Lv-1として算出
            exp += getExpNeededToLevelUp( lvl - 1 );
            lvl--;
        }

        return exp;
    }

    /**
     * プレイヤーのレベルに応じた経験値パーセンテージから演算する
     *
     * @param xp
     * @param expPercentage
     * @return 
     */
    public static int convertExpPercentageToExp( int xp, float expPercentage ) {
        return ( int ) ( Math.round( getExpNeededToLevelUp( xp ) * expPercentage ) );
    }

    /**
     * 次のレベルアップに必要な経験値の算出
     *
     * @param xp
     * @return 
     */
    public static int getExpNeededToLevelUp( int xp ) {
        /* 旧計算式
        if ( xp >= 30 ) return 62 + ( xp - 30 ) * 7;
        if ( xp >= 15 ) return 17 + ( xp - 15 ) * 3;
        if ( xp > 0 )  return 17;
        return 0;
        */
        //  次のレベルに必要な経験値が知りたいので、現在のLv+1で演算する
        xp++;
        if ( xp >= 32 ) return 121 + ( ( xp - 32 ) * 9 );
        if ( xp >= 17 ) return 42 + ( ( xp - 17 ) * 5 );
        return 5 + ( xp * 2 );
    }

    /**
     * 現状のプレイヤーの経験値情報などのステータス表示
     *
     * @param player
     */
    public static void PlayerStatus( Player player ) {
        if ( player != null ) {
            Tools.Prt( player, ChatColor.AQUA + "Player name " + ChatColor.WHITE + player.getDisplayName(), programCode );
            Tools.Prt( player, ChatColor.AQUA + "Level   : " + ChatColor.WHITE + player.getLevel(), programCode );
            Tools.Prt( player, ChatColor.AQUA + "Exp     : " + ChatColor.WHITE + player.getExp(), programCode );
            Tools.Prt( player, ChatColor.AQUA + "CalcExp : " + ChatColor.WHITE + convertExpPercentageToExp( player.getLevel(), player.getExp() ), programCode );
            Tools.Prt( player, ChatColor.AQUA + "Total   : " + ChatColor.WHITE + getNowTotalExp( player ), programCode );
            Tools.Prt( player, ChatColor.AQUA + "G Total : " + ChatColor.WHITE + player.getTotalExperience(), programCode );
        } else {
            Tools.Prt( player, ChatColor.RED + "Use this Command ONLINE", programCode);
        }
    }
}
