package si.virag.parkomat.models;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = ParkomatDatabase.NAME, version = ParkomatDatabase.VERSION)
public class ParkomatDatabase {

    public static final String NAME = "ParkomatDB";
    public static final int VERSION = 1;

}
