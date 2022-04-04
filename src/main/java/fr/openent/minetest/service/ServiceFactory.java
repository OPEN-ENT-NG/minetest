package fr.openent.minetest.service;

import fr.openent.minetest.Minetest;
import fr.openent.minetest.config.MinetestConfig;
import fr.openent.minetest.service.impl.DefaultMinetestService;
import fr.openent.minetest.service.impl.DefaultWorldService;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

public class ServiceFactory {
    private final Vertx vertx;
    private final MinetestConfig minetestConfig;
    private final MongoDb mongoDb;

    public ServiceFactory(Vertx vertx, MinetestConfig minetestConfig, MongoDb mongoDb) {
        this.vertx = vertx;
        this.minetestConfig = minetestConfig;
        this.mongoDb = mongoDb;
    }

    // Helpers
    public EventBus eventBus() {
        return this.vertx.eventBus();
    }

    public Vertx vertx() {
        return this.vertx;
    }

    public MinetestConfig minetestConfig() {
        return this.minetestConfig;
    }


    // Services

    public MinetestService minetestService() {
        return new DefaultMinetestService(this.vertx, this.minetestConfig);
    }

    public WorldService worldService() {
        return new DefaultWorldService(this, Minetest.WORLD_COLLECTION, mongoDb);
    }
}
