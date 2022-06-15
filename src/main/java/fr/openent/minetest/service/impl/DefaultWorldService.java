package fr.openent.minetest.service.impl;

import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.enums.MinestestServiceAction;
import fr.openent.minetest.service.MinetestService;
import fr.openent.minetest.service.ServiceFactory;
import fr.openent.minetest.service.WorldService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.I18n;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static fr.wseduc.webutils.http.Renders.getHost;

public class DefaultWorldService implements WorldService {

    private final MinetestConfig minetestConfig;
    private final MinetestService minetestService;
    private final MongoDb mongoDb;
    private final String collection;
    private final Logger log = LoggerFactory.getLogger(DefaultWorldService.class);
    private final EventBus eb;

    public DefaultWorldService(ServiceFactory serviceFactory, String collection, MongoDb mongo) {
        this.collection = collection;
        this.mongoDb = mongo;
        this.minetestConfig = serviceFactory.minetestConfig();
        this.minetestService = serviceFactory.minetestService();
        this.eb = serviceFactory.eventBus();
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

        String loginMinetest = reformatLogin(userInfos.getLogin());

        //add user Login
        body.put(Field.OWNER_LOGIN, loginMinetest);

        //add loginMinetest to whitelist
        body.put(Field.WHITELIST, new JsonArray().add(loginMinetest));

        //add link to minetest server
        body.put(Field.LINK, minetestConfig.minetestServer());

        //get New Port
        JsonObject sortByPort = new JsonObject().put(Field.PORT, 1);
        getMongo(null, null, null, null, null, null, sortByPort)
                .compose(this::getNewPort)
                .compose(res ->  {
                    int newPort = res;
                    body.put(Field.PORT, newPort);
                    body.put(Field._ID, UUID.randomUUID().toString());
                    return createMongo(body);
                })
                .compose(res -> minetestService.action(body, MinestestServiceAction.CREATE))
                .onSuccess(promise::complete)
                .onFailure(err -> promise.fail(err.getMessage()));
        return promise.future();
    }

    private String reformatLogin(String login) {
        //limit the player_name to 20 characters and replace all disallow characters
        return login.substring(0, Math.min(login.length(), 19))
                .replace(".","_")
                .replaceAll("[^a-zA-Z\\d_-]", "");
    }

    private Future<Integer> getNewPort(JsonArray res) {
        Promise<Integer> promise = Promise.promise();

        Integer newPort = minetestConfig.minetestMinPort();

        for (Object world: res) {
            JsonObject worldJson = (JsonObject) world;
            if (Boolean.TRUE.equals(worldJson.getBoolean(Field.ISEXTERNAL))) {
                if (!worldJson.getString(Field.PORT).isEmpty()) {
                    promise.complete(Integer.parseInt(worldJson.getString(Field.PORT)));
                }
                return promise.future();
            }
            int port = worldJson.getInteger(Field.PORT);
            if (port > newPort) {
                break;
            }
            else {
                newPort ++;
            }
        }
        if (newPort > minetestConfig.minetestMaxPort()) {
            String message = String.format("[Minetest@%s::createWorld]: The maximum port limit was reach. " +
                    "Please, contact the administrator to extend the port range.", this.getClass().getSimpleName());
            log.error(message);
            promise.fail(message);
        } else {
            promise.complete(newPort);
        }
        return promise.future();
    }

    /**
     * Import World
     *
     * @param body JsonObject containing the data for the world
     * @param user User Object containing user id
     */
    @Override
    public Future<JsonObject> importWorld(JsonObject body, UserInfos user) {
        Promise<JsonObject> promise = Promise.promise();

        mongoDb.insert(this.collection, body, MongoDbResult.validResultHandler(result -> {
            if (result.isLeft()) {
                String message = String.format("[Minetest@%s::importWorld]: An error has occurred while importing world: %s",
                        this.getClass().getSimpleName(), result.left().getValue());
                log.error(message, result.left().getValue());
                promise.fail(message);
                return;
            }
            promise.complete();
        }));
        return promise.future();
    }

