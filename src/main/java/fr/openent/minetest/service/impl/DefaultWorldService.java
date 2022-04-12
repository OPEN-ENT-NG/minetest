package fr.openent.minetest.service.impl;

import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.enums.MinestestServiceAction;
import fr.openent.minetest.service.MinetestService;
import fr.openent.minetest.service.ServiceFactory;
import fr.openent.minetest.service.WorldService;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.user.UserInfos;

import java.util.List;

public class DefaultWorldService implements WorldService {

    private final MinetestConfig minetestConfig;
    private final MinetestService minetestService;
    private final MongoDb mongoDb;
    private final String collection;
    private final Logger log = LoggerFactory.getLogger(DefaultWorldService.class);

    public DefaultWorldService(ServiceFactory serviceFactory,String collection, MongoDb mongo) {
        this.collection = collection;
        this.mongoDb = mongo;
        this.minetestConfig = serviceFactory.minetestConfig();
        this.minetestService = serviceFactory.minetestService();
    }


    /**
     * Create World
     *
     * @param userInfos User Object containing user id
     * @param body JsonObject containing the data for the world
     */
    @Override
    public Future<JsonObject> create(JsonObject body, UserInfos userInfos) {
        Promise<JsonObject> promise = Promise.promise();

        //add user Login
        body.put(Field.OWNER_LOGIN, userInfos.getLogin());

        //add link to minetest server
        body.put(Field.LINK, minetestConfig.minetestServer());

        //get New Port
        JsonObject sortByPort = new JsonObject().put(Field.PORT, 1);
        getMongo(null, null, null, null, null, null, null, sortByPort)
                .compose(this::getNewPort)
                .compose(res ->  {
                    int newPort = res;
                    body.put(Field.PORT, newPort);
                    return createMongo(body);
                })
                .compose(res ->  {
                    JsonObject sortByDate = new JsonObject().put(Field.CREATED_AT, -1);
                    return getMongo(userInfos.getUserId(), null,null,null,null,null,
                            null, sortByDate);
                })
                .compose(res -> {
                    JsonObject worldCreated = res.getJsonObject(0);
                    return minetestService.action(worldCreated, MinestestServiceAction.CREATE);
                })
                .onSuccess(promise::complete)
                .onFailure(err -> promise.fail(err.getMessage()));
        return promise.future();
    }

    private Future<Integer> getNewPort(JsonArray res) {
        Promise<Integer> promise = Promise.promise();

        Integer newPort = minetestConfig.minetestMinPort();

        for(Object world: res) {
            JsonObject worldJson = (JsonObject) world;
            int port = worldJson.getInteger(Field.PORT);
            if(port > newPort) {
                break;
            }
            else {
                newPort ++;
            }
        }
        if(newPort > minetestConfig.minetestMaxPort()) {
            String message = String.format("[Minetest@%s::createWorld]: The maximum port limit was reach. " +
                    "Please, contact the administrator to extend the port range.", this.getClass().getSimpleName());
            log.error(message);
            promise.fail(message);
        } else {
            promise.complete(newPort);
        }
        return promise.future();
    }

    @Override
    public Future<JsonObject> updateStatus(UserInfos user, JsonObject body) {
        Promise<JsonObject> promise = Promise.promise();

        JsonObject worldId = new JsonObject().put(Field._ID, body.getString(Field._ID));
        JsonObject status = new JsonObject().put("$set", new JsonObject().put("status", body.getBoolean(Field.STATUS)));

        updateStatusMongo(worldId, status)
                .compose(res -> {
                    JsonObject bodyToUpdateStatus = new JsonObject()
                            .put(Field.ID,body.getString(Field._ID))
                            .put(Field.PORT,body.getInteger(Field.PORT));
                    return minetestService.action(bodyToUpdateStatus,
                            body.getBoolean(Field.STATUS) ? MinestestServiceAction.OPEN : MinestestServiceAction.CLOSE);
                })
                .onSuccess(promise::complete)
                .onFailure(err -> promise.fail(err.getMessage()));
        return promise.future();
    }

    @Override
    public Future<JsonObject> update(UserInfos user, JsonObject body) {
        Promise<JsonObject> promise = Promise.promise();

        JsonObject worldId = new JsonObject().put(Field._ID, body.getValue(Field._ID));
        JsonObject world = new JsonObject().put("$set", new JsonObject()
                .put(Field.TITLE, body.getValue(Field.TITLE))
                .put(Field.IMG,body.getValue(Field.IMG))
                .put(Field.UPDATED_AT, body.getValue(Field.UPDATED_AT))
        );

        mongoDb.update(this.collection, worldId, world, MongoDbResult.validResultHandler(result -> {
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
    public Future<JsonObject> delete(UserInfos user, List<String> ids, List<String> ports) {
        Promise<JsonObject> promise = Promise.promise();
        JsonObject query = new JsonObject()
                .put(Field._ID, new JsonObject().put(Field.$IN, ids));

        deleteMongo(query)
                .compose(res -> {
                    JsonObject bodyToDelete = new JsonObject().put(Field.ID,ids.get(0)).put(Field.PORT,ports.get(0));
                    return minetestService.action(bodyToDelete, MinestestServiceAction.DELETE);
                })
                .onSuccess(promise::complete)
                .onFailure(err -> promise.fail(err.getMessage()));
        return promise.future();
    }

    @Override
    public Future<JsonArray> getMongo(String ownerId, String ownerName, String createdAt, String updatedAt,
                                 String img, String shared, String name, JsonObject sortJson) {
        Promise<JsonArray> promise = Promise.promise();

        JsonObject worldQuery = new JsonObject();

        if(ownerId != null) {
            worldQuery.put(Field.OWNER_ID, ownerId);
        }
        if(ownerName != null) {
            worldQuery.put(Field.OWNER_NAME, ownerName);
        }
        if(createdAt != null) {
            worldQuery.put(Field.CREATED_AT, createdAt);
        }
        if(updatedAt != null) {
            worldQuery.put(Field.UPDATED_AT, updatedAt);
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

        mongoDb.find(this.collection, worldQuery, sortJson, null, MongoDbResult.validResultsHandler(result -> {
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
    public Future<Void> createMongo(JsonObject body) {
        Promise<Void> promise = Promise.promise();

        mongoDb.insert(this.collection, body, MongoDbResult.validResultHandler(result -> {
            if(result.isLeft()) {
                String message = String.format("[Minetest@%s::createWorld]: An error has occurred while creating new world: %s",
                        this.getClass().getSimpleName(), result.left().getValue());
                log.error(message, result.left().getValue());
                promise.fail(message);
                return;
            }
            promise.complete();
        }));
        return promise.future();
    }

    @Override
    public Future<Void> deleteMongo(JsonObject query) {
        Promise<Void> promise = Promise.promise();

        mongoDb.delete(this.collection, query, MongoDbResult.validResultHandler(result -> {
            if(result.isLeft()) {
                String message = String.format("[Minetest@%s::deleteWorld]: An error has occurred while deleting new world: %s",
                        this.getClass().getSimpleName(), result.left().getValue());
                log.error(message, result.left().getValue());
                promise.fail(message);
                return;
            }
            promise.complete();
        }));
        return promise.future();
    }

    @Override
    public Future<JsonObject> updateStatusMongo(JsonObject worldId, JsonObject status) {
        Promise<JsonObject> promise = Promise.promise();

        mongoDb.update(this.collection, worldId, status, MongoDbResult.validResultHandler(result -> {
            if(result.isLeft()) {
                String message = String.format("[Minetest@%s::updateWorld]: An error has occurred while updating status: %s",
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
