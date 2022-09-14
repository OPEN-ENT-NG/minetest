package fr.openent.minetest;

import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.controller.MinetestController;
import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.core.constants.Right;
import fr.openent.minetest.cron.ShuttingDownWorld;
import fr.openent.minetest.controller.ShareWorldController;
import fr.openent.minetest.service.ServiceFactory;
import fr.wseduc.cron.CronTrigger;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.http.BaseServer;
import org.entcore.common.http.filter.ShareAndOwner;
import org.entcore.common.mongodb.MongoDbConf;
import org.entcore.common.notification.TimelineHelper;
import org.entcore.common.service.impl.MongoDbCrudService;
import org.entcore.common.share.impl.MongoDbShareService;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

import java.text.ParseException;

public class Minetest extends BaseServer {
	public static final String WORLD_COLLECTION = Field.WORLD;

	public static final String CONTRIB_RESOURCE_RIGHT = Right.CONTRIB;
	public static final String MANAGER_RESOURCE_RIGHT = Right.MANAGER;

	@Override
	public void start() throws Exception {
		super.start();

		Storage storage = new StorageFactory(vertx, config).getStorage();
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

		try {
			new CronTrigger(vertx, config.getString(Field.MINETEST_SHUTTING_DOWN_CRON)).schedule(
					new ShuttingDownWorld(serviceFactory)
			);
		} catch (ParseException e) {
			log.fatal("[Minetest@Minetest.java] Invalid shutting-down-cron cron expression" + e.getMessage());
		}
	}

}
