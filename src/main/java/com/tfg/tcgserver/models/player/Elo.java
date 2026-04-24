package com.tfg.tcgserver.models.player;

public class Elo {

    private String name;
    private int mmrRequirement;
    private String icon;

    public Elo() {
    }

    public Elo(String name, int mmrRequirement, String icon) {
        this.name = name;
        this.mmrRequirement = mmrRequirement;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMmrRequirement() {
        return mmrRequirement;
    }

    public void setMmrRequirement(int mmrRequirement) {
        this.mmrRequirement = mmrRequirement;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
