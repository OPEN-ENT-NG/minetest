package fr.openent.minetest.service.impl;

import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.enums.MinestestServiceAction;
import fr.openent.minetest.service.MinetestService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class DefaultMinetestService implements MinetestService {

    private final Logger log = LoggerFactory.getLogger(DefaultMinetestService.class);
    private final WebClient client;
    private final String serverPythonUrl;

    public DefaultMinetestService(Vertx vertx, MinetestConfig minetestConfig) {
        this.client = WebClient.create(vertx);
        this.serverPythonUrl = minetestConfig.minetestServer() + ":" + minetestConfig.minetestPythonServerPort();
    }

    @Override
    public Future<JsonObject> action(JsonObject body, MinestestServiceAction action) {
        Promise<JsonObject> promise = Promise.promise();

        for (String name : body.fieldNames()){
            body.put(name, body.getValue(name).toString());
        }

        client.postAbs(serverPythonUrl + "/" + action.toString())
                .sendJsonObject(body , resp -> {
                    if (resp.failed() || resp.result().statusCode() != 200) {
                        answerFailure(action, promise, resp);
                        return;
                    }
                    promise.complete(resp.result().bodyAsJsonObject());
                });
        return promise.future();
    }

    private void answerFailure(MinestestServiceAction action, Promise<JsonObject> promise,
                               AsyncResult<HttpResponse<Buffer>> resp) {
        String error;
        if (resp.failed()){
            error = resp.cause().getMessage();
        } else {
            JsonObject errorJson = resp.result().bodyAsJsonObject();
            error = errorJson.getJsonArray(Field.MESSAGE).getString(0) + " ; " + errorJson.getString(Field.DATA);
        }
        String message = String.format("[Minetest@%s:: %s]: An error has occurred through python server: %s",
                this.getClass().getSimpleName(), action, error);
        log.error(message, error);
        promise.fail(message);
    }
}
