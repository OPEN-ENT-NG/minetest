package fr.openent.minetest.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserInfos;

import java.util.List;

public interface WorldService {
    /**
     * Get Worlds with filter
     *
     * @param createdAt Created date
     * @param updatedAt Updated date
     * @return FutureObject containing world {@link JsonObject}
     */
    Future<JsonArray> get(String ownerId, String ownerName, String createdAt, String updatedAt, String img, String shared,
                          String name);

    /**
     * Get All Worlds
     *
     * @return FutureObject containing world {@link JsonObject}
     */
    Future<JsonArray> getAll();

    /**
     * Create World
     *
     * @param body Data to store
     * @param fileId File identifier
     * @return Future {@link Future<JsonObject>} containing new world
     */
    Future<JsonObject> create(JsonObject body, String fileId, String metadata);

    /**
     * Update World
     *
     * @param user {@link UserInfos}
     * @param body Data to store
     * @return Future {@link Future<JsonObject>} containing new world
     */
    Future<JsonObject> update(UserInfos user, JsonObject body);

    /**
     * @param user User
     * @param ids  List world id to delete
     * @return returning data
     */
    Future<JsonObject> delete(UserInfos user, List<String> ids);
}
