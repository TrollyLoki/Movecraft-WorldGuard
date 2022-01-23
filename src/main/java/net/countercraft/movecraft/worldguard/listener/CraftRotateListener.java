package net.countercraft.movecraft.worldguard.listener;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.PilotedCraft;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.util.hitboxes.HitBox;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import net.countercraft.movecraft.worldguard.localisation.I18nSupport;
import net.countercraft.movecraft.worldguard.utils.CustomFlags;
import net.countercraft.movecraft.worldguard.utils.WorldGuardUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CraftRotateListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onCraftRotateEvent(@NotNull CraftRotateEvent e){
        Craft craft = e.getCraft();
        HitBox hitBox = craft.getHitBox();
        if(!(craft instanceof PilotedCraft) || hitBox.isEmpty())
            return;

        WorldGuardUtils wgUtils = MovecraftWorldGuard.getInstance().getWGUtils();
        World w = craft.getWorld();
        Player p = ((PilotedCraft) craft).getPilot();
        if(wgUtils.allowedTo(p, w, e.getNewHitBox(), CustomFlags.ALLOW_ROTATE))
            return; // return if the player is allowed to translate in the new location

        // Find the first offending location and notify the player
        boolean canBuild = true;
        MovecraftLocation location = hitBox.getMidPoint();
        for(MovecraftLocation ml : hitBox) {
            Location loc = ml.toBukkit(w);
            if(!wgUtils.allowedTo(p, loc, Flags.BUILD)) {
                canBuild = false;
                location = ml;
                break;
            }
            if(!wgUtils.allowedTo(p, loc, CustomFlags.ALLOW_ROTATE)) {
                location = ml;
                break;
            }
        }

        e.setCancelled(true);
        String message;
        if(!canBuild)
            message = I18nSupport.getInternationalisedString("Rotation - WorldGuard - Not Permitted To Build");
        else
            message = I18nSupport.getInternationalisedString("CustomFlags - Rotation Failed");

        e.setFailMessage(message + String.format(" @ %d,%d,%d", location.getX(), location.getY(), location.getZ()));
    }
}
