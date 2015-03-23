package com.tarkus.merchantmonitor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tarkus.merchantmonitor.Merc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kein on 04/02/14.
 */
public class DBAdaptor extends SQLiteOpenHelper {

    // константы для конструктора
    private static final String DATABASE_NAME = "merc_database.db";
    private static final int DATABASE_VERSION = 2;
    //название таблицы и столбцы
    public static final String TABLE_NAME = "merc_sell";
    public static final String UID = "_id";
    public static final String NAME = "name";
    public static final String ITEMS = "items";

    public DBAdaptor(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }


    // запрос для создания
    private static final String SQL_CREATE_ENTRIES = "create table if not exists " + TABLE_NAME + "( " + UID + "  integer primary key autoincrement, " + NAME + " text not null, " + ITEMS + " text not null,  UNIQUE(" + NAME + ") ON CONFLICT REPLACE);";
    // запрос для удаления
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Удаляем предыдущую таблицу при апгрейде
        db.execSQL(SQL_DELETE_ENTRIES);
        // Создаём новый экземпляр таблицы
        onCreate(db);
    }

    public void addMerchant(Merc merc){
        SQLiteDatabase wwd = this.getReadableDatabase();

        // внесение данных
        ContentValues values = new ContentValues();
        values.put(NAME, merc.getName());
        values.put(ITEMS, MerchantAdapter.encodeItemsJSON(merc.getItems()));
        try{
            wwd.insert(TABLE_NAME, null, values);
        } catch(android.database.sqlite.SQLiteConstraintException e){

        }
        wwd.close();
    }

    private Cursor get_mercs() {

        SQLiteDatabase wwd = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = wwd.rawQuery(query, null);

        return cursor;
    }

    public List<Merc> getMercs(){
        Cursor cursor = get_mercs();
        List<Merc> mercs = new ArrayList<Merc>();
        Merc merc;
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(DBAdaptor.NAME));
            merc = new Merc();
            merc.setName(name);
            String items = cursor.getString(cursor.getColumnIndex(DBAdaptor.ITEMS));
            merc.setItems(ItemAdapter.decodeJSONItems(items));
            mercs.add(merc);
        }

        cursor.close();
        return mercs;
    }

    public Merc getMercByName(String name){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE name = '" + name + "'";
        Cursor cursor = db.rawQuery(query, null);

        Merc merc = new Merc();
        merc.setName(name);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            String items = cursor.getString(cursor.getColumnIndex(DBAdaptor.ITEMS));
            merc.setItems(ItemAdapter.decodeJSONItems(items));
        }
        cursor.close();
        return merc;
    }

    public void removeMerc(Merc merc){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, NAME + "=\""+merc.getName()+"\"",null);
        db.close();
//        getWritableDatabase().delete(TABLE_NAME, NAME + "=\""+merc.getName()+"\"",null);
    }

     public void updateMerc(Merc merc){
         SQLiteDatabase db = getWritableDatabase();

         ContentValues values = new ContentValues();
         values.put(NAME, merc.getName());
         values.put(ITEMS, ItemAdapter.encodeJSONItems(merc.getItems()));

         db.insert(TABLE_NAME, null, values);
         db.replace(TABLE_NAME, NAME + "=\"" + merc.getName() + "\"", values);

         db.close();

     }
}
