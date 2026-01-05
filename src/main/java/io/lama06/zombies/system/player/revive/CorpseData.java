package io.lama06.zombies.system.player.revive;

import org.bukkit.entity.ArmorStand;

import java.util.UUID;

public final class CorpseData {
    private final ArmorStand armorStand;
    private final UUID deadPlayerUUID;
    private int remainingTime;
    private int reviveProgress;
    private UUID reviverUUID;
    private boolean reviveByClick;

    public CorpseData(
            final ArmorStand armorStand,
            final UUID deadPlayerUUID,
            final int remainingTime,
            final int reviveProgress
    ) {
        this.armorStand = armorStand;
        this.deadPlayerUUID = deadPlayerUUID;
        this.remainingTime = remainingTime;
        this.reviveProgress = reviveProgress;
        this.reviverUUID = null;
        this.reviveByClick = false;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public UUID getDeadPlayerUUID() {
        return deadPlayerUUID;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(final int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void decrementRemainingTime() {
        this.remainingTime--;
    }

    public int getReviveProgress() {
        return reviveProgress;
    }

    public void setReviveProgress(final int reviveProgress) {
        this.reviveProgress = reviveProgress;
    }

    public void decrementReviveProgress(final int amount) {
        this.reviveProgress -= amount;
    }

    public UUID getReviverUUID() {
        return reviverUUID;
    }

    public void setReviverUUID(final UUID reviverUUID) {
        this.reviverUUID = reviverUUID;
    }

    public boolean isReviveByClick() {
        return reviveByClick;
    }

    public void setReviveByClick(final boolean reviveByClick) {
        this.reviveByClick = reviveByClick;
    }

    public boolean hasReviver() {
        return reviverUUID != null;
    }

    public void clearReviver() {
        this.reviverUUID = null;
        this.reviveByClick = false;
    }
}
