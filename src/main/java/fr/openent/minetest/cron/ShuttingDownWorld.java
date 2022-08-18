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
    private final long TIMER = 500;

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
                current = current.compose(v -> shutDownWorld(worlds.getJsonObject(finalI)).onComplete(Future.succeededFuture()));
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
    private Future<JsonObject> shutDownWorld(JsonObject world) {
        // update status of the world by closing the port
        Promise<JsonObject> promise = Promise.promise();
        world.put(Field.STATUS, false);
        //Wait 0,5 second in order to not crash the Minetest server by multiple request
        vertx.setTimer(TIMER, timer ->
                worldService.updateStatus(world)
                        .onSuccess(promise::complete)
                        .onFailure(err -> {
                            log.error("[Minetest@shutDownWorld] Failed to update status of the world in cron, world : " + world);
                            log.error("[Minetest@shutDownWorld] Failed to update status of the world in cron, error : " + err.getMessage());
                            err.printStackTrace();
                            promise.fail(err.getMessage());
                        }));
        return promise.future();
    }
}
