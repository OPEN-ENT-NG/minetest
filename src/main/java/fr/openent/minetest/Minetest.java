package fr.openent.minetest;

import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.controller.MinetestController;
import fr.openent.minetest.service.ServiceFactory;
import fr.wseduc.mongodb.MongoDb;
import org.entcore.common.http.BaseServer;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

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
	}

}
