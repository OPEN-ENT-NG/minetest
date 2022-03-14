package fr.openent.minetest.config;

import fr.openent.minetest.core.constants.Field;
import io.vertx.core.json.JsonObject;

public class MinetestConfig {

    private final String minetestDownload;
    private final String minetestServer;

    public MinetestConfig(JsonObject config) {
        this.minetestDownload = config.getString(Field.MINETEST_DOWNLOAD);
        this.minetestServer = config.getString(Field.MINETEST_SERVER);
    }

    public String minetestDownload() {
        return minetestDownload;
    }

    public String minetestServer() {
        return minetestServer;
    }

}
