package fr.openent.minetest.cron;

import fr.openent.minetest.Minetest;
import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.service.ServiceFactory;
import fr.openent.minetest.service.WorldService;
import fr.openent.minetest.service.impl.DefaultWorldService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ShuttingDownWorld {
    private static final Logger log = LoggerFactory.getLogger(ShuttingDownWorld.class);
    private final WorldService worldService;

    JsonObject config = new JsonObject()
            .put("minetest-port-range", "30000-30100");

    public ShuttingDownWorld() {
        MinetestConfig minetestConfig = new MinetestConfig(config);
        ServiceFactory serviceFactory = new ServiceFactory(vertx, minetestConfig, mongo);
        this.worldService = new DefaultWorldService(serviceFactory, Minetest.WORLD_COLLECTION, mongo);
    }

    @Override
    public void handle(Long event) {
        log.info("shutting down world launched");
        worldService.(someActionsEvent -> {
            if (someActionsEvent.isLeft()) {
                log.info("CRON failed");
            }
            else {
                log.info("CRON successfully launch");
            }
        });
    }

    public void functionToDoSomeActions(Handler<Either<String, JsonObject>> handler) {
        // Here some calls BDD requests or anything you want to do
        // Needs to handle the result to 'handler' param

    }

//
//    @Override
//    public void handle(Long event) {
//        log.info("Absence removal task launched");
//        absenceService.absenceRemovalTask(result -> {
//            if (result.isLeft()) {
//                String message = "[Minetest@AbsenceRemovalTask] failed to automate Absence Removal CRON Task";
//                log.error(message, result.left().getValue());
//            } else {
//                log.info("Absence Removal CRON Task succeeded");
//            }
//        });
//    }


}
