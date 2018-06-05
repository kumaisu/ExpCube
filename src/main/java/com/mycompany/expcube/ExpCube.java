/*
 *  Copyright (c) 2018 Kumaisu. All rights reserved.
 */
package com.mycompany.expcube;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Kumaisu
 */
public class ExpCube extends JavaPlugin implements Listener {

    BukkitTask task = null; //  あとで自分を止めるためのもの
    private Config config;
    private boolean ClickFlag = false;
    final private int MiniExp = 100;
    final private int MaxExp = 1000;

    private class Timer extends BukkitRunnable{
        int time;//秒数
        JavaPlugin plugin;//BukkitのAPIにアクセスするためのJavaPlugin
        
        public Timer(JavaPlugin plugin ,int i) {
            this.time = i;
            this.plugin = plugin;
        }
        
        @Override
        public void run() {
            if( time <= 0 ){
                //タイムアップなら
                ClickFlag = false;
                plugin.getServer().getScheduler().cancelTask( task.getTaskId() ); //自分自身を止める
            }
            /*
            else{
                plugin.getServer().broadcastMessage("" + time);//残り秒数を全員に表示
            }
            */
            time--; //  1秒減算
        }
    }

    public String StringBuild( String ... StrItem ) {
        StringBuilder buf = new StringBuilder();

        for ( String StrItem1 : StrItem ) buf.append( StrItem1 );
 
        return buf.toString();
    }
    
    // 0:none 1:normal 2:full
    public void Debug( String msg, int lvl, Player player ) {
        Boolean prtf;
        StringBuilder buf = new StringBuilder();
        if ( player != null ) {
            buf.append( player.getDisplayName() );
            buf.append( " " );
        }
        switch ( config.getDebug() ) {
            case 0:
                prtf = ( lvl == 0 );
                break;
            case 1:
                buf.append( ChatColor.YELLOW );
                buf.append( "(DB:n) " );
                prtf = ( lvl == 1 );
                break;
            case 2:
                buf.append( ChatColor.YELLOW );
                buf.append( "(DB:f) " );
                prtf = true;
                break;
            default:
                prtf = false;
        }
        buf.append( ChatColor.WHITE );
        buf.append( msg );
        if ( prtf ) {
            Bukkit.getServer().getConsoleSender().sendMessage( buf.toString() );
        }
    }

    public void PrintStatus( CommandSender sender ) {
        String StsMsg;
        sender.sendMessage( ChatColor.AQUA + "=== ExpCube Config Status ===" );
        switch ( config.getDebug() ) {
            case 0:
                StsMsg = "none";
                break;
            case 1:
                StsMsg = "normal";
                break;
            case 2:
                StsMsg = "full";
                break;
            default:
                StsMsg = ChatColor.RED + "Error";
        }
        
        sender.sendMessage( ChatColor.AQUA + "Degub Mode : " + ChatColor.WHITE + StsMsg );
        sender.sendMessage( ChatColor.AQUA + "Recipe Mode : " + ChatColor.WHITE + ( config.getRecipe() ? "true":"false" ) );
        sender.sendMessage( ChatColor.AQUA + "ExpOrb Mode : " + ChatColor.WHITE + ( config.getOrbMode() ? "ExpOrg":"Direct") );
        sender.sendMessage( ChatColor.AQUA + "ExpSet: " + ChatColor.WHITE + ( sender.hasPermission( "ExpCube.set" ) ? "true":"false" ) );
        sender.sendMessage( ChatColor.AQUA + "ExpGet: " + ChatColor.WHITE + ( sender.hasPermission( "ExpCube.get" ) ? "true":"false" ) );
        sender.sendMessage( ChatColor.AQUA + "ExpToCube : " + ChatColor.WHITE + config.getExpToCube() );
        sender.sendMessage( ChatColor.AQUA + "ExpfmCube : " + ChatColor.WHITE + config.getExpFromCube() );
        sender.sendMessage( ChatColor.AQUA + "ExpEnough : " + ChatColor.WHITE + config.getNoEnough() );
        sender.sendMessage( ChatColor.AQUA + "Exp Empty : " + ChatColor.WHITE + config.getCubeEmpty() );
        sender.sendMessage( ChatColor.AQUA + "Exp Full  : " + ChatColor.WHITE + config.getCubeFull() );
        sender.sendMessage( ChatColor.AQUA + "UseExpCube: " + ChatColor.WHITE + config.getSneaking() );
    }

