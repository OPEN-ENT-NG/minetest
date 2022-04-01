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

    /**
     * Get Worlds with filter
     *
     * @param createdAt Created date
     * @param updatedAt Updated date
     * @return FutureObject containing world {@link JsonObject}
     */
    Future<JsonArray> getMongo(String ownerId, String ownerName, String createdAt, String updatedAt, String img, String shared,
                          String name, JsonObject sortJson);

    /**
     * create world in Mongo
     * @param body body to create in Mongo
     * @return Future<Void> {@link Void}
     */
    Future<Void> createMongo(JsonObject body);

    /**
     * delete world in Mongo
     * @param query query to delete in Mongo
     * @return Future<Void> {@link Void}
     */
    Future<Void> deleteMongo(JsonObject query);

    /**
     * update world in Mongo
     * @param worldId id of the world to update in Mongo
     * @param status id of the world to update in Mongo
     * @return Future<JsonObject> {@link JsonObject}
     */
    Future<JsonObject> updateStatusMongo(JsonObject worldId, JsonObject status);
}
