/*
 *  Copyright (c) 2018 Kumaisu. All rights reserved.
 */
package io.github.kumaisu.expcube;

import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import io.github.kumaisu.expcube.command.ECCommand;
import io.github.kumaisu.expcube.TabComplete.ExpCubeTabComp;
import io.github.kumaisu.expcube.config.Config;
import io.github.kumaisu.expcube.tool.ExpCalc;
import io.github.kumaisu.expcube.tool.CubeTool;
import io.github.kumaisu.expcube.Lib.Utility;
import io.github.kumaisu.expcube.Lib.Tools;
import static io.github.kumaisu.expcube.config.Config.programCode;
import static io.github.kumaisu.expcube.Lib.Tools.consoleMode;

/**
 *
 * @author Kumaisu
 */
public class ExpCube extends JavaPlugin implements Listener {

    public Config config;

    /**
     * プラグイン起動時のシーケンス
     */
    @Override
    public void onEnable() {
        config = new Config( this );
        getServer().getPluginManager().registerEvents( this, this );
        getCommand( "expcube" ).setExecutor( new ECCommand( this ) );
        getCommand( "expcube" ).setTabCompleter( new ExpCubeTabComp() );

        if ( Config.OnRecipe ) {
            this.getLogger().info( "ExpCube Recipe Enable." );
            ItemStack item = new ItemStack(Material.QUARTZ_BLOCK, 1);
            item = CubeTool.ItemToInventory( item, 0 );
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
                Tools.Prt( player, CubeTool.ReplaceString( player, Config.InventoryFullMsg ), consoleMode.full, programCode );
                return;
            }

            player.getInventory().setItemInMainHand( null );

            int ench = item.getItemMeta().getEnchantLevel( Enchantment.PROTECTION );

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
                        if ( !( Config.MiniExp>ExpCalc.getNowTotalExp( player ) ) ) {
                            ench++;

                            int OldExp = ExpCalc.getNowTotalExp( player );
                            Tools.Prt( player, CubeTool.ReplaceString( player, Config.ExpToCube, ench*Config.MiniExp, Config.MaxExp ), consoleMode.max, programCode );
                            ExpCalc.setNewExp( player, OldExp - Config.MiniExp );
                            Tools.Prt(
                                Utility.StringBuild(
                                    player.getDisplayName(),
                                    " Right Click Cube(", String.valueOf( ench ),
                                    ") Exp(", String.valueOf( OldExp ),
                                    " => ", String.valueOf( ExpCalc.getNowTotalExp( player ) ), ")"
                                ),
                                consoleMode.full, programCode
                            );
                        } else Tools.Prt( player, CubeTool.ReplaceString( player, Config.NoEnough ), consoleMode.full, programCode );
                    } else Tools.Prt( player, CubeTool.ReplaceString( player, Config.CubeFull ), consoleMode.full, programCode );
                } else Tools.Prt( player, CubeTool.ReplaceString( player, Config.NoPermission ), consoleMode.full, programCode );
            }

            //  if( action.equals( Action.LEFT_CLICK_AIR ) || ( action.equals( Action.LEFT_CLICK_BLOCK ) ) ) {
            if( action.equals( Action.LEFT_CLICK_AIR ) ) {
                if ( player.hasPermission( "ExpCube.get" ) ) {
                    if ( ench>0 ) {
                        ench--;

                        Tools.Prt( player, CubeTool.ReplaceString( player, Config.ExpFromCube, ench*Config.MiniExp, Config.MaxExp ), consoleMode.max, programCode );
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
                            exp.setExperience( Config.MiniExp );
                        } else {
                            Tools.Prt( "None OrbMode", consoleMode.full, programCode );
                            //  従来のEｘｐ直接反映方式
                            //  オフハンドに修繕アイテムがある場合の処理を追加して対応
                            int BackExp = Config.MiniExp;

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
                    } else Tools.Prt( player, CubeTool.ReplaceString( player, Config.CubeEmpty ), consoleMode.full, programCode );
                } else Tools.Prt( player, CubeTool.ReplaceString( player, Config.NoPermission ), consoleMode.full, programCode );
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
            player.getInventory().setItemInMainHand( CubeTool.ItemToInventory( item, ench ) );

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
                Tools.Prt( player, CubeTool.ReplaceString( player, Config.Sneaking ), consoleMode.full, programCode );
                event.setCancelled( true );
            }
        }
    }
}
