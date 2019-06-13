/*
 *  Copyright (c) 2018 Kumaisu. All rights reserved.
 */
package com.mycompany.expcube;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.mycompany.expcube.config.Config;
import com.mycompany.expcube.tool.ExpCalc;
import com.mycompany.kumaisulibraries.Utility;
import com.mycompany.kumaisulibraries.Tools;
import static com.mycompany.expcube.config.Config.programCode;
import static com.mycompany.kumaisulibraries.Tools.consoleMode;

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

        if ( Config.OnRecipe ) {
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
            Msg = Msg.replace( "%TotalExp%", String.valueOf( ExpCalc.getNowTotalExp( p ) ) );
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

        if ( item.getType() != Material.QUARTZ_BLOCK ) { return; }
        if ( !item.getItemMeta().hasDisplayName() ) { return; }
        if ( !item.getItemMeta().getDisplayName().equalsIgnoreCase( "§aExpCube" ) ) { return; }

        if ( player.isSneaking() ) {
            event.setCancelled( true );
            if ( ( item.getAmount() > 1 ) && ( !Arrays.asList( player.getInventory().getStorageContents() ).contains( null ) ) ) {
                Tools.Prt( player, ReplaceString( player, config.InventoryFullMsg() ), consoleMode.full, programCode );
                return;
            }

            //  CoolTimer
            //  ClickFlag が true ならクリックキャンセルだけして終わり
            //  false なら、true にしてタイマー起動後、処理
            if ( ClickFlag ) { return; }
            ClickFlag = true;
            task = this.getServer().getScheduler().runTaskTimer( this, new Timer( this , Config.CoolCount ), 0L, Config.CoolTick );

            player.getInventory().setItemInMainHand( null );

            int ench = item.getItemMeta().getEnchantLevel( Enchantment.PROTECTION_ENVIRONMENTAL );

            if ( Tools.isDebugFlag( consoleMode.full, programCode ) ) {
                Tools.Prt(
                    player,
                    Utility.StringBuild(
                        ChatColor.GREEN.toString(), "[ExpCube]",
                        ChatColor.YELLOW.toString(), " Now your Experience is ",
                        String.valueOf( ExpCalc.getNowTotalExp( player ) ), "."
                    ),
                    consoleMode.max, programCode
                );
            }
            Tools.Prt( 
                Utility.StringBuild(
                    player.getDisplayName(),
                    " Befor Ex", String.valueOf( player.getExp() ),
                    ":Lv", String.valueOf( player.getLevel() ),
                    " > ", String.valueOf( ExpCalc.getNowTotalExp( player ) )
                ),
                consoleMode.full, programCode
            );

            // if( action.equals( Action.RIGHT_CLICK_AIR ) || action.equals( Action.RIGHT_CLICK_BLOCK ) ) {
            if( action.equals( Action.RIGHT_CLICK_AIR ) ) {
                if ( player.hasPermission( "ExpCube.set" ) ) {
                    Tools.Prt(
                        Utility.StringBuild( "Cube State = ", String.valueOf( ench ) ),
                        consoleMode.full, programCode
                    );
                    if ( ench<10 ) {
                        Tools.Prt(
                            Utility.StringBuild( "Player Experience = ", String.valueOf( ExpCalc.getNowTotalExp( player ) ) ),
                            consoleMode.full, programCode
                        );
                        if ( !( MiniExp>ExpCalc.getNowTotalExp( player ) ) ) {
                            ench++;

                            int OldExp = ExpCalc.getNowTotalExp( player );
                            Tools.Prt( player, ReplaceString( player, Config.ExpToCube, ench*MiniExp, MaxExp ), consoleMode.max, programCode );
                            ExpCalc.setNewExp( player, OldExp - MiniExp );
                            Tools.Prt(
                                Utility.StringBuild(
                                    player.getDisplayName(),
                                    " Right Click Cube(", String.valueOf( ench ),
                                    ") Exp(", String.valueOf( OldExp ),
                                    " => ", String.valueOf( ExpCalc.getNowTotalExp( player ) ), ")"
                                ),
                                consoleMode.full, programCode
                            );
                        } else Tools.Prt( player, ReplaceString( player, Config.NoEnough ), consoleMode.full, programCode );
                    } else Tools.Prt( player, ReplaceString( player, Config.CubeFull ), consoleMode.full, programCode );
                } else Tools.Prt( player, ReplaceString( player, config.getNoPermission() ), consoleMode.full, programCode );
            }

            //  if( action.equals( Action.LEFT_CLICK_AIR ) || ( action.equals( Action.LEFT_CLICK_BLOCK ) ) ) {
            if( action.equals( Action.LEFT_CLICK_AIR ) ) {
                if ( player.hasPermission( "ExpCube.get" ) ) {
                    if ( ench>0 ) {
                        ench--;

                        Tools.Prt( player, ReplaceString( player, Config.ExpFromCube, ench*MiniExp, MaxExp ), consoleMode.max, programCode );
                        Tools.Prt(
                            Utility.StringBuild(
                                player.getDisplayName(),
                                " Left Click Cube(", String.valueOf( ench ),
                                ") Exp(", String.valueOf( ExpCalc.getNowTotalExp( player ) ), ")"
                            ),
                            consoleMode.full, programCode
                        );

                        if ( Config.OrbMode ) {
                            Tools.Prt( "OrbMode", consoleMode.full, programCode );
                            //  従来は直接EXPに反映していたが、"修繕"への影響を加味し
                            //  経験値100のExpOrbをドロップする形式
                            Location loc = player.getLocation();
                            ExperienceOrb exp = (ExperienceOrb) loc.getBlock().getWorld().spawn( loc.getBlock().getLocation().add( 0, 0, 0 ), ExperienceOrb.class );
                            exp.setExperience( MiniExp );
                        } else {
                            Tools.Prt( "None OrbMode", consoleMode.full, programCode );
                            //  従来のEｘｐ直接反映方式
                            //  オフハンドに修繕アイテムがある場合の処理を追加して対応
                            int BackExp = MiniExp;

                            //  オフハンドに修繕するアイテムがあるかチェックするとこ
                            Tools.Prt( "Get off Hand", consoleMode.full, programCode );

                            if ( player.getInventory().getItemInOffHand().getType() != Material.AIR ) {
                                ItemStack offHand = player.getInventory().getItemInOffHand();
                                Tools.Prt(
                                    Utility.StringBuild(
                                        "OffHand = ", offHand.getItemMeta().getDisplayName()
                                    ),
                                    consoleMode.full, programCode
                                );
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
                                        Tools.Prt(
                                            Utility.StringBuild(
                                                player.getDisplayName(),
                                                ChatColor.AQUA.toString(), " Repair Item (",
                                                String.valueOf( dmg ), ") to Exp(",
                                                String.valueOf( BackExp ), ")"
                                            ),
                                            consoleMode.full, programCode
                                        );
                                        offHand.setDurability( (short) dmg );
                                    }
                                }
                            }
                            Tools.Prt( "Set Exp " + BackExp, consoleMode.full, programCode );

                            ExpCalc.setNewExp( player, ExpCalc.getNowTotalExp( player ) + BackExp );
                        }
                    } else Tools.Prt( player, ReplaceString( player, Config.CubeEmpty ), consoleMode.full, programCode );
                } else Tools.Prt( player, ReplaceString( player, config.getNoPermission() ), consoleMode.full, programCode );
            }
            if ( Tools.isDebugFlag( consoleMode.full, programCode ) ) {
                Tools.Prt(
                    player,
                    Utility.StringBuild(
                        ChatColor.GREEN.toString(), "[ExpCube]",
                        ChatColor.YELLOW.toString(), " Now your Experience is ",
                        String.valueOf( ExpCalc.getNowTotalExp( player ) ), "."
                    ),
                    consoleMode.max, programCode
                );
            }
            Tools.Prt(
                Utility.StringBuild(
                    player.getDisplayName(),
                    " After Ex", String.valueOf( player.getExp() ),
                    ":Lv", String.valueOf( player.getLevel() ),
                    " > ", String.valueOf( ExpCalc.getNowTotalExp( player ) )
                ),
                consoleMode.full, programCode
            );

            int amount = item.getAmount();
            Tools.Prt( Utility.StringBuild( player.getDisplayName(), " Have Amount is [", String.valueOf( amount ), "]" ), consoleMode.full, programCode );
            item.setAmount( 1 );    // １スタックに強制設定
            player.getInventory().setItemInMainHand( ItemToInventory( item, ench ) );

            if ( amount>1 ) {
                item2.setAmount( --amount );               // スタック数を-1セット
                Tools.Prt( player.getDisplayName() + "Add the rest Cube.", consoleMode.full, programCode );
                player.getInventory().addItem( item2 );
            }
        } else {
            Block block = event.getClickedBlock();
            Boolean ActCancel = true;

            if  ( !( block == null ) ) {
                Tools.Prt(
                    Utility.StringBuild(
                        player.getDisplayName(),
                        ChatColor.LIGHT_PURPLE.toString(), " Clicked to ",
                        block.getType().toString().toUpperCase()
                    ),
                    consoleMode.full, programCode
                );
                ActCancel = !( block.getType().equals( Material.CHEST ) || block.getType().equals( Material.TRAPPED_CHEST ) );
            }
            if ( ActCancel ) {
                Tools.Prt( player, ReplaceString( player, Config.Sneaking ), consoleMode.full, programCode );
                event.setCancelled( true );
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
        Player player = ( ( sender instanceof Player ) ? (Player)sender:(Player)null );

        if ( cmd.getName().equalsIgnoreCase( "cubeget" ) ) {
            if ( player == null ) {
                Tools.Prt( ReplaceString( player, config.InsideErrorMsg() ), programCode );
                return false;
            } else {
                Tools.Prt( player, ChatColor.AQUA + "[ExpCube] ExpCube 1 Get !!", consoleMode.max, programCode );
                ItemStack is = new ItemStack( Material.QUARTZ_BLOCK, 1 );
                player.getInventory().addItem( ItemToInventory( is, 0 ) );
            }
            return true;
        }
        if ( cmd.getName().equalsIgnoreCase( "ExpCube" ) ) {
            if ( args.length > 0 ) {
                switch ( args[0] ) {
                    case "reload":
                        config = new Config( this );
                        Tools.Prt( player, ChatColor.GREEN + "ExpCube Config Reloaded.", consoleMode.max, programCode );
                        return true;
                    case "status":
                        config.PrintStatus( player );
                        return true;
                    case "mode":
                        Config.OrbMode = !Config.OrbMode;
                        Tools.Prt( player, ChatColor.GREEN + "Change Mode to " + ( Config.OrbMode ? "ExpOrb":"Direct" ), consoleMode.max, programCode );
                        return true;
                    case "playerstatus":
                    case "ps":
                        ExpCalc.PlayerStatus( player );
                        return true;
                    case "console":
                        if ( args.length>1 ) {
                            Tools.setDebug( args[1], programCode );
                        } else Tools.Prt( player, "usage: ExpCube console [full/normal/none]", consoleMode.max, programCode );
                        Tools.Prt( player,
                            ChatColor.GREEN + "System Debug Mode is [ " +
                            ChatColor.RED + Tools.consoleFlag.get( programCode ).toString() +
                            ChatColor.GREEN + " ]",
                            ( ( player == null ) ? consoleMode.none:consoleMode.max ), programCode
                        );
                        return true;
                    default:
                }
            }
        }
        return false;
    }
}