    public ItemStack ItemToInventory( ItemStack item, int ench ) {
        List<String> lores = new ArrayList();
        String MSG;
        
        if ( ench>0 ) {
            MSG = "§f[";
            for ( int i=0; i<10; i++) { MSG += ( i<ench ) ? "§a§l■" : "§f§l□" ; }
            MSG += "§f]";
            lores.add( MSG );
            MSG = String.format( "§f[EXP] %4d/%4d", ench*100, MaxExp );
            lores.add( MSG );
        } else {
            lores.add( ReplaceString( (Player)null, config.ZeroCubeMsg( 1 ) ) );
            lores.add( ReplaceString( (Player)null, config.ZeroCubeMsg( 2 ) ) );
            lores.add( ReplaceString( (Player)null, config.ZeroCubeMsg( 3 ) ) );
        }
        
        ItemMeta im = item.getItemMeta();   //ItemStackから、ItemMetaを取得します。
        im.setDisplayName( "§aExpCube" );  //Item名を設定
        im.setLore( lores );                //loreを設定します。
        im.addItemFlags( ItemFlag.HIDE_ENCHANTS );
        item.setItemMeta(im);               //元のItemStackに、変更したItemMetaを設定します。

        item.addUnsafeEnchantment( Enchantment.PROTECTION_ENVIRONMENTAL, ench );
                    
        return item;
    }

    public void setNewLevel( Player p ) {
        p.setLevel( 0 );
        p.setExp( 0 );
        int TotalExp = p.getTotalExperience();
        for( ;TotalExp > p.getExpToLevel(); )
        {
            //  Debug( "Level = " + p.getLevel() + " Total = " + TotalExp + " Calc Exp = " + ( p.getLevel() >= 15 ? 37 + ( p.getLevel() - 15 ) * 5 : 7 + p.getLevel() * 2 ) + " getExpToLevel = " + p.getExpToLevel(), 2 );
            Debug(
                StringBuild(
                    "Level = ", String.valueOf( p.getLevel() ), 
                    " Total = ", String.valueOf( TotalExp ),
                    " Calc Exp = ", String.valueOf( ( p.getLevel() >= 15 ? 37 + ( p.getLevel() - 15 ) * 5 : 7 + p.getLevel() * 2 ) ),
                    " getExpToLevel = ", String.valueOf( p.getExpToLevel() )
                ), 2, null
            );

            TotalExp -= p.getExpToLevel();
            p.setLevel( p.getLevel() + 1 );
        }
        float xp = ( float ) TotalExp / ( float ) p.getExpToLevel();
        //  Debug( "Level = " + p.getLevel() + " Total = " + TotalExp + " Calc Exp = " + ( p.getLevel() >= 15 ? 37 + ( p.getLevel() - 15 ) * 5 : 7 + p.getLevel() * 2 ) + " getExpToLevel = " + p.getExpToLevel() + " SetExpBar = " + xp, 2 );
        Debug(
            StringBuild(
                "Level = ", String.valueOf( p.getLevel() ),
                " Total = ", String.valueOf( TotalExp ),
                " Calc Exp = ", String.valueOf( ( p.getLevel() >= 15 ? 37 + ( p.getLevel() - 15 ) * 5 : 7 + p.getLevel() * 2 ) ),
                " getExpToLevel = ", String.valueOf( p.getExpToLevel() ),
                " SetExpBar = ", String.valueOf( xp )
            ), 2, null
        );

        p.setExp( xp );
    }

