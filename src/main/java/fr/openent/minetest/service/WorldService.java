package fr.openent.minetest.service;

import io.netty.handler.codec.http.HttpRequest;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
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
     * Import World
     *
     * @param body Data to store
     * @param user {@link UserInfos}
     * @return Future {@link Future<JsonObject>} containing new world
     */
    Future<JsonObject> importWorld(JsonObject body, UserInfos user);

    /**
     * Update World
     *
     * @param body Data to store
     * @param worldId World identifier
     * @return Future {@link Future<JsonObject>} containing new world
     */
    Future<JsonObject> update(String worldId, JsonObject body);

    /**
     * Update Status
     *
     * @param user {@link UserInfos}
     * @param body Data to store
     * @return Future {@link Future<JsonObject>} containing new world
     */
    Future<JsonObject> updateStatus(UserInfos user, JsonObject body);

    /**
     * Update whitelist of a world
     *
     * @param user {@link UserInfos}
     * @param body Data to update
     * @param request request
     * @return Future {@link Future<JsonObject>} containing array of invitees
     */
    Future<JsonObject> join(UserInfos user, JsonObject body, HttpServerRequest request);

    /**
     * Delete World
     * @param user User
     * @param ids  List world id to delete
     * @param ports  List world port to delete
     * @return returning data
     */
    Future<JsonObject> delete(UserInfos user, List<String> ids, List<String> ports);

    /**
     * Delete Import World
     * @param user User
     * @param ids  List world id to delete
     * @return returning data
     */
    Future<JsonObject> deleteImportWorld(UserInfos user, List<String> ids);

    /**
     * @param newPassword String
     * @param body Data of the world to change password
     * @return returning data
     */
    Future<JsonObject> resetPassword(String newPassword, JsonObject body);

    /**
     * Get Worlds with filter
     *
     * @param createdAt Created date
     * @param updatedAt Updated date
     * @return FutureObject containing world {@link JsonObject}
     */
    Future<JsonArray> getMongo(String ownerId, String ownerName, String createdAt, String updatedAt, String img, String name,
                               JsonObject sortJson);

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
