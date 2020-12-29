package com.gamesense.client.module.modules.render;

import com.gamesense.api.event.events.RenderEntityHeadEvent;
import com.gamesense.api.event.events.RenderEntityReturnEvent;
import com.gamesense.api.setting.Setting;
import com.gamesense.api.util.render.GSColor;
import com.gamesense.api.util.render.GameSenseTessellator;
import com.gamesense.client.GameSense;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.*;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;

/**
 * @author Techale
 * @author Hoosiers
 */

public class Chams extends Module {

    public Chams() {
        super("Chams", Category.Render);
    }

    Setting.Mode chamsType;
    Setting.ColorSetting playerColor;
    Setting.ColorSetting mobColor;
    Setting.ColorSetting entityColor;
    Setting.ColorSetting itemColor;
    Setting.Integer colorOpacity;
    Setting.Integer range;
    Setting.Boolean player;
    Setting.Boolean mob;
    Setting.Boolean entity;
    Setting.Boolean item;

    public void setup() {
        ArrayList<String> chamsTypes = new ArrayList<>();
        chamsTypes.add("Texture");
        chamsTypes.add("Color");

        chamsType = registerMode("Type", "Type", chamsTypes, "Texture");
        range = registerInteger("Range", "Range", 100, 10, 260);
        player = registerBoolean("Player", "Player", true);
        mob = registerBoolean("Mob", "Mob", false);
        entity = registerBoolean("Entity", "Entity", false);
        item = registerBoolean("Item", "Item", false);
        colorOpacity = registerInteger("Opacity", "Opacity", 155, 10, 255);
        playerColor = registerColor("Player Color", "PlayerColor", new GSColor(0, 255, 255, 255));
        mobColor = registerColor("Mob Color", "Mob Color", new GSColor(255, 255, 0, 255));
        entityColor = registerColor("Entity Color", "EntityColor", new GSColor(0, 255, 0, 255));
        itemColor = registerColor("Item Color", "ItemColor", new GSColor(255, 0, 255, 255));
    }

    @EventHandler
    private final Listener<RenderEntityHeadEvent> renderEntityHeadEventListener = new Listener<>(event -> {
        if (mc.player == null || mc.world == null) {
            return;
        }

        Entity entity1 = event.getEntity();

        if (entity1.getDistance(mc.player) > range.getValue()) {
            return;
        }

        if (player.getValue() && entity1 instanceof EntityPlayer && entity1 != mc.player) {
            renderChamsPre(new GSColor(playerColor.getValue(), colorOpacity.getValue()));
        }

        if (mob.getValue() && (entity1 instanceof EntityCreature || entity1 instanceof EntitySlime || entity1 instanceof EntitySquid)) {
            renderChamsPre(new GSColor(mobColor.getValue(), colorOpacity.getValue()));
        }

        if (entity.getValue() && (entity1 instanceof EntityEnderPearl || entity1 instanceof EntityXPOrb || entity1 instanceof EntityExpBottle || entity1 instanceof EntityEnderCrystal)) {
            renderChamsPre(new GSColor(entityColor.getValue(), colorOpacity.getValue()));
        }

        if (item.getValue() && entity1 instanceof EntityItem) {
            renderChamsPre(new GSColor(itemColor.getValue(), colorOpacity.getValue()));
        }
    });

    @EventHandler
    private final Listener<RenderEntityReturnEvent> renderEntityReturnEventListener = new Listener<>(event -> {
        if (mc.player == null || mc.world == null) {
            return;
        }

        Entity entity1 = event.getEntity();

        if (entity1.getDistance(mc.player) > range.getValue()) {
            return;
        }

        if (player.getValue() && entity1 instanceof EntityPlayer && entity1 != mc.player) {
            renderChamsPost();
        }

        if (mob.getValue() && (entity1 instanceof EntityCreature || entity1 instanceof EntitySlime || entity1 instanceof EntitySquid)) {
            renderChamsPost();
        }

        if (entity.getValue() && (entity1 instanceof EntityEnderPearl || entity1 instanceof EntityXPOrb || entity1 instanceof EntityExpBottle || entity1 instanceof EntityEnderCrystal)) {
            renderChamsPost();
        }

        if (item.getValue() && entity1 instanceof EntityItem) {
            renderChamsPost();
        }
    });

    private void renderChamsPre(GSColor color) {
        switch (chamsType.getValue()) {
            case "Texture":
                GameSenseTessellator.createChamsPre();
                break;
            case "Color":
                GameSenseTessellator.createColorPre(color);
                break;
        }
    }

    private void renderChamsPost() {
        switch (chamsType.getValue()) {
            case "Color":
            case "Texture":
                GameSenseTessellator.createChamsPost();
                break;
        }
    }

    public void onEnable() {
        GameSense.EVENT_BUS.subscribe(this);
    }

    public void onDisable() {
        GameSense.EVENT_BUS.unsubscribe(this);
    }
}