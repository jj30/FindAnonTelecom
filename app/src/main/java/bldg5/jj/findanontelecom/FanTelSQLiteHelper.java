package bldg5.jj.findanontelecom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

// https://examples.javacodegeeks.com/android/core/database/android-database-example/
public class FanTelSQLiteHelper extends SQLiteOpenHelper {
    private static final int database_VERSION = 1;
    private static final String database_NAME = "FANTEL";
    private static final String table_main = "FantelMain";

    private static final String tco_id = "ID";
    private static final String tco_lat = "Lat";
    private static final String tco_long = "Long";
    private static final String tco_active = "Active";

    private static final String[] COLUMNS = { tco_id, tco_lat, tco_long, tco_active };

    public FanTelSQLiteHelper(Context context) {
        super(context, database_NAME, null, database_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FANTEL_TABLE = "CREATE TABLE 'FantelMain' ('ID' INTEGER, 'Lat' NUMERIC, 'Long' NUMERIC, 'Active' INTEGER, PRIMARY KEY(ID));";
        db.execSQL(CREATE_FANTEL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS FantelMain");
        this.onCreate(db);
    }

    public void createTCOption(TCOption tc)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        // make values to be inserted
        ContentValues values = new ContentValues();
        values.put(tco_lat, tc.getLat());
        values.put(tco_long, tc.getLong());
        values.put(tco_active, tc.getActive());

        // insert tc option
        db.insert(table_main, null, values);

        // close database transaction
        db.close();
    }

    public TCOption readTCO(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(table_main, COLUMNS, " ID = ?", new String[] { String.valueOf(id) }, null, null, null, null);

        // if results !=null, parse the first one
        if (cursor != null)
            cursor.moveToFirst();

        TCOption tco = new TCOption();
        tco.setLat(cursor.getDouble(1));
        tco.setLat(cursor.getDouble(2));
        tco.setActive(cursor.getInt(3));
        return tco;
    }

    public List getAllTCOs() {
        List tcos = new LinkedList();
        String query = "SELECT  * FROM " + table_main;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        TCOption tco = null;

        if (cursor.moveToFirst()) {
            do {
                tco = new TCOption();
                tco.setLat(cursor.getDouble(1));
                tco.setLong(cursor.getDouble(2));
                tco.setActive(cursor.getInt(3));

                tcos.add(tco);
            } while (cursor.moveToNext());
        }
        return tcos;
    }

    public int updateTCO(TCOption tcOption) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", tcOption.getLong());
        values.put("author", tcOption.getLat());
        // update
        int i = db.update(table_main, values, tco_id + " = ?", new String[] { String.valueOf(tcOption.getID()) });

        db.close();
        return i;
    }

    public void deleteTCO(TCOption tcOption) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(table_main, tco_id + " = ?", new String[] { String.valueOf(tcOption.getID()) });
        db.close();
    }

}
