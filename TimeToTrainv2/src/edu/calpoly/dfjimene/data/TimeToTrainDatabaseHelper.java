package edu.calpoly.dfjimene.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class TimeToTrainDatabaseHelper extends SQLiteOpenHelper{
   
   /** Database name */
   public static final String DB_NAME = "timetotrain.db";
   
   /** Database version */
   public static final int DB_VER = 3;
   
   /**
    * Create a helper object to create, open, and/or manage a database.
    * 
    * @param context
    *                The application context.
    * @param name
    *                The name of the database.
    * @param factory
    *                Factory used to create a cursor. Set to null for default behavior.
    * @param version
    *                The starting database version.
    */
   public TimeToTrainDatabaseHelper(Context context, String name,
      CursorFactory factory, int version) {
      super(context, name, null, version);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      TimeToTrainTables.onCreate(db);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      TimeToTrainTables.onUpgrade(db, oldVersion, newVersion);
      
   }
}
