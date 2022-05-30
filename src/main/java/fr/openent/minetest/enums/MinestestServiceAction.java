package fr.openent.minetest.enums;

public enum MinestestServiceAction {
    CREATE("create"),
    DELETE("delete"),
    OPEN("open"),
    CLOSE("close");


    private final String action;

    MinestestServiceAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return this.action;
    }
}
