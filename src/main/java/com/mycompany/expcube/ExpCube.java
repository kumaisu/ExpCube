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
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.expcube.config.Config;

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

    /**
     * Expの出し入れの際に一定間隔のクールタイムを実行する
     */
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

    /**
     * プラグイン起動時のシーケンス
     */
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

    /**
     * デバッグモードに応じた表示切り替え
     *
     * @param msg 
     * @param player 
     * @param key 
     */
    public void Debug( String msg, Player player, Utility.consoleMode key ) {
        if ( config.isDebugFlag( key ) ) {
            String printString = Utility.StringBuild( ChatColor.YELLOW.toString(), "(EC:", key.toString(), ") " );
            if ( player != null ) {
                printString = Utility.StringBuild( printString, player.getDisplayName(), " " );
            }
            printString = Utility.StringBuild( printString, ChatColor.WHITE.toString(), msg );
            Utility.Prt( printString );
        }
    }

    /**
     * ExpCube の生成
     *
     * @param item
     * @param ench
     * @return 
     */
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

    /**
     * 現状のプレイヤーの経験値情報などのステータス表示
     *
     * @param sender 
     */
    public void PlayerStatus( CommandSender sender ) {
        Player p = ( Player ) sender;
        sender.sendMessage( ChatColor.AQUA + "Player name " + ChatColor.WHITE + p.getDisplayName() );
        sender.sendMessage( ChatColor.AQUA + "Level   : " + ChatColor.WHITE + p.getLevel() );
        sender.sendMessage( ChatColor.AQUA + "Exp     : " + ChatColor.WHITE + p.getExp() );
        sender.sendMessage( ChatColor.AQUA + "CalcExp : " + ChatColor.WHITE + convertExpPercentageToExp( p.getLevel(), p.getExp() ) );
        sender.sendMessage( ChatColor.AQUA + "Total   : " + ChatColor.WHITE + getNowTotalExp( p ) );
        sender.sendMessage( ChatColor.AQUA + "G Total : " + ChatColor.WHITE + p.getTotalExperience() );
    }

    /**
     * Configで設定された定型文言を差し替える
     *
     * @param p
     * @param Msg
     * @param nums
     * @return 
     */
    public String ReplaceString( Player p, String Msg, int ... nums ) {
        if ( "".equals( Msg ) ) { return config.ConfigErrorMsg(); }

        if ( p != null ) {
            Msg = Msg.replace( "%player%", p.getName() );
            Msg = Msg.replace( "%TotalExp%", String.valueOf( getNowTotalExp( p ) ) );
        }
        if ( nums.length>0 ) {
            Msg = Msg.replace( "%minExp%", String.valueOf( nums[0] ) );
            Msg = Msg.replace( "%maxExp%", String.valueOf( nums[1] ) );
        }
        Msg = Msg.replace( "%$", "§" );
        return Msg;
    }

    /**
     * 経験値出し入れのメインルーチン
     *
     * @param event 
     */
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

                        if ( ( item.getAmount() > 1 ) && ( !Arrays.asList( player.getInventory().getStorageContents() ).contains( null ) ) ) {
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

                        if ( config.getDebug() == Utility.consoleMode.full ) {
                            player.sendMessage( Utility.StringBuild( ChatColor.GREEN.toString(), "[ExpCube]", ChatColor.YELLOW.toString(), " Now your Experience is ", String.valueOf( getNowTotalExp( player ) ), "." ) );
                        }
                        Debug( Utility.StringBuild(
                                "Befor Ex", String.valueOf( player.getExp() ),
                                ":Lv", String.valueOf( player.getLevel() ),
                                " > ", String.valueOf( getNowTotalExp( player ) )
                                ), player, Utility.consoleMode.normal );

                        // if( action.equals( Action.RIGHT_CLICK_AIR ) || action.equals( Action.RIGHT_CLICK_BLOCK ) ) {
                        if( action.equals( Action.RIGHT_CLICK_AIR ) ) {
                            if ( player.hasPermission( "ExpCube.set" ) ) {
                                Debug( Utility.StringBuild( "Cube State = ", String.valueOf( ench ) ), null, Utility.consoleMode.full );
                                if ( ench<10 ) {
                                    Debug( Utility.StringBuild( "Player Experience = ", String.valueOf( getNowTotalExp( player ) ) ), null, Utility.consoleMode.full );
                                    if ( !( MiniExp>getNowTotalExp( player ) ) ) {
                                        ench++;

                                        int OldExp = getNowTotalExp( player );
                                        player.sendMessage( ReplaceString( player, config.getExpToCube(), ench*MiniExp, MaxExp ) );
                                        setNewExp( player, OldExp - MiniExp );
                                        Debug( Utility.StringBuild( "Right Click Cube(", String.valueOf( ench ), ") Exp(", String.valueOf( OldExp ), " => ", String.valueOf( getNowTotalExp( player ) ), ")" ), player, Utility.consoleMode.normal );

                                    } else {
                                        player.sendMessage( ReplaceString( player, config.getNoEnough() ) );
                                        Debug( ReplaceString( player, config.getNoEnough() ), player, Utility.consoleMode.normal );
                                    }
                                } else {
                                    player.sendMessage( ReplaceString( player, config.getCubeFull() ) );
                                    Debug( ReplaceString( player, config.getCubeFull() ), player, Utility.consoleMode.normal );
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
                                    Debug( Utility.StringBuild( "Left Click Cube(", String.valueOf( ench ), ") Exp(", String.valueOf( getNowTotalExp( player ) ), ")" ), player, Utility.consoleMode.normal );

                                    if ( config.getOrbMode() ) {
                                        Debug( "OrbMode", null, Utility.consoleMode.full );
                                        //  従来は直接EXPに反映していたが、"修繕"への影響を加味し
                                        //  経験値100のExpOrbをドロップする形式
                                        Location loc = player.getLocation();
                                        ExperienceOrb exp = (ExperienceOrb) loc.getBlock().getWorld().spawn( loc.getBlock().getLocation().add( 0, 0, 0 ), ExperienceOrb.class );
                                        exp.setExperience( MiniExp );
                                    } else {
                                        Debug( "None OrbMode", null, Utility.consoleMode.full );
                                        //  従来のEｘｐ直接反映方式
                                        //  オフハンドに修繕アイテムがある場合の処理を追加して対応
                                        int BackExp = MiniExp;

                                        //  オフハンドに修繕するアイテムがあるかチェックするとこ
                                        Debug( "Get off Hand", null, Utility.consoleMode.full );

                                        if ( player.getInventory().getItemInOffHand().getType() != Material.AIR ) {
                                            ItemStack offHand = player.getInventory().getItemInOffHand();
                                            Debug( Utility.StringBuild( "OffHand = ", offHand.getItemMeta().getDisplayName() ), null, Utility.consoleMode.full );
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
                                                    Debug( Utility.StringBuild( ChatColor.AQUA.toString(), " Repair Item (", String.valueOf( dmg ), ") to Exp(", String.valueOf( BackExp ), ")" ), player, Utility.consoleMode.normal );
                                                    offHand.setDurability( (short) dmg );
                                                }
                                            }
                                        }
                                        Debug( "Set Exp " + BackExp, null, Utility.consoleMode.full );

                                        setNewExp( player, getNowTotalExp( player ) + BackExp );
                                    }

                                } else {
                                    player.sendMessage( ReplaceString( player, config.getCubeEmpty() ) );
                                    Debug( ReplaceString( player, config.getCubeEmpty() ), player, Utility.consoleMode.normal );
                                }
                            } else {
                                player.sendMessage( ReplaceString( player, config.getNoPermission() ) );
                            }
                        }
                        if ( config.getDebug() == Utility.consoleMode.full ) {
                            player.sendMessage( Utility.StringBuild( ChatColor.GREEN.toString(), "[ExpCube]", ChatColor.YELLOW.toString(), " Now your Experience is ", String.valueOf( getNowTotalExp( player ) ), "." ) );
                        }
                        Debug( Utility.StringBuild( "After Ex", String.valueOf( player.getExp() ), ":Lv", String.valueOf( player.getLevel() ), " > ", String.valueOf( getNowTotalExp( player ) ) ), player, Utility.consoleMode.normal );

                        int amount = item.getAmount();
                        Debug( Utility.StringBuild( "Have Amount is [", String.valueOf( amount ), "]" ), player, Utility.consoleMode.full );
                        item.setAmount( 1 );    // １スタックに強制設定
                        player.getInventory().setItemInMainHand( ItemToInventory( item, ench ) );

                        if ( amount>1 ) {
                            item2.setAmount( --amount );               // スタック数を-1セット
                            Debug( "Add the rest Cube.", player, Utility.consoleMode.full );
                            player.getInventory().addItem( item2 );
                        }

                    } else {
                        Block block = event.getClickedBlock();
                        Boolean ActCancel = true;

                        if  ( !( block == null ) ) {
                            Debug( Utility.StringBuild( ChatColor.LIGHT_PURPLE.toString(), "Clicked to ", block.getType().toString().toUpperCase() ), player, Utility.consoleMode.full );
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
        if ( cmd.getName().equalsIgnoreCase( "cubeget" ) ) {
            if ( !( sender instanceof Player ) ) {
                Utility.Prt( ReplaceString( (Player)null, config.InsideErrorMsg() ) );
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
                switch ( args[0] ) {
                    case "reload":
                        config = new Config( this );
                        sender.sendMessage( ChatColor.GREEN + "ExpCube Config Reloaded." );
                        break;
                    case "status":
                        config.PrintStatus( sender );
                        break;
                    case "mode":
                        config.setOrbMode( !config.getOrbMode() );
                        sender.sendMessage( ChatColor.GREEN + "Change Mode to " + ( config.getOrbMode() ? "ExpOrb":"Direct" ) );
                        break;
                    case "playerstatus":
                    case "ps":
                        PlayerStatus( sender );
                        break;
                    case "console":
                        if ( args.length>1 ) {
                            config.setDebug( args[1] );
                        } else sender.sendMessage( "usage: ExpCube console [full/normal/none]" );
                        sender.sendMessage(
                                ChatColor.GREEN + "System Debug Mode is [ " +
                                ChatColor.RED + config.getDebug().toString() +
                                ChatColor.GREEN + " ]"
                            );
                    default:
                }
            } else {
                sender.sendMessage( "usage: ExpCube command" );
                sender.sendMessage( "  reload" );
                sender.sendMessage( "  status" );
                sender.sendMessage( "  mode" );
                sender.sendMessage( "  playerstatus/ps" );
                sender.sendMessage( "  console [full/normal/none]" );
            }
            return true;
        }
        return false;
    }

}
