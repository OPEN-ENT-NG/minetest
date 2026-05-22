package fr.openent.minetest.controller;

import fr.openent.minetest.cron.ShuttingDownWorld;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.BaseController;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class TaskController extends BaseController {
    protected static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final ShuttingDownWorld shuttingDownWorldTask;

    public TaskController(ShuttingDownWorld shuttingDownWorldTask) {
        this.shuttingDownWorldTask = shuttingDownWorldTask;
    }


    @Post("api/internal/shutting-down-world")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void shuttingDownWorld(final HttpServerRequest request) {
        log.info("Triggered shutting down world check task");
        shuttingDownWorldTask.handle(0L);
        render(request, null, 202);
    }
}
