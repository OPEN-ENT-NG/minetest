package fr.openent.minetest.controller;

import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.service.ServiceFactory;
import fr.openent.minetest.service.WorldService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.user.UserUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinetestController extends ControllerHelper {
    private final WorldService worldService;

    private final EventStore eventStore;
    private final MinetestConfig minetestConfig;

    public MinetestController(ServiceFactory serviceFactory) {
        this.worldService = serviceFactory.worldService();
        this.eventStore = EventStoreFactory.getFactory().getEventStore(fr.openent.minetest.Minetest.class.getSimpleName());
        this.minetestConfig = serviceFactory.minetestConfig();
    }

    @Get("")
    @ApiDoc("Render view")
    @SecuredAction("view")
    public void view(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            JsonObject config = new JsonObject()
                    .put(Field.MINETESTDOWNLOAD, this.minetestConfig.minetestDownload());
            renderView(request, config);
            eventStore.createAndStoreEvent(Field.ACCESS, request);
        });
    }

    @Get("/worlds")
    @ApiDoc("Retrieve worlds")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getWorlds(final HttpServerRequest request) {
        String ownerId = request.getParam(Field.OWNER_ID);
        String ownerName = request.getParam(Field.OWNER_NAME);
        String createdAt = request.getParam(Field.CREATED_AT);
        String updatedAt = request.getParam(Field.UPDATE_AT);
        String img = request.getParam(Field.IMG);
        String shared = request.getParam(Field.SHARED);
        String title = request.getParam(Field.TITLE);

        UserUtils.getUserInfos(eb, request, user -> worldService.get(ownerId, ownerName, createdAt, updatedAt, img,
                        shared, title)
                .onSuccess(world -> renderJson(request, world))
                .onFailure(err -> renderError(request)));
    }

    @Post("/worlds")
    @ApiDoc("Create world")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void postWorld(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "world", body
                -> UserUtils.getUserInfos(eb, request, user
                -> worldService.create(user, body)
                .onSuccess(res -> renderJson(request, body))
                .onFailure(err -> renderError(request))));
    }

    @Put("/worlds")
    @ApiDoc("Create world")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void putWorld(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "world", body
                -> UserUtils.getUserInfos(eb, request, user
                -> worldService.update(user, body)
                            .onSuccess(res -> renderJson(request, body))
                            .onFailure(err -> renderError(request))));
    }

    @Delete("/worlds")
    @ApiDoc("Delete world")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteWorld(final HttpServerRequest request) {
        if (!request.params().contains(Field.ID)) {
            badRequest(request);
            return;
        }
        List<String> ids = request.params().getAll(Field.ID);
        UserUtils.getUserInfos(eb, request, user -> worldService.delete(user, ids)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request)));

    }
}
