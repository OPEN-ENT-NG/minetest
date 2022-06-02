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
    public static final String OWNER_LOGIN = "owner_login";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
    public static final String IMG = "img";
    public static final String TITLE = "title";
    public static final String WORLD = "world";
    public static final String IMPORT_WORLD = "import_world";
    public static final String PORT = "port";
    public static final String FILEID = "fileId";
    public static final String METADATA = "metadata";
    public static final String LINK = "link";
    public static final String MESSAGE = "message";
    public static final String STATUS = "status";
    public static final String DATA = "data";
    public static final String ADDRESS = "address";
    public static final String ISEXTERNAL = "isExternal";
    public static final String PASSWORD = "password";

    // Config
    public static final String MINETEST_DOWNLOAD = "minetest-download";
    public static final String MINETESTDOWNLOAD = "minetestDownload";
    public static final String MINETEST_SERVER = "minetest-server";
    public static final String MINETEST_PORT_RANGE = "minetest-port-range";
    public static final String MINETEST_PYTHON_SERVER_PORT = "minetest-python-server-port";
}