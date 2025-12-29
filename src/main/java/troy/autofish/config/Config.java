package troy.autofish.config;

import com.google.gson.annotations.Expose;

public class Config {

    @Expose boolean isAutofishEnabled = true;
    @Expose boolean multiRod = false;
    @Expose boolean isOpenWaterDetectEnabled = true;
    @Expose boolean noBreak = false;
    @Expose boolean persistentMode = false;
    @Expose boolean useSoundDetection = false;
    @Expose boolean forceMPDetection = false;
    @Expose boolean autoTurnView = false;
    @Expose boolean disableInInventory = true;
    @Expose float turnAngle = 30.0f;
    @Expose int turnDuration = 500;
    @Expose long recastDelay = 1500;
    @Expose long randomPercent = 50;
    @Expose long reelInDelay = 1;
    @Expose String clearLagRegex = "\\[ClearLag\\] Removed [0-9]+ Entities!";

    public boolean isAutofishEnabled() {
        return isAutofishEnabled;
    }

    public boolean isOpenWaterDetectEnabled() {
        return isOpenWaterDetectEnabled;
    }

    public boolean isMultiRod() {
        return multiRod;
    }

    public boolean isNoBreak() {
        return noBreak;
    }

    public boolean isPersistentMode() { return persistentMode; }

    public boolean isUseSoundDetection() {
        return useSoundDetection;
    }

    public boolean isForceMPDetection() { return forceMPDetection; }

    public boolean isAutoTurnView() {
        return autoTurnView;
    }

    public void setAutoTurnView(boolean autoTurnView) {
        this.autoTurnView = autoTurnView;
    }

    public boolean isDisableInInventory() {
        return disableInInventory;
    }

    public void setDisableInInventory(boolean disableInInventory) {
        this.disableInInventory = disableInInventory;
    }

    public float getTurnAngle() {
        return turnAngle;
    }

    public void setTurnAngle(float turnAngle) {
        this.turnAngle = turnAngle;
    }

    public int getTurnDuration() {
        return turnDuration;
    }

    public void setTurnDuration(int turnDuration) {
        this.turnDuration = turnDuration;
    }

    public long getRecastDelay() {
        return recastDelay;
    }

    public long getRandomDelay(){
        return randomPercent;
    }

    public String getClearLagRegex() {
        return clearLagRegex;
    }

    public void setAutofishEnabled(boolean autofishEnabled) { isAutofishEnabled = autofishEnabled; }

    public void setMultiRod(boolean multiRod) {
        this.multiRod = multiRod;
    }

    public void setNoBreak(boolean noBreak) {
        this.noBreak = noBreak;
    }

    public void setPersistentMode(boolean persistentMode) { this.persistentMode = persistentMode; }

    public void setUseSoundDetection(boolean useSoundDetection) {
        this.useSoundDetection = useSoundDetection;
    }

    public void setForceMPDetection(boolean forceMPDetection) { this.forceMPDetection = forceMPDetection; }

    public void setRecastDelay(long recastDelay) {
        this.recastDelay = recastDelay;
    }
    public void setRandomDelay(long randomPercent){
        this.randomPercent = randomPercent;
    }

    public void setClearLagRegex(String clearLagRegex) {
        this.clearLagRegex = clearLagRegex;
    }

    public void setOpenWaterDetectEnabled(boolean openWaterDetectEnabled) {
        isOpenWaterDetectEnabled = openWaterDetectEnabled;
    }

    public long getRandomPercent() {
        return randomPercent;
    }

    public void setRandomPercent(long randomPercent) {
        this.randomPercent = randomPercent;
    }

    public long getReelInDelay() {
        return reelInDelay;
    }

    public void setReelInDelay(long reelInDelay) {
        this.reelInDelay = reelInDelay;
    }

    /**
     * @return true if anything was changed
     */
    public boolean enforceConstraints() {
        boolean changed = false;
        if (recastDelay < 500) {
            recastDelay = 500;
            changed = true;
        }
        if (clearLagRegex == null) {
            clearLagRegex = "";
            changed = true;
        }
        return changed;
    }
}
