package fr.openent.minetest.services;

import fr.openent.minetest.Minetest;
import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.service.ServiceFactory;
import fr.openent.minetest.service.WorldService;
import fr.openent.minetest.service.impl.DefaultWorldService;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.user.UserInfos;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.mock;

@RunWith(VertxUnitRunner.class)
public class DefaultWorldServiceTest {

    private WorldService worldService;
    MongoDb mongo = mock(MongoDb.class);

    private static final String IMPORTWORLD_ID = "{\"_id\":\"111\"}";

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
    public void testCreateImportWorld(TestContext context) {
        JsonObject now = MongoDb.now();

        // Arguments
        Promise<JsonObject> promise = Promise.promise();

        JsonObject world = new JsonObject()
                .put("_id", IMPORTWORLD_ID)
                .put("owner_id", "ownerId")
                .put("owner_name", "ownerName")
                .put("owner_login", "ownerLogin")
                .put("created_at", "createdAt")
                .put("updated_at", "updatedAt")
                .put("title", "my world")
                .put("address", "myworld.fr")
                .put("port", "30000")
                .put("isExternal", true);

        //Expected data
        String expectedCollection = "world";
        JsonObject expectedWorld = new JsonObject()
                .put("_id", IMPORTWORLD_ID)
                .put("owner_id", "ownerId")
                .put("owner_name", "ownerName")
                .put("owner_login", "ownerLogin")
                .put("created_at", "createdAt")
                .put("updated_at", "updatedAt")
                .put("title", "my world")
                .put("address", "myworld.fr")
                .put("port", "30000")
                .put("isExternal", true);

        Mockito.doAnswer(invocation -> {
            String collection = invocation.getArgument(0);
            JsonObject query = invocation.getArgument(1);
            context.assertEquals(collection, expectedCollection);
            context.assertEquals(query, expectedWorld);
            return null;
        }).when(mongo).insert(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        try {
            Whitebox.invokeMethod(worldService, "importWorld", promise, world);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testImportWorldHasIsExternal(TestContext context) {

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