package fr.openent.minetest.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserInfos;

import java.util.List;

public interface WorldService {

    /**
     * Create World
     *
     * @param body Data to store
     * @param user infos of the user
     * @return Future {@link Future<JsonObject>} containing new world
     */
    Future<JsonObject> create(JsonObject body, UserInfos user);

    /**
     * Update World
     *
     * @param user {@link UserInfos}
     * @param body Data to store
     * @return Future {@link Future<JsonObject>} containing new world
     */
    Future<JsonObject> updateStatus(UserInfos user, JsonObject body);

    /**
     * @param user User
     * @param ids  List world id to delete
     * @param ports  List world port to delete
     * @return returning data
     */
    Future<JsonObject> delete(UserInfos user, List<String> ids, List<String> ports);

}
