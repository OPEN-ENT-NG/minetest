package fr.openent.minetest.service;

import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.sql.Sql;
import org.entcore.common.storage.Storage;

public class ServiceFactory {
    private final Vertx vertx;
    private final Storage storage;
    private final Neo4j neo4j;
    private final Sql sql;
    private final MongoDb mongoDb;

    public ServiceFactory(Vertx vertx, Storage storage, Neo4j neo4j, Sql sql, MongoDb mongoDb) {
        this.vertx = vertx;
        this.storage = storage;
        this.neo4j = neo4j;
        this.sql = sql;
        this.mongoDb = mongoDb;
    }

    // Helpers
    public EventBus eventBus() {
        return this.vertx.eventBus();
    }

    public Vertx vertx() {
        return this.vertx;
    }
}
