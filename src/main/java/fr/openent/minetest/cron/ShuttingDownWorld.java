package fr.openent.minetest.cron;

import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.service.ServiceFactory;
import fr.openent.minetest.service.WorldService;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.controller.ControllerHelper;

public class ShuttingDownWorld extends ControllerHelper implements Handler<Long>   {
    private static final Logger log = LoggerFactory.getLogger(ShuttingDownWorld.class);
    private final WorldService worldService;


    public ShuttingDownWorld(ServiceFactory serviceFactory) {
        this.worldService = serviceFactory.worldService();
    }

    @Override
    public void handle(Long event) {
        log.info("[Minetest@ShuttingDownWorld] Shutting down world launched");
        worldService.getMongo(null, null, null, null, null, null,
                        true, true, new JsonObject())
                .compose(this::updateStatusAllWorld)
                .onSuccess(res -> log.info("[Minetest@ShuttingDownWorld] Shutting down cron finish successfully"))
                .onFailure(err -> {
                    log.error("[Minetest@ShuttingDownWorld] Shutting down cron on failure : " + err.getMessage());
                    err.printStackTrace();
                });
    }

    private Future<Void> updateStatusAllWorld(JsonArray worlds) {
        Promise<Void> promise = Promise.promise();
        if (worlds.isEmpty()) {
            promise.complete();
        } else {
            Future<JsonObject> current = Future.succeededFuture();
            for (int i = 0; i < worlds.size(); i++) {
                int finalI = i;
                current = current.compose(v -> {
                    JsonObject world = worlds.getJsonObject(finalI);
                    world.put(Field.STATUS, false);
                    return worldService.updateStatus(world);
                });
            }
            current.onSuccess(res -> promise.complete())
                    .onFailure(err -> {
                        err.printStackTrace();
                        log.error("[Minetest@updateStatusAllWorld] Failed to update status of all world in cron : " + err.getMessage());
                        promise.fail(err.getMessage());
                    });
        }
        return promise.future();
    }
}
