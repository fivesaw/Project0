package com.legitclient.module;

public abstract class Module {

    public enum Category {
        COMBAT, MOVEMENT, VISUAL, UTILITY
    }

    private final String name;
    private final Category category;
    private boolean enabled;
    private int keybind;

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
        this.enabled = false;
        this.keybind = -1;
    }

    public void toggle() {
        if (enabled) {
            enabled = false;
            onDisable();
        } else {
            enabled = true;
            onEnable();
        }
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getKeybind() {
        return keybind;
    }

    public void setKeybind(int keybind) {
        this.keybind = keybind;
    }
}
