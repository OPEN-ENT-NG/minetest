package fr.openent.minetest.controller;

import fr.openent.minetest.cron.ShuttingDownWorld;
import fr.wseduc.rs.Post;
import fr.wseduc.webutils.http.BaseController;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


public class TaskController extends BaseController {
	protected static final Logger log = LoggerFactory.getLogger(TaskController.class);

	final ShuttingDownWorld shuttingDownWorld;

	public TaskController(ShuttingDownWorld shuttingDownWorld) {
		this.shuttingDownWorld = shuttingDownWorld;
	}

	@Post("api/internal/shutting-down-world")
	public void shuttingDownWorld(HttpServerRequest request) {
		log.info("Triggered shutting down world task");
		shuttingDownWorld.handle(0L);
		render(request, null, 202);
	}
}
