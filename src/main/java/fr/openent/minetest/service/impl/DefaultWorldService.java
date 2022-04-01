package fr.openent.minetest.service.impl;

import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.enums.MinestestServiceAction;
import fr.openent.minetest.service.MinetestService;
import fr.openent.minetest.service.MongoService;
import fr.openent.minetest.service.ServiceFactory;
import fr.openent.minetest.service.WorldService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserInfos;

import java.util.List;

public class DefaultWorldService implements WorldService {

    private final MinetestConfig minetestConfig;
    private final MinetestService minetestService;
    private final MongoService mongoService;

    public DefaultWorldService(ServiceFactory serviceFactory) {
        this.minetestConfig = serviceFactory.minetestConfig();
        this.minetestService = serviceFactory.minetestService();
        this.mongoService = serviceFactory.mongoService();
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
        mongoService.get(null,null,null,null,null,null,null,sortByPort)
                .compose(res ->  {
                    int newPort = getNewPort(res);
                    body.put(Field.PORT, newPort);
                    return mongoService.create(body);
                })
                .compose(res ->  {
                    JsonObject sortByDate = new JsonObject().put(Field.CREATED_AT, -1);
                    return mongoService.get(userInfos.getUserId(), null,null,null,null,null,
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

    private int getNewPort(JsonArray res) {
        int newPort = minetestConfig.minetestMinPort();

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
        return newPort;
    }

    @Override
    public Future<JsonObject> updateStatus(UserInfos user, JsonObject body) {
        Promise<JsonObject> promise = Promise.promise();

        JsonObject worldId = new JsonObject().put(Field._ID, body.getString(Field._ID));
        JsonObject status = new JsonObject().put("$set", new JsonObject().put("status", body.getBoolean(Field.STATUS)));

        mongoService.updateStatus(worldId, status)
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
    public Future<JsonObject> delete(UserInfos user, List<String> ids, List<String> ports) {
        Promise<JsonObject> promise = Promise.promise();
        JsonObject query = new JsonObject()
                .put(Field._ID, new JsonObject().put(Field.$IN, ids));

        mongoService.delete(query)
                .compose(res -> {
                    JsonObject bodyToDelete = new JsonObject().put(Field.ID,ids.get(0)).put(Field.PORT,ports.get(0));
                    return minetestService.action(bodyToDelete, MinestestServiceAction.DELETE);
                })
                .onSuccess(promise::complete)
                .onFailure(err -> promise.fail(err.getMessage()));
        return promise.future();
    }
}
