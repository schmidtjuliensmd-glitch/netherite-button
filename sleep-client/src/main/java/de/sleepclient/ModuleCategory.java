package de.sleepclient;

public enum ModuleCategory {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    DONUT_SMP("DonutSMP"),
    VISUALS("Visuals"),
    MISC("Misc"),
    GUI("GUI");

    private final String displayName;
    ModuleCategory(String displayName) { this.displayName = displayName; }
    public String displayName() { return displayName; }
}
