package fr.openent.minetest.service.impl;

import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.enums.MinestestServiceAction;
import fr.openent.minetest.service.MinetestService;
import fr.openent.minetest.service.ServiceFactory;
import fr.openent.minetest.service.WorldService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.I18n;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
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

        //add user in the whitelist
        createWhitelist(body, userInfos, loginMinetest);

        //add link to minetest server
        body.put(Field.LINK, minetestConfig.minetestServer());

        //Put new Id
        body.put(Field._ID, UUID.randomUUID().toString());

        //get New Port
        JsonObject sortByPort = new JsonObject().put(Field.PORT, 1);
        getMongo(null, null, null, null, null, null, sortByPort)
                .compose(this::getNewPort)
                .compose(res ->  {
                    int newPort = res;
                    body.put(Field.PORT, newPort);
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

        String loginMinetest = reformatLogin(user.getLogin());

        //Put new Id
        body.put(Field._ID, UUID.randomUUID().toString());

        //add user in the whitelist
        createWhitelist(body, user, loginMinetest);

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

    private void createWhitelist(JsonObject body, UserInfos user, String loginMinetest) {
        JsonObject userToInsert = new JsonObject()
                .put(Field.ID, user.getUserId())
                .put(Field.LOGIN, loginMinetest)
                .put(Field.DISPLAY_NAME, user.getUsername())
                .put(Field.FIRST_NAME, user.getFirstName())
                .put(Field.LAST_NAME, user.getLastName())
                .put(Field.WHITELIST, false);
        body.put(Field.WHITELIST, new JsonArray().add(userToInsert));
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
        JsonObject status = new JsonObject().put("$set", new JsonObject().put(Field.STATUS, body.getBoolean(Field.STATUS)));

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

        extractUsersFromGroups(body)
                .compose(res -> reformatWhitelist(res, whitelistMinetest, whitelistDetails, user))
                .compose(res -> {
                    body.put(Field.WHITELIST, whitelistDetails);
                    return update(body.getString(Field._ID), body);
                })
                .compose(res -> {
                    if (Boolean.TRUE.equals(body.getBoolean(Field.ISEXTERNAL))) {
                        Promise<JsonObject> doNothing = Promise.promise();
                        doNothing.complete(new JsonObject());
                        return doNothing.future();
                    } else {
                        StringBuilder whitelistInFile = new StringBuilder();
                        for (Object login : whitelistMinetest) {
                            whitelistInFile.append(login).append("\n");
                        }
                        JsonObject copyBody = body.copy();
                        copyBody.put(Field.WHITELIST, whitelistInFile.toString());
                        copyBody.put(Field.ID, body.getString(Field._ID));
                        return minetestService.action(copyBody, MinestestServiceAction.WHITELIST);
                    }
                })
                .compose(res -> {
                    JsonArray listMails = createMailList(user, body, request, password);
                    return sendMail(listMails);
                })
                .onSuccess(promise::complete)
                .onFailure(err -> promise.fail(err.getMessage()));
        return promise.future();
    }

    private Future<JsonArray> extractUsersFromGroups(JsonObject body) {
        Promise<JsonArray> promise = Promise.promise();
        JsonArray whiteListJustUser = new JsonArray();
        JsonArray whitelist = body.getJsonArray(Field.WHITELIST);
        for (int i = 0; i < whitelist.size(); i++) {
            JsonObject element = whitelist.getJsonObject(i);
            if (element.containsKey(Field.IS_GROUP)){
                String idGroup = element.getString(Field.ID);
                JsonObject action = new JsonObject()
                        .put("action", "list-users")
                        .put("groupIds", new JsonArray().add(idGroup));
                int finalI = i;
                eb.request("directory", action,
                        (Handler<AsyncResult<Message<JsonObject>>>) messageEvent -> {
                            if (!"ok".equals(messageEvent.result().body().getString("status"))) {
                                log.error("[Minetest@extractUsersFromGroups] Failed to search users in group : " + messageEvent.cause());
                                promise.fail("[Minetest@extractUsersFromGroups] Failed to search users in group : " + messageEvent.cause());
                            } else {
                                JsonArray listUsers = messageEvent.result().body().getJsonArray("result");
                                for (Object u : listUsers){
                                    whiteListJustUser.add(new JsonObject().put(Field.ID,((JsonObject) u).getString(Field.ID)));
                                }
                                if (finalI == whitelist.size() - 1) {
                                    promise.complete(whiteListJustUser);
                                }
                            }
                        });
            } else {
                whiteListJustUser.add(element);
                if (i == whitelist.size() - 1) {
                    promise.complete(whiteListJustUser);
                }
            }
        }
        return promise.future();
    }

    private Future<JsonArray> reformatWhitelist(JsonArray whitelist, JsonArray whitelistMinetest,
                                                JsonArray whitelistDetails, UserInfos owner) {
        Promise<JsonArray> promise = Promise.promise();
        JsonArray newWhiteList = new JsonArray();
        // append data to whitelist JsonArray, whitelistMinetest JsonArray etc...
        createOldNewWhiteList(whitelist, whitelistMinetest, whitelistDetails, newWhiteList, owner)
                .compose( res -> getInfosUsers(whitelistMinetest, whitelistDetails, newWhiteList))
                .onSuccess(promise::complete)
                .onFailure(err -> promise.fail(err.getMessage()));;
        return promise.future();
    }

    private Future<JsonArray> getInfosUsers(JsonArray whitelistMinetest, JsonArray whitelistDetails, JsonArray newWhiteList) {
        Promise<JsonArray> promise = Promise.promise();
        if (newWhiteList.size() > 0) {
            List<Future> users = new ArrayList<>();
            for (int i = 0; i < newWhiteList.size(); i++) {
                Promise<JsonObject> future = Promise.promise();
                users.add(future.future());
                JsonObject userInfos = newWhiteList.getJsonObject(i);
                UserUtils.getUserInfos(eb, userInfos.getString(Field.ID), user -> {
                    String loginToInsert = reformatLogin(user.getLogin());
                    checkDuplicates(whitelistDetails, whitelistMinetest, user, loginToInsert);
                    future.handle(Future.succeededFuture(new JsonObject()));
                });
            }
            CompositeFuture.all(users)
                    .onSuccess(success -> promise.complete(new JsonArray()))
                    .onFailure(fail -> {
                        log.error("[Minetest@getInfosUsers] Failed to get users infos : " + fail.getCause());
                        promise.fail(fail.getCause());
                    });
        } else {
            promise.complete(new JsonArray());
        }
        return promise.future();
    }

    private Future<JsonObject> createOldNewWhiteList(JsonArray whitelist, JsonArray whitelistMinetest, JsonArray whitelistDetails,
                                                     JsonArray newWhiteList, UserInfos owner) {
        Promise<JsonObject> promise = Promise.promise();
        JsonArray whitelistDetailsId = new JsonArray();
        for (int i = 0; i < whitelist.size(); i++) {
            JsonObject userInfos = whitelist.getJsonObject(i);
            if (!userInfos.containsKey(Field.IS_GROUP)) {
                if (userInfos.containsKey(Field.LOGIN)) {
                    //Already invited, we don't send a new mail except for the owner
                    userInfos.put(Field.WHITELIST, !userInfos.getString(Field.ID).equals(owner.getUserId()));
                    whitelistDetails.add(userInfos);
                    whitelistDetailsId.add(userInfos.getString(Field.ID));
                    whitelistMinetest.add(userInfos.getString(Field.LOGIN));
                    completePromise(i, whitelist.size(), promise);
                } else {
                    if (whitelistDetailsId.contains(userInfos.getString(Field.ID))) {
                        //the owner wants to reinvite the user by sending a new mail
                        for (int j = 0; j < whitelistDetails.size(); j++) {
                            JsonObject w = whitelistDetails.getJsonObject(j);
                            if(w.getString(Field.ID).equals(userInfos.getString(Field.ID))){
                                w.put(Field.WHITELIST, false);
                            }
                        }
                        completePromise(i, whitelist.size(), promise);
                    } else {
                        newWhiteList.add(userInfos);
                        completePromise(i, whitelist.size(), promise);
                    }
                }
            } else completePromise(i, whitelist.size(), promise);
        }
        return promise.future();
    }

    private void completePromise(int i, int whitelist, Promise<JsonObject> promise) {
        if (i == whitelist - 1) {
            promise.complete(new JsonObject());
        }
    }

    private void checkDuplicates(JsonArray whitelistDetails, JsonArray whitelistMinetest, UserInfos userInfos,
                                 String loginToInsert) {
        //Check duplicate login
        int i = 1;
        while (whitelistMinetest.contains(loginToInsert)) {
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
                .put(Field.LAST_NAME, userInfos.getLastName())
                .put(Field.WHITELIST, false);
        whitelistDetails.add(userToInsert);
    }

    private Future<JsonObject> sendMail(JsonArray listMails) {
        Promise<JsonObject> promise = Promise.promise();

        int j = 0;
        this.recursiveSendMail(j, listMails, sendEvent -> {
            if (sendEvent.isRight()) {
                promise.complete(new JsonObject());
            } else {
                log.error("[Minetest@sendMail] Failed to send mails : " + sendEvent.left().getValue());
                promise.fail(sendEvent.left().getValue());
            }
        });

        return promise.future();
    }
    private void recursiveSendMail(int indexMail, JsonArray listMails, Handler<Either<String, JsonObject>> result) {
        // Send mail via Conversation app if it exists or else with Zimbra
        eb.request("org.entcore.conversation", listMails.getJsonObject(indexMail),
                (Handler<AsyncResult<Message<JsonObject>>>) messageEvent -> {
                    if (messageEvent.result().body().getString("status") == null ||
                            (messageEvent.result().body().getString("status") != null && !"ok".equals(messageEvent.result().body().getString("status")))) {
                        log.error("[Minetest@recursiveSendMail] Failed to send mail : " +
                                (messageEvent.result().body().getString("message") != null ? messageEvent.result().body().getString("message") : ""));
                    }
                });
        if (indexMail == listMails.size() - 1) {
            result.handle(new Either.Right<>(new JsonObject()));
        } else {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.recursiveSendMail(indexMail + 1, listMails, result);
        }
    }


    private JsonArray createMailList(UserInfos owner, JsonObject body, HttpServerRequest request, String password) {
        JsonArray listMails = new JsonArray();
        I18n i18n = I18n.getInstance();
        String host = getHost(request);
        String acceptLanguage = I18n.acceptLanguage(request);
        String path = body.getString(Field.ADDRESS)
                .replace("http://","").replace("https://","");
        JsonArray whitelistIdAndLogin = body.getJsonArray(Field.WHITELIST);
        for (Object u : whitelistIdAndLogin) {
            JsonObject user = (JsonObject) u;
            if (Boolean.FALSE.equals(user.getBoolean(Field.WHITELIST))) {
                String passwordBody = "";
                String loginBody = "";
                if (body.getBoolean(Field.ISEXTERNAL) == null) {
                    passwordBody = i18n.translate("minetest.invitation.default.body.password", host, acceptLanguage) + password;
                    loginBody = i18n.translate("minetest.invitation.default.body.name", host, acceptLanguage) + user.getString(Field.LOGIN);
                }

                String mailBody = i18n.translate("minetest.invitation.default.body.1", host, acceptLanguage)
                        .replace("<mettre lien>", this.minetestConfig.minetestDownload()) +
                        i18n.translate("minetest.invitation.default.body.address", host, acceptLanguage) + path +
                        i18n.translate("minetest.invitation.default.body.port", host, acceptLanguage) + body.getInteger(Field.PORT) +
                        loginBody + passwordBody + i18n.translate("minetest.invitation.default.body.end", host, acceptLanguage);

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
            JsonArray whitelist = body.getJsonArray(Field.WHITELIST);
            for (Object u : whitelist) {
                JsonObject userInfos = (JsonObject) u;
                userInfos.remove(Field.$$HASHKEY);
            }
            worldData.put(Field.WHITELIST, whitelist);
        }

        if (body.getBoolean(Field.ISEXTERNAL) != null && Boolean.TRUE.equals(body.getBoolean(Field.ISEXTERNAL))) {
            if (body.getString(Field.ADDRESS) != null) {
                worldData.put(Field.ADDRESS, body.getString(Field.ADDRESS));
            }
            if (body.getInteger(Field.PORT) != null) {
                worldData.put(Field.PORT, body.getInteger(Field.PORT));
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
            if (body.getString(Field.PASSWORD) != null) {
                resetPassword(String.valueOf(body.getString(Field.PASSWORD)), body)
                        .onSuccess(res -> promise.complete(result.right().getValue()))
                        .onFailure(err -> promise.fail(err.getMessage()));
            } else {
                promise.complete(result.right().getValue());
            }
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
    public void shuttingDownWorld(Handler<Either<String, JsonObject>> handler) {

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
