package bldg5.jj.findanontelecom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

// https://examples.javacodegeeks.com/android/core/database/android-database-example/
public class FanTelSQLiteHelper extends SQLiteOpenHelper {
    private static final int database_VERSION = 1;
    private static final String database_NAME = "FANTEL";

    private static final String table_main = "FantelMain";
    private static final String tco_id = "OptionsID";
    private static final String tco_global_id = "GlobalID";
    private static final String tco_lat = "Latitude";
    private static final String tco_long = "Longitude";
    private static final String tco_user_id = "UserID";
    private static final String tco_date_tagged = "DateTagged";
    private static final String tco_date_untagged = "DateUntagged";

    private static final String table_user = "User";
    private static final String user_id = "UserID";
    private static final String date_created = "DateCreated";

    private static final String[] COLUMNS = { tco_id, tco_global_id, tco_lat, tco_long, tco_user_id, tco_date_tagged, tco_date_untagged };
    private static final String[] USER_COLUMNS = { user_id, date_created };
    public String UserID = "";

    public FanTelSQLiteHelper(Context context) {
        super(context, database_NAME, null, database_VERSION);

        if (context != null) {
            UserID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MAIN_TABLE = "CREATE TABLE 'FantelMain' ('OptionsID' INTEGER, 'GlobalID' TEXT, 'Latitude' TEXT, 'Longitude' TEXT, 'UserID' TEXT, 'DateTagged' TEXT, 'DateUntagged' TEXT, PRIMARY KEY(OptionsID));";
        String CREATE_USER_TABLE = "CREATE TABLE 'User' ('UserID' String, 'DateCreated' TEXT);";

        db.execSQL(CREATE_MAIN_TABLE);
        db.execSQL(CREATE_USER_TABLE);

        // we're creating the database, so create a user id.
        // we have to pass a ref to the db to avoid "getDatabase called recursively"
        createUser(db, UserID);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS FantelMain");
        // the user db wont get upgraded, it's just userid and created date.
        this.onCreate(db);
    }

    public void createTCODb(TCODb tc)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        // make values to be inserted
        ContentValues values = new ContentValues();
        values.put(tco_lat, tc.getLatitude());
        values.put(tco_long, tc.getLongitude());
        values.put(tco_user_id, tc.getUserID());
        values.put(tco_date_tagged, tc.getDateTagged());
        values.put(tco_date_untagged, tc.getDateUntagged());

        // insert tc option
        db.insert(table_main, null, values);

        // close database transaction
        db.close();
    }

    public void createUser(SQLiteDatabase db, String strUserID)
    {
        // first, get the current date.
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        // make values to be inserted
        ContentValues values = new ContentValues();
        values.put(user_id, strUserID);
        values.put(date_created, dateFormat.format(date));

        // insert tc option
        db.insert(table_user, null, values);
    }

    public TCODb readTCO(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(table_main, COLUMNS, " OptionsID = ?", new String[] { String.valueOf(id) }, null, null, null, null);

        // if results !=null, parse the first one
        if (cursor != null)
            cursor.moveToFirst();

        TCODb tco = new TCODb();
        tco.setOptionsID(id);
        tco.setGlobalID(cursor.getString(1));
        tco.setLatitude(cursor.getDouble(2));
        tco.setLongitude(cursor.getDouble(3));
        tco.setUserID(cursor.getString(4));
        tco.setDateTagged(cursor.getString(5));
        tco.setDateUntagged(cursor.getString(6));
        return tco;
    }

    public List getAllTCOs() {
        List tcos = new LinkedList();
        String query = "SELECT  * FROM " + table_main;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        TCODb tco = null;

        if (cursor.moveToFirst()) {
            do {
                tco = new TCODb();
                tco.setOptionsID(cursor.getInt(0));
                tco.setGlobalID(cursor.getString(1));
                tco.setLatitude(cursor.getDouble(2));
                tco.setLongitude(cursor.getDouble(3));
                tco.setUserID(cursor.getString(4));
                tco.setDateTagged(cursor.getString(5));
                tco.setDateUntagged(cursor.getString(6));

                tcos.add(tco);
            } while (cursor.moveToNext());
        }
        return tcos;
    }

    /* public int updateTCO(TCODb tcoDb) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("lat", tcoDb.getLatitude());
        values.put("long", tcoDb.getLongitude());

        // update
        int i = db.update(table_main, values, tco_id + " = ?", new String[] { String.valueOf(tcoDb.getOptionsID()) });

        db.close();
        return i;
    }*/

    public void deleteTCO(TCODb tcoDb) {
        // get today's date for DateUntagged
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();

        SQLiteDatabase db = this.getWritableDatabase();

        // db.delete(table_main, tco_id + " = ?", new String[] { String.valueOf(tcoDb.getOptionsID()) });
        // soft delete
        ContentValues values = new ContentValues();
        values.put("DateUntagged", dateFormat.format(date));
        db.update(table_main, values, tco_id + " = ?", new String[] { String.valueOf(tcoDb.getOptionsID()) });

        db.close();
    }
}
