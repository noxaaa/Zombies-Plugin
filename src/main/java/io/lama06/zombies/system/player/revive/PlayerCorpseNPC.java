package io.lama06.zombies.system.player.revive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

public final class PlayerCorpseNPC {

    public static CorpseData createCorpse(final Player deadPlayer, final Location location) {
        final ArmorStand armorStand = createArmorStand(deadPlayer, location);

        return new CorpseData(
                armorStand,
                deadPlayer.getUniqueId(),
                PlayerCorpse.TIME,
                PlayerCorpse.REVIVE_TIME
        );
    }

    private static ArmorStand createArmorStand(final Player deadPlayer, final Location location) {
        // Adjust location to be on the ground
        final Location corpseLocation = location.clone();
        corpseLocation.setY(Math.floor(corpseLocation.getY()));

        final ArmorStand armorStand = location.getWorld().spawn(corpseLocation, ArmorStand.class);

        // Basic properties
        armorStand.setCanMove(false);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setCollidable(false);
        armorStand.setSilent(true);
        armorStand.setVisible(true);
        armorStand.setArms(true);
        armorStand.setBasePlate(false);
        armorStand.setSmall(false);

        // Rotate to lie flat (simulate lying down)
        // Rotate the head to look sideways
        armorStand.setHeadPose(new EulerAngle(Math.toRadians(90), 0, 0));
        armorStand.setBodyPose(new EulerAngle(Math.toRadians(90), 0, 0));
        armorStand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
        armorStand.setRightArmPose(new EulerAngle(Math.toRadians(-90), 0, 0));
        armorStand.setLeftLegPose(new EulerAngle(Math.toRadians(90), 0, 0));
        armorStand.setRightLegPose(new EulerAngle(Math.toRadians(90), 0, 0));

        // Set player head
        final EntityEquipment equipment = armorStand.getEquipment();
        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(deadPlayer);
        head.setItemMeta(headMeta);
        equipment.setHelmet(head);

        // Set leather armor to match player appearance
        equipment.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        equipment.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        equipment.setBoots(new ItemStack(Material.LEATHER_BOOTS));

        return armorStand;
    }

    public static void despawnCorpse(final CorpseData corpse) {
        final ArmorStand armorStand = corpse.getArmorStand();
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.remove();
        }
    }
}
