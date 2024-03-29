package fr.openent.minetest.core.constants;

public class Field {

    private Field() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ID = "id";
    public static final String _ID = "_id";
    public static final String $IN = "$in";
    public static final String ACCESS = "ACCESS";
    public static final String USERID = "userId";
    public static final String OWNER_ID = "owner_id";
    public static final String OWNER_NAME = "owner_name";
    public static final String OWNER_LOGIN = "owner_login";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String IMG = "img";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String WORLD = "world";
    public static final String IMPORT_WORLD = "import_world";
    public static final String JOIN = "join";
    public static final String PORT = "port";
    public static final String FILEID = "fileId";
    public static final String METADATA = "metadata";
    public static final String LINK = "link";
    public static final String MESSAGE = "message";
    public static final String STATUS = "status";
    public static final String SHUTTINGDOWN = "shuttingDown";
    public static final String DATA = "data";
    public static final String ADDRESS = "address";
    public static final String ISEXTERNAL = "isExternal";
    public static final String PASSWORD = "password";
    public static final String WHITELIST = "whitelist";
    public static final String LOGIN = "login";
    public static final String DISPLAY_NAME = "displayName";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String SUBJECT = "subject";
    public static final String $$HASHKEY = "$$hashKey";
    public static final String IS_GROUP = "isGroup";
    public static final String SHARED = "shared";

    // Config
    public static final String MINETEST_DOWNLOAD = "minetest-download";
    public static final String MINETEST_LINK = "minetest-link";
    public static final String MINETEST_WIKI = "minetest-wiki";
    public static final String MINETEST_SERVER = "minetest-server";
    public static final String MINETEST_PORT_RANGE = "minetest-port-range";
    public static final String MINETEST_PYTHON_SERVER_PORT = "minetest-python-server-port";
    public static final String MINETEST_MESSAGING = "minetest-messaging";
    public static final String MINETEST_SHUTTING_DOWN_CRON = "shutting-down-cron";
}