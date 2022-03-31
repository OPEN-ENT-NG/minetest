package fr.openent.minetest.service;

import fr.openent.minetest.enums.MinestestServiceAction;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface MinetestService {

    /**
     * @param action action to do to the world : close, open, delete
     * @param body Data to store
     * @return Future {@link Future<JsonObject>} containing new world
     */
    Future<JsonObject> action(JsonObject body, MinestestServiceAction action);
}
