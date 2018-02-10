/*
 *  Copyright (c) 2018 sugichan. All rights reserved.
 */
package com.mycompany.expcube;

import java.util.List;
import java.util.ArrayList;
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

/**
 *
 * @author sugichan
 */
public class ExpCube extends JavaPlugin implements Listener {

    private Config config;
    static private int MiniExp = 100;
    static private int MaxExp = 1000;

    // 0:none 1:normal 2:full
    public void Debug( String msg, int lvl ) {
        Boolean prtf;
        String LogMsg = ChatColor.WHITE + msg;
        switch ( config.getDebug() ) {
            case 0:
                prtf = ( lvl == 0 );
                break;
            case 1:
                LogMsg = ChatColor.YELLOW + "(DB:n) " + ChatColor.WHITE + msg;;
                prtf = ( lvl == 1 );
                break;
            case 2:
                LogMsg = ChatColor.YELLOW + "(DB:f) " + ChatColor.WHITE + msg;
                prtf = true;
                break;
            default:
                prtf = false;
        }
        if ( prtf ) {
            Bukkit.getServer().getConsoleSender().sendMessage( LogMsg );
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
            MSG = "§eスニーク§fしながら";
            lores.add( MSG );
            MSG = "§b右クリック§fで貯める";
            lores.add( MSG );
            MSG = "§b左クリック§fで取り出す";
            lores.add( MSG );
        }
        
        ItemMeta im = item.getItemMeta();   //ItemStackから、ItemMetaを取得します。
        im.setDisplayName( "§aExpCube" );  //Item名を設定
        im.setLore( lores );                //loreを設定します。
        im.addItemFlags( ItemFlag.HIDE_ENCHANTS );
        item.setItemMeta(im);               //元のItemStackに、変更したItemMetaを設定します。

        item.addUnsafeEnchantment( Enchantment.PROTECTION_ENVIRONMENTAL, ench );
                    
        return item;
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

    public void setNewLevel( Player p ) {
        p.setLevel( 0 );
        p.setExp( 0 );
        int TotalExp = p.getTotalExperience();
        for( ;TotalExp > p.getExpToLevel(); )
        {
            TotalExp -= p.getExpToLevel();
            p.setLevel( p.getLevel() + 1 );
        }
        float xp = (float)TotalExp / (float)p.getExpToLevel();
        p.setExp(xp);                                        
    }

    @EventHandler
    public void Click(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemStack item2 = new ItemStack( item ); // 複数スタックの退避用

        if ( item.getType() == Material.QUARTZ_BLOCK ) {
            if ( item.getItemMeta().hasDisplayName() ) {
                if ( item.getItemMeta().getDisplayName().equalsIgnoreCase( "§aExpCube" ) ) {
                    
                    if ( player.isSneaking() ) {
                        event.setCancelled(true);
                        player.getInventory().setItemInMainHand( null );
                        
                        int ench = item.getItemMeta().getEnchantLevel( Enchantment.PROTECTION_ENVIRONMENTAL );

                        Debug( player.getName() + " Befor Ex" + player.getExp() + ":Lv" + player.getLevel() + " > " + player.getTotalExperience(), 2 );

                        // if( action.equals( Action.RIGHT_CLICK_AIR ) || action.equals( Action.RIGHT_CLICK_BLOCK ) ) {
                        if( action.equals( Action.RIGHT_CLICK_AIR ) ) {
                            if ( player.hasPermission( "ExpCube.set" ) ) {
                                if ( ench<10 ) {
                                    if ( MiniExp<player.getTotalExperience() ) {
                                        ench++;

                                        int OldExp = player.getTotalExperience();
                                        // int totalExp = player.getTotalExperience() - MiniExp;
                                        player.setTotalExperience( player.getTotalExperience() - MiniExp );
                                        player.sendMessage( config.getExpToCube() + "(" + ( ench*MiniExp ) +  "/" + MaxExp + ")" );
                                        Debug( player.getName() + " Right Click Cube(" + ench + ") Exp(" + OldExp + " => " + player.getTotalExperience() + ")", 1 );

                                        setNewLevel( player );
                                        
                                    } else {
                                        if ( config.getNoEnough().equals( "" ) ) {
                                            player.sendMessage( ChatColor.RED + "[ExpCube] Not enough EXP, Your have " + player.getTotalExperience() + " EXP" );
                                        } else {
                                            player.sendMessage( config.getNoEnough() + "(" + player.getTotalExperience() + " Exp.)" );
                                        }
                                        Debug( player.getName() + " Not enough EXP", 1 );
                                    }
                                } else {
                                    player.sendMessage( config.getCubeFull() + "(" + player.getTotalExperience() + ")" );
                                    Debug( player.getName() + " Cube Full", 1 );
                                }
                            } else {
                                player.sendMessage( ChatColor.RED + "You do not have permission" );
                            }
                        }
                        
                        //  if( action.equals( Action.LEFT_CLICK_AIR ) || ( action.equals( Action.LEFT_CLICK_BLOCK ) ) ) {
                        if( action.equals( Action.LEFT_CLICK_AIR ) ) {
                            if ( player.hasPermission( "ExpCube.get" ) ) {
                                if ( ench>0 ) {
                                    ench--;

                                    player.sendMessage( config.getExpFromCube() + "(" + ( ench*MiniExp ) +  "/" + MaxExp + ")" );
                                    Debug( player.getName() + " Left Click Cube(" + ench + ") Exp(" + player.getTotalExperience() + ")", 1 );

                                    if ( config.getOrbMode() ) {
                                        //  従来は直接EXPに反映していたが、"修繕"への影響を加味し
                                        //  経験値100のExpOrbをドロップする形式
                                        Location loc = player.getLocation();
                                        ExperienceOrb exp = (ExperienceOrb) loc.getBlock().getWorld().spawn( loc.getBlock().getLocation().add( 0, 0, 0 ), ExperienceOrb.class );
                                        exp.setExperience( MiniExp );
                                    } else {
                                        //  従来のEｘｐ直接反映方式
                                        //  オフハンドに修繕アイテムがある場合の処理を追加して対応
                                        int BackExp = MiniExp;

                                        //  オフハンドに修繕するアイテムがあるかチェックするとこ
                                        ItemStack offHand = player.getInventory().getItemInOffHand();
                                        if ( offHand.getAmount()>0 ) {
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
                                                    Debug( player.getName() + ChatColor.AQUA + " Repair Item (" + dmg + ") to Exp(" + BackExp + ")", 1 );
                                                    offHand.setDurability( (short) dmg );
                                                }
                                            }
                                        }
                                        //  player.giveExp( BackExp );
                                        player.setTotalExperience( player.getTotalExperience() + BackExp );
                                        setNewLevel( player );
                                    }

                                } else {
                                    player.sendMessage( config.getCubeEmpty() + "(" + player.getTotalExperience() + ")" );
                                    Debug( player.getName() + " Cube Empty", 1 );
                                }
                            } else {
                                player.sendMessage( ChatColor.RED + "You do not have permission" );
                            }
                        }
                        Debug( player.getName() + " After Ex" + player.getExp() + ":Lv" + player.getLevel() + " > " + player.getTotalExperience(), 2 );

                        int amount = item.getAmount();
                        Debug( player.getName() + " Have Amount is [" + amount + "]", 2 );
                        // player.setItemInHand( null );
                        item.setAmount( 1 );    // １スタックに強制設定
                        player.getInventory().setItemInMainHand( ItemToInventory( item, ench ) );

                        if ( amount>1 ) {
                            item2.setAmount( --amount );               // スタック数を-1セット
                            Debug( player.getName() + " Add the rest Cube.", 2 );
                            player.getInventory().addItem( item2 );
                        }

                    } else {
                        Block block = event.getClickedBlock();
                        Boolean ActCancel = true;
                        
                        if  ( !( block == null ) ) { 
                            Debug( player.getName() + " Clicked to " + block.getType().toString().toUpperCase(), 2 );
                            ActCancel = !( block.getType().equals( Material.CHEST ) || block.getType().equals( Material.TRAPPED_CHEST ) );
                        }
                        if ( ActCancel ) {
                            event.setCancelled(true);
                        }
                        player.sendMessage( config.getSneaking() );
                    }
                }
            }
        }

    }

    @Override
    public boolean onCommand(CommandSender sender,Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("cubeget")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください。");
            } else {
                Player p = (Player)sender;
                p.sendMessage(ChatColor.AQUA + "[ExpCube] ExpCube 1 Get !!");

                ItemStack is = new ItemStack(Material.QUARTZ_BLOCK, 1);
                
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

