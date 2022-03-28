package fr.openent.minetest.service.impl;

import fr.openent.minetest.config.MinetestConfig;
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
    private final MinetestConfig minetestConfig;

    public DefaultWorldService(String collection, MongoDb mongo, MinetestConfig minetestConfig) {
        this.collection = collection;
        this.mongoDb = mongo;
        this.minetestConfig = minetestConfig;
    }

    @Override
    public Future<JsonArray> get(String ownerId, String ownerName, String createdAt, String updatedAt,
                                 String img, String shared, String name) {
        Promise<JsonArray> promise = Promise.promise();

        JsonObject worldQuery = new JsonObject()
                .put(Field.OWNER_ID, ownerId)
                .put(Field.OWNER_NAME, ownerName);

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

    @Override
    public Future<JsonArray> getAll() {
        Promise<JsonArray> promise = Promise.promise();

        JsonObject sortByPort = new JsonObject().put("port", 1);

        mongoDb.find(this.collection, new JsonObject(), sortByPort, null, MongoDbResult.validResultsHandler(result -> {
            if(result.isLeft()) {
                String message = String.format("[Minetest@%s::getAll] An error has occured while finding worlds list: %s",
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
     * @param body JsonObject containing the data for the world
     */
    @Override
    public Future<JsonObject> create(JsonObject body, String fileId, String metadata) {
        Promise<JsonObject> promise = Promise.promise();

        if(fileId != null) {
            body.put("fileId", fileId);
        }
        if(metadata != null) {
            body.put("metadata", metadata);
        }

        //add link to minetest server
        body.put("link", minetestConfig.minetestServer());

        //get New Port
        getAll().onSuccess(res -> {
                    int newPort = getNewPort(res);
                    body.put("port",newPort);
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
        })
        .onFailure(err -> promise.fail(err.getMessage()));
        return promise.future();
    }

    private int getNewPort(JsonArray res) {
        int newPort = minetestConfig.minetestMinPort();
        for(Object world: res){
            JsonObject worldJson = (JsonObject) world;
            int port = worldJson.getInteger("port");
            if(port > newPort){
                break;
            }else{
                newPort ++;
            }
        }
        return newPort;
    }

    @Override
    public Future<JsonObject> update(UserInfos user, JsonObject body) {
        Promise<JsonObject> promise = Promise.promise();

        JsonObject worldId = new JsonObject().put(Field._ID, body.getValue(Field._ID));

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
                .put(Field._ID, new JsonObject().put(Field.$IN, ids));

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
