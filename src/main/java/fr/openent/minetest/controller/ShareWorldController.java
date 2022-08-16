package fr.openent.minetest.controller;

import fr.openent.minetest.Minetest;
import fr.openent.minetest.core.constants.Field;
import fr.openent.minetest.service.ServiceFactory;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.I18n;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserUtils;

public class ShareWorldController extends ControllerHelper {

    public ShareWorldController() {
    }

    // Init sharing rights
    @SecuredAction(value = Minetest.CONTRIB_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void initContribResourceRight(final HttpServerRequest request) {
    }

    @SecuredAction(value = Minetest.MANAGER_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void initManagerResourceRight(final HttpServerRequest request) {
    }

    @Get("/share/json/:id")
    @ApiDoc("Share world by id.")
    @SecuredAction(value = Minetest.MANAGER_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void shareWorld(final HttpServerRequest request) {
        shareJson(request, false);
    }

    @Put("/share/resource/:id")
    @ApiDoc("Share world by id.")
    @SecuredAction(value = Minetest.MANAGER_RESOURCE_RIGHT, type = ActionType.RESOURCE)
    public void shareResource(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                final String id = request.params().get("id");
                if (id == null || id.trim().isEmpty()) {
                    badRequest(request, "invalid.id");
                    return;
                }

                JsonObject params = new JsonObject();
                params.put("profilUri", "/userbook/annuaire#" + user.getUserId() + "#" + user.getType());
                params.put("username", user.getUsername());
                params.put("minetestUri", "/minetest");

                JsonObject pushNotif = new JsonObject()
                        .put("title", "push.notif.minetest.share")
                        .put("body", user.getUsername() + " " + I18n.getInstance().translate("minetest.shared.push.notif.body",
                                getHost(request), I18n.acceptLanguage(request)));

                params.put("pushNotif", pushNotif);

                shareResource(request, "minetest.share", false, params, Field.TITLE);
            }
        });
    }
}