    public String ReplaceString( Player p, String Msg, int ... nums ) {
        if ( "".equals( Msg ) ) { return config.ConfigErrorMsg(); }
        
        if ( p != null ) {
            Msg = Msg.replace( "%player%", p.getName() );
            Msg = Msg.replace( "%TotalExp%", String.valueOf( p.getTotalExperience() ) );
        }
        if ( nums.length>0 ) {
            Msg = Msg.replace( "%minExp%", String.valueOf( nums[0] ) );
            Msg = Msg.replace( "%maxExp%", String.valueOf( nums[1] ) );
        }
        Msg = Msg.replace( "%$", "§" );
        return Msg;
    }
    
    @Override
    public void onEnable() {
        config = new Config( this );
        getServer().getPluginManager().registerEvents( this, this );
        
        if ( config.getRecipe() ) {
            this.getLogger().info( "ExpCube Recipe Enable." );
            ItemStack item = new ItemStack(Material.QUARTZ_BLOCK, 1);
            item = ItemToInventory( item, 0 );
            ShapedRecipe CubeRecipe = new ShapedRecipe( item );
            CubeRecipe.shape( "lql", "qrq" ,"lql" );
            CubeRecipe.setIngredient( 'r', Material.REDSTONE_BLOCK );
            CubeRecipe.setIngredient( 'l', Material.LAPIS_BLOCK );
            CubeRecipe.setIngredient( 'q', Material.QUARTZ_BLOCK );
            getServer().addRecipe( CubeRecipe );
        } else {
            this.getLogger().info( "ExpCube Recipe Disable." );
        }
    }

    @EventHandler
    public void Click( PlayerInteractEvent event ) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemStack item2 = new ItemStack( item ); // 複数スタックの退避用

