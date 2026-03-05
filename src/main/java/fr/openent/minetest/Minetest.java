package fr.openent.minetest;

import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.controller.MinetestController;
import fr.openent.minetest.controller.ShareWorldController;
import fr.openent.minetest.controller.TaskController;
import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.core.constants.Right;
import fr.openent.minetest.cron.ShuttingDownWorld;
import fr.openent.minetest.service.ServiceFactory;
import fr.wseduc.cron.CronTrigger;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.http.BaseServer;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.mongodb.MongoDbConf;
import org.entcore.common.service.impl.MongoDbCrudService;
import org.entcore.common.share.impl.MongoDbShareService;

import java.text.ParseException;

public class Minetest extends BaseServer {
	public static final String WORLD_COLLECTION = Field.WORLD;

	public static final String CONTRIB_RESOURCE_RIGHT = Right.CONTRIB;
	public static final String MANAGER_RESOURCE_RIGHT = Right.MANAGER;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
    final Promise<Void> promise = Promise.promise();
    super.start(promise);
    promise.future()
      .compose(e -> this.initMineTest())
      .onComplete(startPromise);
  }
  public Future<Void> initMineTest() {
		MinetestConfig minetestConfig = new MinetestConfig(config);

		final MongoDbConf conf = MongoDbConf.getInstance();
		conf.setCollection(Field.WORLD);
		conf.setResourceIdLabel(Field.ID);

		setDefaultResourceFilter(new ShareAndOwner());

		ServiceFactory serviceFactory = new ServiceFactory(vertx, minetestConfig,
				MongoDb.getInstance());

		final EventBus eb = getEventBus(vertx);

		MinetestController minetestController = new MinetestController(serviceFactory);
		ShareWorldController shareWorldController = new ShareWorldController();

		addController(minetestController);
		addController(shareWorldController);

		shareWorldController.setShareService(new MongoDbShareService(eb, MongoDb.getInstance(), Field.WORLD, securedActions, null));
		shareWorldController.setCrudService(new MongoDbCrudService(Field.WORLD));

		// CRON
	    String shuttingDownCron = config.getString(Field.MINETEST_SHUTTING_DOWN_CRON);
		ShuttingDownWorld shuttingDownWorld = new ShuttingDownWorld(serviceFactory);
		// Enable the task to be triggered via API
		addController(new TaskController(shuttingDownWorld));
		// Schedule the task to run from cron expression if configured
		if (shuttingDownCron != null) {
			try {
				new CronTrigger(vertx, shuttingDownCron).schedule(shuttingDownWorld);
			} catch (ParseException e) {
				log.fatal("[Minetest@Minetest.java] Invalid shutting-down-cron cron expression" + e.getMessage());
			}
		}
    return Future.succeededFuture();
	}

}
