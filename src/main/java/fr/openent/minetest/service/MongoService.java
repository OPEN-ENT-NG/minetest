package fr.openent.minetest.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserInfos;

import java.util.List;

public interface MongoService {
    /**
     * Get Worlds with filter
     *
     * @param createdAt Created date
     * @param updatedAt Updated date
     * @return FutureObject containing world {@link JsonObject}
     */
    Future<JsonArray> get(String ownerId, String ownerName, String createdAt, String updatedAt, String img, String shared,
                          String name, JsonObject sortJson);

    /**
     * create world in Mongo
     * @param body body to create in Mongo
     * @return Future<Void> {@link Void}
     */
    Future<Void> create(JsonObject body);

    /**
     * delete world in Mongo
     * @param query query to delete in Mongo
     * @return Future<Void> {@link Void}
     */
    Future<Void> delete(JsonObject query);

    /**
     * update world in Mongo
     * @param worldId id of the world to update in Mongo
     * @param status id of the world to update in Mongo
     * @return Future<JsonObject> {@link JsonObject}
     */
    Future<JsonObject> updateStatus(JsonObject worldId, JsonObject status);
}
