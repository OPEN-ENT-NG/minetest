package fr.openent.minetest.services;

import fr.openent.minetest.Minetest;
import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.service.ServiceFactory;
import fr.openent.minetest.service.WorldService;
import fr.openent.minetest.service.impl.DefaultWorldService;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.user.UserInfos;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

@RunWith(VertxUnitRunner.class)
public class DefaultWorldServiceTest {

    private WorldService worldService;
    MongoDb mongo = mock(MongoDb.class);

    private static final String IMPORTWORLD_ID = "111";

    @Before
    public void setUp() {
        Vertx vertx = Vertx.vertx();
        JsonObject config = new JsonObject()
                .put("minetest-port-range", "30000-30100");

        MinetestConfig minetestConfig = new MinetestConfig(config);
        ServiceFactory serviceFactory = new ServiceFactory(vertx, minetestConfig, mongo);
        this.worldService = new DefaultWorldService(serviceFactory, Minetest.WORLD_COLLECTION, mongo);
    }

    @Test
    public void testImportWorld(TestContext context) {

        //Expected data
        String expectedCollection = "world";
        JsonObject expectedQuery = new JsonObject()
                .put("_id", IMPORTWORLD_ID)
                .put("isExternal", true);

        Mockito.doAnswer(invocation -> {
            String collection = invocation.getArgument(0);
            JsonObject query = invocation.getArgument(1);
            context.assertEquals(collection, expectedCollection);
            context.assertEquals(query, expectedQuery);
            return null;
        }).when(mongo).findOne(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        worldService.importWorld(expectedQuery, new UserInfos());
    }
}