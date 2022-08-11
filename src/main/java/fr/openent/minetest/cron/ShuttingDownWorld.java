package fr.openent.minetest.cron;

import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.service.ServiceFactory;
import fr.openent.minetest.service.WorldService;
import fr.wseduc.webutils.Either;
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
        log.info("shutting down world launched");
        worldService.getMongo(null, null, null, null, null, null,
                        true, true, new JsonObject())
                .compose(this::updateStatusAllWorld)
                .onSuccess(res -> log.info("shutting down cron finish successfully"))
                .onFailure(err -> {
                    log.error("shutting down cron on failure", err);
                });
    }

    private Future<JsonObject> updateStatusAllWorld(JsonArray res) {
        Promise<JsonObject> promise = Promise.promise();

        if(res.isEmpty()){
            promise.complete(new JsonObject());
        } else{
            int j = 0;
            this.recursiveShuttingDownWorld(j, res, event -> {
                if (event.isRight()) {
                    promise.complete(new JsonObject());
                } else {
                    log.error("[Minetest@updateStatusAllWorld] Failed to update status of all world in cron : " + event.left().getValue());
                    promise.fail(event.left().getValue());
                }
            });
        }

        return promise.future();
    }

    private void recursiveShuttingDownWorld(int index, JsonArray worlds, Handler<Either<String, JsonObject>> result) {
        // update status of the world by closing the port
        JsonObject world = worlds.getJsonObject(index);
        world.put(Field.STATUS, false);
        worldService.updateStatus(world)
                .onSuccess(res -> updateNextWorld(index, worlds, result))
                .onFailure(err -> {
                    log.error("[Minetest@recursiveShuttingDownWorld] Failed to update status of the world in cron : " + world, err);
                    updateNextWorld(index, worlds, result);
                });
    }

    private void updateNextWorld(int index, JsonArray worlds, Handler<Either<String, JsonObject>> result) {
        if (index == worlds.size() - 1) {
            result.handle(new Either.Right<>(new JsonObject()));
        } else {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.recursiveShuttingDownWorld(index + 1, worlds, result);
        }
    }

}
