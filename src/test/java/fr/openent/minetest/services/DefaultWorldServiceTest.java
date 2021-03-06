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
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.mock;

@RunWith(VertxUnitRunner.class)
public class DefaultWorldServiceTest {

    private WorldService worldService;
    MongoDb mongo = mock(MongoDb.class);

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
    public void testCreateImportWorld(TestContext context) throws Exception {
        // Arguments
        UserInfos userInfos = new UserInfos();
        userInfos.setLogin("login");

        JsonObject world = new JsonObject()
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
        String expectedWorld = "{\"owner_id\":\"ownerId\",\"owner_name\":\"ownerName\",\"owner_login\":\"ownerLogin\"," +
                "\"created_at\":\"createdAt\",\"updated_at\":\"updatedAt\",\"title\":\"my world\"," +
                "\"address\":\"myworld.fr\",\"port\":\"30000\",\"isExternal\":true,\"whitelist\":[{\"id\":null," +
                "\"login\":\"login\",\"displayName\":null,\"firstName\":null,\"lastName\":null,\"whitelist\":false}]}";

        Mockito.doAnswer(invocation -> {
            String collection = invocation.getArgument(0);
            JsonObject query = invocation.getArgument(1);
            query.remove("_id");
            context.assertEquals(collection, expectedCollection);
            context.assertEquals(query.toString(), expectedWorld);
            return null;
        }).when(mongo).insert(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        Whitebox.invokeMethod(worldService, "importWorld", world, userInfos);
    }

}