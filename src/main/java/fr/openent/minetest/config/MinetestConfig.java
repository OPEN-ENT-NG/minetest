package fr.openent.minetest.config;

import fr.openent.minetest.core.constants.Field;
import io.vertx.core.json.JsonObject;

public class MinetestConfig {

    private final String minetestDownload;
    private final String minetestServer;
    private final String minetestPythonServerPort;
    private final Integer minetestMinPort;
    private final Integer minetestMaxPort;

    public MinetestConfig(JsonObject config) {
        this.minetestDownload = config.getString(Field.MINETEST_DOWNLOAD);
        this.minetestServer = config.getString(Field.MINETEST_SERVER);
        this.minetestMinPort = Integer.parseInt(config.getString(Field.MINETEST_PORT_RANGE).split("-")[0]);
        this.minetestMaxPort = Integer.parseInt(config.getString(Field.MINETEST_PORT_RANGE).split("-")[1]);
        this.minetestPythonServerPort = config.getString(Field.MINETEST_PYTHON_SERVER_PORT);
    }

    public String minetestDownload() {
        return minetestDownload;
    }

    public String minetestServer() { return minetestServer; }

    public String minetestPythonServerPort() { return minetestPythonServerPort; }

    public Integer minetestMinPort() {
        return minetestMinPort;
    }

    public Integer minetestMaxPort() {
        return minetestMaxPort;
    }
}
