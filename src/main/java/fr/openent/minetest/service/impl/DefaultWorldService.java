package fr.openent.minetest.service.impl;

import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.service.WorldService;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.user.UserInfos;

import java.util.List;


public class DefaultWorldService implements WorldService {

    private final MongoDb mongoDb;
    private final String collection;
    private final Logger log = LoggerFactory.getLogger(DefaultWorldService.class);

    public DefaultWorldService(String collection, MongoDb mongo) {
        this.collection = collection;
        this.mongoDb = mongo;
    }

    @Override
    public Future<JsonArray> get(String ownerId, String ownerName, String createdAt, String updatedAt,
                                 String img, String shared, String name) {
        Promise<JsonArray> promise = Promise.promise();

        JsonObject worldQuery = new JsonObject()
                .put(Field.OWNER_ID, ownerId)
                .put(Field.OWNER_NAME, ownerName);

//        QueryBuilder query = QueryBuilder.start("")
        if(createdAt != null) {
            worldQuery.put(Field.CREATED_AT, createdAt);
        }
        if(updatedAt != null) {
            worldQuery.put(Field.UPDATE_AT, updatedAt);
        }
        if(img != null) {
            worldQuery.put(Field.IMG, img);
        }
        if(shared != null) {
            worldQuery.put(Field.SHARED, shared);
        }
        if(name != null) {
            worldQuery.put(Field.TITLE, name);
        }

        mongoDb.find(this.collection, worldQuery, MongoDbResult.validResultsHandler(result -> {
            if(result.isLeft()) {
                String message = String.format("[Minetest@%s::getWorlds] An error has occured while finding worlds list: %s",
                        this.getClass().getSimpleName(), result.left().getValue());
                log.error(message, result.left().getValue());
                promise.fail(message);
                return;
            }
            promise.complete(result.right().getValue());
        }));
        return promise.future();
    }

    /**
     * Create World
     *
     * @param user User Object containing user id
     * @param body JsonObject containing the data for the world
     */
    @Override
    public Future<JsonObject> create(UserInfos user, JsonObject body) {
        Promise<JsonObject> promise = Promise.promise();

        mongoDb.insert(this.collection, body, MongoDbResult.validResultHandler(result -> {
            if(result.isLeft()) {
                String message = String.format("[Minetest@%s::createWorld]: An error has occurred while creating new world: %s",
                        this.getClass().getSimpleName(), result.left().getValue());
                log.error(message, result.left().getValue());
                promise.fail(message);
                return;
            }
            promise.complete(result.right().getValue());
        }));
        return promise.future();
    }

    @Override
    public Future<JsonObject> update(UserInfos user, JsonObject body) {
        Promise<JsonObject> promise = Promise.promise();

        JsonObject worldId = new JsonObject().put("_id", body.getValue("_id"));

        mongoDb.update(this.collection, worldId, body, MongoDbResult.validResultHandler(result -> {
            if(result.isLeft()) {
                String message = String.format("[Minetest@%s::updateWorld]: An error has occurred while updating world: %s",
                        this.getClass().getSimpleName(), result.left().getValue());
                log.error(message, result.left().getValue());
                promise.fail(message);
                return;
            }
            promise.complete(result.right().getValue());
        }));
        return promise.future();
    }

    @Override
    public Future<JsonObject> delete(UserInfos user, List<String> ids) {
        Promise<JsonObject> promise = Promise.promise();
        JsonObject query = new JsonObject()
                .put("_id", new JsonObject().put("$in", ids));

        mongoDb.delete(this.collection, query, MongoDbResult.validResultHandler(result -> {
            if(result.isLeft()) {
                String message = String.format("[Minetest@%s::deleteWorld]: An error has occurred while deleting world: %s",
                        this.getClass().getSimpleName(), result.left().getValue());
                log.error(message, result.left().getValue());
                promise.fail(message);
                return;
            }
            promise.complete(result.right().getValue());
        }));
        return promise.future();
    }
}
