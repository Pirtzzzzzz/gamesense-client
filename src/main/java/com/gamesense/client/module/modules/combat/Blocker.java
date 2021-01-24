package com.gamesense.client.module.modules.combat;

import com.gamesense.api.setting.Setting;
import com.gamesense.api.util.combat.CrystalUtil;
import com.gamesense.api.util.misc.MessageBus;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import com.gamesense.client.module.modules.gui.ColorMain;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static com.gamesense.api.util.player.RotationUtil.ROTATION_UTIL;

/**
 * @Author TechAle on (date)
 * Ported and modified from AutoTrap.java,
 * Ported Crystal Break from AutoCrystal.java
 */

public class Blocker extends Module {

    public Blocker() {
        super("Blocker", Category.Combat);
    }

    Setting.Boolean chatMsg;
    Setting.Boolean rotate;
    Setting.Boolean anvilBlocker;
    Setting.Boolean pistonBlocker;
    Setting.Integer tickDelay;

    public void setup() {
        rotate = registerBoolean("Rotate", true);
        anvilBlocker = registerBoolean("Anvil", true);
        pistonBlocker = registerBoolean("Piston", true);
        tickDelay = registerInteger("Tick Delay", 5, 0, 10);
        chatMsg = registerBoolean("Chat Msgs", true);
    }

    private int delayTimeTicks = 0;
    private boolean noObby;
    private boolean noActive;

    public void onEnable() {
        ROTATION_UTIL.onEnable();
        if (mc.player == null) {
            disable();
            return;
        }

        if (chatMsg.getValue()) {

            String output = "";

            if (anvilBlocker.getValue())
                output += "Anvil ";
            if (pistonBlocker.getValue())
                output += " Piston ";

            if (!output.equals("")) {
                noActive = false;
                MessageBus.sendClientPrefixMessage(ColorMain.getEnabledColor() + output +" turned ON!");
            }else {
                noActive = true;
                disable();
            }
        }
        noObby = false;
    }

    public void onDisable() {
        ROTATION_UTIL.onDisable();
        if (mc.player == null) {
            return;
        }
        if (chatMsg.getValue()) {
            if (noActive) {
                printChat("Nothing is active... Blocker turned OFF!", true);
            }else if(noObby)
                printChat("Obsidian not found... Blocker turned OFF!", true);
            else
                printChat("Blocker turned OFF!", true);
        }

    }

    public void onUpdate() {
        if (mc.player == null) {
            disable();
            return;
        }

        if (noObby) {
            disable();
            return;
        }

        if (delayTimeTicks < tickDelay.getValue()) {
            delayTimeTicks++;
            return;
        }
        else {
            ROTATION_UTIL.shouldSpoofAngles(true);
            delayTimeTicks = 0;

            if (anvilBlocker.getValue()) {
                blockAnvil();
            }
            if (pistonBlocker.getValue()) {
                blockPiston();
            }

        }

    }

    private void blockAnvil() {
        // Iterate for everything
        for (Entity t : mc.world.loadedEntityList) {
            // If it's a falling block
            if (t instanceof EntityFallingBlock) {
                Block ex = ((EntityFallingBlock) t).fallTile.getBlock();
                // If it's anvil
                if (ex instanceof BlockAnvil
                    // If coords are the same as us
                    && (int) t.posX == (int) mc.player.posX && (int) t.posZ == (int) mc.player.posZ
                    && BlockUtil.getBlock(mc.player.posX, mc.player.posY + 2, mc.player.posZ) instanceof BlockAir) {
                    // Place the block
                    placeBlock(new BlockPos(mc.player.posX, mc.player.posY + 2, mc.player.posZ));
                    printChat("AutoAnvil detected... Anvil Blocked!", false);
                }
            }
        }
    }

    private void blockPiston() {
        // Iterate for everything
        for (Entity t : mc.world.loadedEntityList) {
            // If it's an ecrystal and it's near us
            if (t instanceof EntityEnderCrystal
                && t.posX >= mc.player.posX - 1.5 && t.posX <= mc.player.posX + 1.5
                && t.posZ >= mc.player.posZ - 1.5 && t.posZ <= mc.player.posZ + 1.5) {
                // Check if it's near
                for(int i = -2; i < 3; i++) {
                    for(int j = -2; j < 3; j++) {
                        if (i == 0 || j == 0) {
                            // If it's a piston
                            if (BlockUtil.getBlock(t.posX + i, t.posY, t.posZ + j) instanceof BlockPistonBase) {
                                // Break
                                breakCrystalPiston(t);
                                printChat("PistonCrystal detected... Destroyed crystal!", false);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean placeBlock(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();

        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return false;
        }

        EnumFacing side = BlockUtil.getPlaceableSide(pos);

        if (side == null) {
            return false;
        }

        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();

        if (!BlockUtil.canBeClicked(neighbour)) {
            return false;
        }

        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();

        int obsidianSlot = InventoryUtil.findObsidianSlot();

        if (mc.player.inventory.currentItem != obsidianSlot && obsidianSlot != -1) {
            mc.player.inventory.currentItem = obsidianSlot;
        }

        if (obsidianSlot == -1) {
            noObby = true;
            return false;
        }

        boolean stoppedAC = false;

        if (ModuleManager.isModuleEnabled("AutoCrystalGS")) {
            AutoCrystalGS.stopAC = true;
            stoppedAC = true;
        }

        if (rotate.getValue()) {
            BlockUtil.faceVectorPacketInstant(hitVec);
        }

        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.rightClickDelayTimer = 4;

        if (stoppedAC) {
            AutoCrystalGS.stopAC = false;
            stoppedAC = false;
        }

        return true;
    }

    private void printChat(String text, Boolean error) {
        MessageBus.sendClientPrefixMessage((error ? ColorMain.getDisabledColor() : ColorMain.getEnabledColor()) + text);
    }

    private void breakCrystalPiston (Entity crystal) {
        // If rotate
        if (rotate.getValue()) {
            ROTATION_UTIL.lookAtPacket(crystal.posX, crystal.posY, crystal.posZ, mc.player);
        }
        CrystalUtil.breakCrystal(crystal);
        // Rotate
        if (rotate.getValue())
            ROTATION_UTIL.resetRotation();
    }
}