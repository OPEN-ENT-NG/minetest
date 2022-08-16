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
import org.entcore.common.user.UserInfos;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.SuperAdminFilter;
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

    @Get("/config")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(SuperAdminFilter.class)
    public void getConfig(final HttpServerRequest request) {
        renderJson(request, config);
    }

    @Get("")
    @ApiDoc("Render view")
    @SecuredAction("view")
    public void view(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            JsonObject config = new JsonObject()
                    .put(Field.MINETEST_DOWNLOAD, this.minetestConfig.minetestDownload())
                    .put(Field.MINETEST_LINK, this.minetestConfig.minetestLink())
                    .put(Field.MINETEST_WIKI, this.minetestConfig.minetestWiki())
                    .put(Field.MINETEST_SERVER, this.minetestConfig.minetestServer())
                    .put(Field.MINETEST_MESSAGING, this.minetestConfig.minetestMessaging());
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
        String updatedAt = request.getParam(Field.UPDATED_AT);
        String img = request.getParam(Field.IMG);
        String title = request.getParam(Field.TITLE);
        Boolean status = request.getParam(Field.STATUS) != null ? Boolean.parseBoolean(request.getParam(Field.STATUS)) : null;
        Boolean shuttingDown = request.getParam(Field.SHUTTINGDOWN) != null ? Boolean.parseBoolean(request.getParam(Field.SHUTTINGDOWN)) : null;

        UserUtils.getUserInfos(eb, request, user -> worldService.getMongo(ownerId, ownerName, createdAt, updatedAt, img,
                        title, status, shuttingDown, new JsonObject())
                .onSuccess(world -> renderJson(request, world))
                .onFailure(err -> renderError(request)));
    }

    @Post("/worlds")
    @ApiDoc("Create world")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void postWorld(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + Field.WORLD, body
                -> UserUtils.getUserInfos(eb, request, user
                -> worldService.create(body, user)
                .onSuccess(res -> renderJson(request, body))
                .onFailure(err -> renderError(request))));
    }

    @Post("/worlds/import")
    @ApiDoc("Import world")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void importWorld(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + Field.IMPORT_WORLD, body
                -> UserUtils.getUserInfos(eb, request, user
                -> worldService.importWorld(body, user)
                .onSuccess(res -> renderJson(request, body))
                .onFailure(err -> renderError(request))));
    }

    @Put("/worlds/:id")
    @ApiDoc("Update world")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void putWorld(final HttpServerRequest request) {
        String worldId = request.getParam("id");
        RequestUtils.bodyToJson(request, pathPrefix + Field.WORLD, body
                -> worldService.update(worldId, body)
                .onSuccess(res -> renderJson(request, body))
                .onFailure(err -> renderError(request)));
    }

    @Put("/worlds/import/:id")
    @ApiDoc("Update import world")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void putImportWorld(final HttpServerRequest request) {
        String worldId = request.getParam("id");
        RequestUtils.bodyToJson(request, pathPrefix + Field.IMPORT_WORLD, body
                -> worldService.update(worldId, body)
                .onSuccess(res -> renderJson(request, body))
                .onFailure(err -> renderError(request)));
    }

    @Put("/worlds/status/:id")
    @ApiDoc("Update status world")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void putStatus(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + Field.WORLD, body
                -> UserUtils.getUserInfos(eb, request, user
                -> worldService.updateStatus(body)
                            .onSuccess(res -> renderJson(request, body))
                            .onFailure(err -> renderError(request))));
    }

    @Put("/world/join")
    @ApiDoc("Invite user to the world")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void joinWorld(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + Field.JOIN, body
                -> UserUtils.getUserInfos(eb, request, user
                -> worldService.join(user, body, request)
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
        List<String> ports = request.params().getAll(Field.PORT);
        UserUtils.getUserInfos(eb, request, user -> worldService.delete(user, ids, ports)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request)));
    }

    @Delete("/worlds/import/:id")
    @ApiDoc("Delete world")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteImportWorld(final HttpServerRequest request) {
        List<String> ids = new ArrayList<>(Collections.singletonList(
                request.getParam(Field.ID)
        ));
        UserUtils.getUserInfos(eb, request, user -> worldService.deleteImportWorld(user, ids)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request)));
    }
}