        if ( item.getType() == Material.QUARTZ_BLOCK ) {
            if ( item.getItemMeta().hasDisplayName() ) {
                if ( item.getItemMeta().getDisplayName().equalsIgnoreCase( "§aExpCube" ) ) {
                    if ( player.isSneaking() ) {
                        event.setCancelled( true );

                        if ( ( item.getAmount() > 1 ) && ( !Arrays.asList( player.getInventory().getStorageContents()).contains( null ) ) ) {
                            player.sendMessage( ReplaceString( player, config.InventoryFullMsg() ) );
                            return;
                        }

                        //  CoolTimer
                        //  ClickFlag が true ならクリックキャンセルだけして終わり
                        //  false なら、true にしてタイマー起動後、処理
                        if ( ClickFlag ) { return; }
                        ClickFlag = true;
                        task = this.getServer().getScheduler().runTaskTimer( this, new Timer( this ,config.CoolCount() ), 0L, config.CoolTick() );

                        player.getInventory().setItemInMainHand( null );

                        int ench = item.getItemMeta().getEnchantLevel( Enchantment.PROTECTION_ENVIRONMENTAL );

                        if ( config.getDebug() == 2 ) {
                            //  player.sendMessage( ChatColor.GREEN + "[ExpCube]" + ChatColor.YELLOW + " Now your Experience is " + player.getTotalExperience() + "." );
                            player.sendMessage( StringBuild( ChatColor.GREEN.toString(), "[ExpCube]", ChatColor.YELLOW.toString(), " Now your Experience is ", String.valueOf( player.getTotalExperience() ), "." ) );
                        }
                        //  Debug( player.getName() + " Befor Ex" + player.getExp() + ":Lv" + player.getLevel() + " > " + player.getTotalExperience(), 2 );
                        Debug( StringBuild( "Befor Ex", String.valueOf( player.getExp() ), ":Lv", String.valueOf( player.getLevel() ), " > ", String.valueOf( player.getTotalExperience() ) ), 2, player );

                        // if( action.equals( Action.RIGHT_CLICK_AIR ) || action.equals( Action.RIGHT_CLICK_BLOCK ) ) {
                        if( action.equals( Action.RIGHT_CLICK_AIR ) ) {
                            if ( player.hasPermission( "ExpCube.set" ) ) {
                                Debug( StringBuild( "Cube State = ", String.valueOf( ench ) ), 2, null );
                                if ( ench<10 ) {
                                    Debug( StringBuild( "Player Experience = ", String.valueOf( player.getTotalExperience() ) ), 2, null );
                                    if ( !( MiniExp>player.getTotalExperience() ) ) {
                                        ench++;

                                        int OldExp = player.getTotalExperience();
                                        player.setTotalExperience( player.getTotalExperience() - MiniExp );
                                        player.sendMessage( ReplaceString( player, config.getExpToCube(), ench*MiniExp, MaxExp ) );

                                        //  Debug( player.getName() + " Right Click Cube(" + ench + ") Exp(" + OldExp + " => " + player.getTotalExperience() + ")", 1 );
                                        Debug( StringBuild( "Right Click Cube(", String.valueOf( ench ), ") Exp(", String.valueOf( OldExp ), " => ", String.valueOf( player.getTotalExperience() ), ")" ), 1, player );

                                        setNewLevel( player );

                                    } else {
                                        player.sendMessage( ReplaceString( player, config.getNoEnough() ) );
                                        Debug( ReplaceString( player, config.getNoEnough() ), 1, player );
                                    }
                                } else {
                                    player.sendMessage( ReplaceString( player, config.getCubeFull() ) );
                                    Debug( ReplaceString( player, config.getCubeFull() ), 1, player );
                                }
                            } else {
                                player.sendMessage( ReplaceString( player, config.getNoPermission() ) );
                            }
                        }

                        //  if( action.equals( Action.LEFT_CLICK_AIR ) || ( action.equals( Action.LEFT_CLICK_BLOCK ) ) ) {
                        if( action.equals( Action.LEFT_CLICK_AIR ) ) {
                            if ( player.hasPermission( "ExpCube.get" ) ) {
                                if ( ench>0 ) {
                                    ench--;

                                    player.sendMessage( ReplaceString( player, config.getExpFromCube(), ench*MiniExp, MaxExp ) );
                                    //  Debug( player.getName() + " Left Click Cube(" + ench + ") Exp(" + player.getTotalExperience() + ")", 1 );
                                    Debug( StringBuild( "Left Click Cube(", String.valueOf( ench ), ") Exp(", String.valueOf( player.getTotalExperience() ), ")" ), 1, player );

                                    if ( config.getOrbMode() ) {
                                        Debug( "OrbMode", 2, null );
                                        //  従来は直接EXPに反映していたが、"修繕"への影響を加味し
                                        //  経験値100のExpOrbをドロップする形式
                                        Location loc = player.getLocation();
                                        ExperienceOrb exp = (ExperienceOrb) loc.getBlock().getWorld().spawn( loc.getBlock().getLocation().add( 0, 0, 0 ), ExperienceOrb.class );
                                        exp.setExperience( MiniExp );
                                    } else {
                                        Debug( "None OrbMode", 2, null );
                                        //  従来のEｘｐ直接反映方式
                                        //  オフハンドに修繕アイテムがある場合の処理を追加して対応
                                        int BackExp = MiniExp;

                                        //  オフハンドに修繕するアイテムがあるかチェックするとこ
                                        Debug( "Get off Hand",2, null );

                                        if ( player.getInventory().getItemInOffHand().getType() != Material.AIR ) {
                                            ItemStack offHand = player.getInventory().getItemInOffHand();
                                            Debug( StringBuild( "OffHand = ", offHand.getItemMeta().getDisplayName() ), 2, null );
                                            if ( offHand.getItemMeta().getEnchantLevel( Enchantment.MENDING ) > 0 ) {
                                                short dmg = offHand.getDurability();
                                                if ( dmg>0 ) {
                                                    if ( dmg>BackExp ) {
                                                        dmg -= BackExp;
                                                        BackExp = 0;
                                                    } else {
                                                        BackExp -= dmg;
                                                        dmg = 0;
                                                    }
                                                    //  Debug( player.getName() + ChatColor.AQUA + " Repair Item (" + dmg + ") to Exp(" + BackExp + ")", 1 );
                                                    Debug( StringBuild( ChatColor.AQUA.toString(), " Repair Item (", String.valueOf( dmg ), ") to Exp(", String.valueOf( BackExp ), ")" ), 1, player );
                                                    offHand.setDurability( (short) dmg );
                                                }
                                            }
                                        }
                                        Debug( "Set Exp " + BackExp, 2, null );
                                        player.setTotalExperience( player.getTotalExperience() + BackExp );

                                        setNewLevel( player );
                                    }

                                } else {
                                    player.sendMessage( ReplaceString( player, config.getCubeEmpty() ) );
                                    Debug( ReplaceString( player, config.getCubeEmpty() ), 1, player );
                                }
                            } else {
                                player.sendMessage( ReplaceString( player, config.getNoPermission() ) );
                            }
                        }
                        if ( config.getDebug() == 2 ) {
                            //  player.sendMessage( ChatColor.GREEN + "[ExpCube]" + ChatColor.YELLOW + " Now your Experience is " + player.getTotalExperience() + "." );
                            player.sendMessage( StringBuild( ChatColor.GREEN.toString(), "[ExpCube]", ChatColor.YELLOW.toString(), " Now your Experience is ", String.valueOf( player.getTotalExperience() ), "." ) );
                        }
                        //  Debug( player.getName() + " After Ex" + player.getExp() + ":Lv" + player.getLevel() + " > " + player.getTotalExperience(), 2 );
                        Debug( StringBuild( "After Ex", String.valueOf( player.getExp() ), ":Lv", String.valueOf( player.getLevel() ), " > ", String.valueOf( player.getTotalExperience() ) ), 2, player );

                        int amount = item.getAmount();
                        Debug( StringBuild( "Have Amount is [", String.valueOf( amount ), "]" ), 2, player );
                        item.setAmount( 1 );    // １スタックに強制設定
                        player.getInventory().setItemInMainHand( ItemToInventory( item, ench ) );

                        if ( amount>1 ) {
                            item2.setAmount( --amount );               // スタック数を-1セット
                            Debug( "Add the rest Cube.", 2, player );
                            player.getInventory().addItem( item2 );
                        }

                    } else {
                        Block block = event.getClickedBlock();
                        Boolean ActCancel = true;

                        if  ( !( block == null ) ) { 
                            Debug( StringBuild( " Clicked to ", block.getType().toString().toUpperCase() ), 2, player );
                            ActCancel = !( block.getType().equals( Material.CHEST ) || block.getType().equals( Material.TRAPPED_CHEST ) );
                        }
                        if ( ActCancel ) {
                            event.setCancelled( true );
                        }
                        player.sendMessage( ReplaceString( player, config.getSneaking() ) );
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand( CommandSender sender,Command cmd, String commandLabel, String[] args ) {
        if ( cmd.getName().equalsIgnoreCase( "cubeget" ) ) {
            if ( !( sender instanceof Player ) ) {
                Bukkit.getServer().getConsoleSender().sendMessage( ReplaceString( (Player)null, config.InsideErrorMsg() ) );
            } else {
                Player p = (Player)sender;
                p.sendMessage( ChatColor.AQUA + "[ExpCube] ExpCube 1 Get !!" );

                ItemStack is = new ItemStack( Material.QUARTZ_BLOCK, 1 );
                
                p.getInventory().addItem( ItemToInventory( is, 0 ) );
            }
            return true;
        }
        if ( cmd.getName().equalsIgnoreCase( "ExpCube" ) ) {
            if ( args.length > 0 ) {
                if ( args[0].equals( "reload" ) ) {
                    config = new Config( this );
                    sender.sendMessage( ChatColor.GREEN + "ExpCube Config Reloaded." );
                }
                if ( args[0].equals( "status" ) ) {
                    PrintStatus( sender );
                }
                if ( args[0].equals( "mode" ) ) {
                    config.setOrbMode( !config.getOrbMode() );
                    sender.sendMessage( ChatColor.GREEN + "Change Mode to " + ( config.getOrbMode() ? "ExpOrb":"Direct" ) );
                }
            }
            return true;
        }
        return false;
    }

}
