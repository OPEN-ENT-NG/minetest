package fr.openent.minetest.core.constants;

public class Field {

    private Field() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ID = "id";
    public static final String _ID = "_id";
    public static final String $IN = "$in";
    public static final String ACCESS = "ACCESS";
    public static final String OWNER_ID = "owner_id";
    public static final String OWNER_NAME = "owner_name";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATE_AT = "update_at";
    public static final String IMG = "img";
    public static final String SHARED = "shared";
    public static final String TITLE = "title";
    public static final String WORLD = "world";
    public static final String PORT = "port";
    public static final String FILEID = "fileId";
    public static final String METADATA = "metadata";
    public static final String LINK = "link";
    public static final String MESSAGE = "message";
    public static final String STATUS = "status";

    // Config
    public static final String MINETEST_DOWNLOAD = "minetest-download";
    public static final String MINETESTDOWNLOAD = "minetestDownload";
    public static final String MINETEST_SERVER = "minetest-server";
    public static final String MINETEST_PORT_RANGE = "minetest-port-range";
}