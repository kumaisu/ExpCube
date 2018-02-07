/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.expcube

import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable

/*

  @author sugichan
  Created on 2018/01/21
*/

class PlayerFlag(val player: Player, val plugin: Plugin) {

    fun set() {
        setMetadata(true)
        object : BukkitRunnable() {
            override fun run() {
                setMetadata(false)
            }
        }.runTaskLater(plugin, 10)
    }

    private fun setMetadata(flag: Boolean) {
        player.setMetadata("PlayerInteractEvent", FixedMetadataValue(plugin, flag))
    }

    fun get(): Boolean {
        try {
            return player.getMetadata("PlayerInteractEvent")[0].value() as Boolean
        } catch (e: Exception) {
            return false
        }
    }
}