    /**
     * Update World status
     *
     * @param body JsonObject containing the data for the world
     * @param user User Object containing user id
     */
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
                            Boolean.TRUE.equals(body.getBoolean(Field.STATUS)) ?
                                    MinestestServiceAction.OPEN : MinestestServiceAction.CLOSE);
                })
                .onSuccess(promise::complete)
                .onFailure(err -> promise.fail(err.getMessage()));
        return promise.future();
    }

    /**
     * Update World whitelist
     *
     * @param body JsonObject containing the data for the world
     * @param user User Object containing user id
     */
    @Override
    public Future<JsonObject> join(UserInfos user, JsonObject body, HttpServerRequest request) {
        Promise<JsonObject> promise = Promise.promise();
        JsonArray whitelistMinetest = new JsonArray();
        JsonArray whitelistDetails = new JsonArray();
        String password = (String) body.remove(Field.PASSWORD);

        reformatWhitelist(body.getJsonArray(Field.WHITELIST), user, whitelistMinetest, whitelistDetails)
                .compose(res -> {
                    body.put(Field.WHITELIST, whitelistDetails);
                    return update(body.getString(Field._ID), body);
                })
                .compose(res -> {
                    StringBuilder whitelistInFile = new StringBuilder();
                    for (Object login : whitelistMinetest){
                        whitelistInFile.append(login).append("\n");
                    }
                    body.put(Field.WHITELIST, whitelistInFile.toString());
                    body.put(Field.PASSWORD, password);
                    body.put(Field.ID, body.getString(Field._ID));
                    return minetestService.action(body,MinestestServiceAction.WHITELIST);
                })
                .compose(res -> sendMail(user,whitelistDetails, body, request, password))
                .onSuccess(promise::complete)
                .onFailure(err -> promise.fail(err.getMessage()));
        return promise.future();
    }

    private Future<JsonArray> reformatWhitelist(JsonArray whitelist, UserInfos owner,
                                                JsonArray whitelistMinetest, JsonArray whitelistDetails) {
        Promise<JsonArray> promise = Promise.promise();
        for (Object u : whitelist){
            JsonObject userInfos = (JsonObject) u;
            UserUtils.getUserInfos(eb, userInfos.getString(Field.ID), user -> {
                String loginToInsert = reformatLogin(user.getLogin());
                checkDuplicates(whitelistDetails, whitelistMinetest, user, loginToInsert);
                if (whitelistMinetest.size() == whitelist.size()) {
                    //insert the owner in the whitelist
                    loginToInsert = reformatLogin(owner.getLogin());
                    checkDuplicates(whitelistDetails, whitelistMinetest, owner, loginToInsert);
                    promise.complete(new JsonArray());
                }
            });
        }
        return promise.future();
    }

    private void checkDuplicates(JsonArray whitelistDetails, JsonArray whitelistMinetest, UserInfos userInfos,
                                 String loginToInsert) {
        //Check duplicate login
        int i = 1;
        while (whitelistMinetest.contains(loginToInsert)){
            Character lastCharacter = loginToInsert.charAt(loginToInsert.length() - 1);
            if (Character.isDigit(lastCharacter)) {
                i = Integer.parseInt(String.valueOf(lastCharacter)) + 1;
            }
            loginToInsert = loginToInsert.substring(0,Math.min(loginToInsert.length(), 19) - 1) + i;
        }
        whitelistMinetest.add(loginToInsert);
        JsonObject userToInsert = new JsonObject()
                .put(Field.ID, userInfos.getUserId())
                .put(Field.LOGIN, loginToInsert)
                .put(Field.DISPLAY_NAME, userInfos.getUsername())
                .put(Field.FIRST_NAME, userInfos.getFirstName())
                .put(Field.LAST_NAME, userInfos.getLastName());
        whitelistDetails.add(userToInsert);
    }

    private Future<JsonObject> sendMail(UserInfos owner, JsonArray whitelistIdAndLogin, JsonObject body,
                                        HttpServerRequest request, String password) {
        Promise<JsonObject> promise = Promise.promise();
        JsonArray listMails = createMailList(owner, whitelistIdAndLogin, body, request, password);
        // Prepare futures to get message responses
        List<Future> mails = new ArrayList<>();
        // Code to send mails
        for (int i = 0; i < listMails.size(); i++) {
            Promise<JsonObject> future = Promise.promise();
            mails.add(future.future());
            // Send mail via Conversation app if it exists or else with Zimbra
            eb.request("org.entcore.conversation", listMails.getJsonObject(i),
                    (Handler<AsyncResult<Message<JsonObject>>>) messageEvent -> {
                        if (!"ok".equals(messageEvent.result().body().getString("status"))) {
                            log.error("[Minetest@sendMail] Failed to send mail : " + messageEvent.cause());
                            future.handle(Future.failedFuture(messageEvent.cause()));
                        } else {
                            future.handle(Future.succeededFuture(messageEvent.result().body()));
                        }
                    });
        }
        // Try to send effectively mails with code below and get results
        CompositeFuture.all(mails)
                .onSuccess(success -> promise.complete(new JsonObject()))
                .onFailure(fail -> {
                    log.error("[Minetest@sendMail] Failed to send mail : " + fail.getCause());
                    promise.fail(fail.getCause());
                });
        return promise.future();
    }

    private JsonArray createMailList(UserInfos owner, JsonArray whitelistIdAndLogin, JsonObject body,
                                     HttpServerRequest request, String password) {
        JsonArray listMails = new JsonArray();
        I18n i18n = I18n.getInstance();
        String host = getHost(request);
        String acceptLanguage = I18n.acceptLanguage(request);
        String path = this.minetestConfig.minetestServer()
                .replace("http://","").replace("https://","");
        // Generate list of mails to send
        for (Object u : whitelistIdAndLogin) {
            JsonObject user = (JsonObject) u;
            String mailBody = i18n.translate("minetest.invitation.default.body.1", host, acceptLanguage)
                    .replace("<mettre lien>", this.minetestConfig.minetestDownload()) +
                    i18n.translate("minetest.invitation.default.body.address", host, acceptLanguage) + path +
                    i18n.translate("minetest.invitation.default.body.port", host, acceptLanguage) + body.getString(Field.PORT) +
                    i18n.translate("minetest.invitation.default.body.name", host, acceptLanguage) + user.getString(Field.LOGIN) +
                    i18n.translate("minetest.invitation.default.body.password", host, acceptLanguage) + password +
                    i18n.translate("minetest.invitation.default.body.end", host, acceptLanguage);

            JsonObject message = new JsonObject()
                    .put("subject", body.getString(Field.SUBJECT))
                    .put("body", mailBody)
                    .put("to", new JsonArray().add(user.getString(Field.ID)))
                    .put("cci", new JsonArray());

            JsonObject action = new JsonObject()
                    .put("action", "send")
                    .put("userId", owner.getUserId())
                    .put("username", owner.getUsername())
                    .put("message", message);
            listMails.add(action);
        }
        return listMails;
    }

    /**
     * Update World
     *
     * @param body JsonObject containing the data for the world
     */
    @Override
    public Future<JsonObject> update(String worldId, JsonObject body) {
        Promise<JsonObject> promise = Promise.promise();

        JsonObject worldQuery = new JsonObject().put(Field._ID, worldId);
        JsonObject worldData = new JsonObject();

        if (body.getString(Field.TITLE) != null) {
            worldData.put(Field.TITLE, body.getString(Field.TITLE));
        }

        if (body.getString(Field.UPDATED_AT) != null) {
            worldData.put(Field.UPDATED_AT, body.getString(Field.UPDATED_AT));
        }

        if (body.getValue(Field.IMG) != null) {
            worldData.put(Field.IMG, body.getValue(Field.IMG));
        }

        if (body.getString(Field.PASSWORD) != null) {
            worldData.put(Field.PASSWORD, body.getString(Field.PASSWORD));
        }

        if (body.getJsonArray(Field.WHITELIST) != null) {
            worldData.put(Field.WHITELIST, body.getJsonArray(Field.WHITELIST));
        }

        if (body.getBoolean(Field.ISEXTERNAL) != null && Boolean.TRUE.equals(body.getBoolean(Field.ISEXTERNAL))) {
            if (body.getString(Field.ADDRESS) != null) {
                worldData.put(Field.ADDRESS, body.getString(Field.ADDRESS));
            }
            if (body.getString(Field.PORT) != null) {
                worldData.put(Field.PORT, body.getString(Field.PORT));
            }
        }

        JsonObject world = new JsonObject().put("$set", worldData);

        mongoDb.update(this.collection, worldQuery, world, MongoDbResult.validResultHandler(result -> {
            if (result.isLeft()) {
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

    /**
     * Delete Import World
     *
     * @param ids List world's ids to delete
     * @param user User Object containing user id
     */
    @Override
    public Future<JsonObject> deleteImportWorld(UserInfos user, List<String> ids) {
        Promise<JsonObject> promise = Promise.promise();
        JsonObject query = new JsonObject()
                .put(Field._ID, new JsonObject().put(Field.$IN, ids));

        mongoDb.delete(this.collection, query, MongoDbResult.validResultHandler(result -> {
            if (result.isLeft()) {
                String message = String.format("[Minetest@%s::deleteImportWorld]: An error has occurred while deleting import world: %s",
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
    public Future<JsonObject> resetPassword(String newPassword, JsonObject body) {
        Promise<JsonObject> promise = Promise.promise();

        body.put(Field.ID,body.getString(Field._ID)).put(Field.PASSWORD, newPassword);

        minetestService.action(body, MinestestServiceAction.RESET_PASSWORD)
                .onSuccess(promise::complete)
                .onFailure(err -> promise.fail(err.getMessage()));

        return promise.future();
    }

    @Override
    public Future<JsonArray> getMongo(String ownerId, String ownerName, String createdAt, String updatedAt,
                                      String img, String name, JsonObject sortJson) {
        Promise<JsonArray> promise = Promise.promise();

        JsonObject worldQuery = new JsonObject();

        if (ownerId != null) {
            worldQuery.put(Field.OWNER_ID, ownerId);
        }
        if (ownerName != null) {
            worldQuery.put(Field.OWNER_NAME, ownerName);
        }
        if (createdAt != null) {
            worldQuery.put(Field.CREATED_AT, createdAt);
        }
        if (updatedAt != null) {
            worldQuery.put(Field.UPDATED_AT, updatedAt);
        }
        if (img != null) {
            worldQuery.put(Field.IMG, img);
        }
        if (name != null) {
            worldQuery.put(Field.TITLE, name);
        }

        mongoDb.find(this.collection, worldQuery, sortJson, null, MongoDbResult.validResultsHandler(result -> {
            if (result.isLeft()) {
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
            if (result.isLeft()) {
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
            if (result.isLeft()) {
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
            if (result.isLeft()) {
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
