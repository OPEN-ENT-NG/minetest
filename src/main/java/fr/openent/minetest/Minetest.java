package fr.openent.minetest;

import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.controller.MinetestController;
import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.cron.ShuttingDownWorld;
import fr.openent.minetest.service.ServiceFactory;
import fr.wseduc.cron.CronTrigger;
import fr.wseduc.mongodb.MongoDb;
import org.entcore.common.http.BaseServer;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

import java.text.ParseException;

public class Minetest extends BaseServer {
	public static final String WORLD_COLLECTION = "world";

	@Override
	public void start() throws Exception {
		super.start();

		Storage storage = new StorageFactory(vertx, config).getStorage();
		MinetestConfig minetestConfig = new MinetestConfig(config);

		ServiceFactory serviceFactory = new ServiceFactory(vertx, minetestConfig,
				MongoDb.getInstance());

		addController(new MinetestController(serviceFactory));

		try {
			new CronTrigger(vertx, config.getString(Field.MINETEST_SHUTTING_DOWN_CRON)).schedule(
					new ShuttingDownWorld(serviceFactory)
			);
		} catch (ParseException e) {
			log.fatal("[Minetest@Minetest.java] Invalid shutting-down-cron cron expression" + e.getMessage());
		}
	}

}
