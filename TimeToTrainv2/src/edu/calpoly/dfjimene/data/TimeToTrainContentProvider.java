package edu.calpoly.dfjimene.data;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Class that provides content from a SQLite database to the application.
 * Provides joke information to a ListView through a CursorAdapter. The database
 * stores jokes in a two-dimensional table, where each row is a joke and each
 * column is a property of a joke (ID, joke text, joke rating, joke author).
 * 
 * Note that CursorLoaders require a ContentProvider, which is why this
 * application wraps a SQLite database into a content provider instead of
 * managing the database<-->application transactions manually.
 */
public class TimeToTrainContentProvider extends ContentProvider {

   /** The joke database. */
   private TimeToTrainDatabaseHelper database;

   /** Values for the URIMatcher. */
   private static final int SESSIONS_ID = 10;
   private static final int SESSION_ID = 11;
   // private static final int EXERCISE_ID = 20;
   private static final int EXERCISE_ENTRIES_ID = 30;
   private static final int EXERCISE_ENTRY_ID = 31;
   private static final int SIMPLE_ENTRY_ID = 40;

   /** The authority for this content provider. */
   private static final String AUTHORITY = "edu.calpoly.dfjimene.data.contentprovider";

   /** Base path for Exercise Entries Table */
   private static final String BASE_PATH_EXERCISE_ENTRIES = TimeToTrainTables.TABLE_EXERCISE_ENTRIES;

   /** Base path for Exercise Table */
   // private static final String BASE_PATH_EXERCISES =
   // TimeToTrainTables.TABLE_EXERCISES;

   /** Base path for Session Table */
   private static final String BASE_PATH_SESSIONS = TimeToTrainTables.TABLE_SESSIONS;

   /**
    * This provider's content locations
    */
   public static final String CONTENT_STRING_EXERCISE_ENTRIES = "content://"
         + AUTHORITY + "/" + BASE_PATH_EXERCISE_ENTRIES;
   /*
    * public static final Uri CONTENT_URI_EXERCISES = Uri.parse("content://" +
    * AUTHORITY + "/" + BASE_PATH_EXERCISES);
    */
   public static final String CONTENT_STRING_SESSIONS = "content://"
         + AUTHORITY + "/" + BASE_PATH_SESSIONS;

   private static final UriMatcher sURIMatcher = new UriMatcher(
         UriMatcher.NO_MATCH);
   static {
      sURIMatcher.addURI(AUTHORITY, BASE_PATH_SESSIONS + "/sessions/#",
            SESSIONS_ID);
      sURIMatcher.addURI(AUTHORITY, BASE_PATH_SESSIONS + "/session/#",
            SESSION_ID);
      /*
       * sURIMatcher.addURI(AUTHORITY, BASE_PATH_EXERCISES + "/exercises/#",
       * EXERCISE_ID);
       */
      sURIMatcher.addURI(AUTHORITY, BASE_PATH_EXERCISE_ENTRIES + "/entries/#",
            EXERCISE_ENTRIES_ID);
      sURIMatcher.addURI(AUTHORITY, BASE_PATH_EXERCISE_ENTRIES + "/entry/#",
            EXERCISE_ENTRY_ID);
      sURIMatcher.addURI(AUTHORITY, "simpleentry/#", SIMPLE_ENTRY_ID);
   }

   @Override
   public boolean onCreate() {
      database = new TimeToTrainDatabaseHelper(this.getContext(),
            TimeToTrainDatabaseHelper.DB_NAME, null,
            TimeToTrainDatabaseHelper.DB_VER);
      return true;
   }

   /**
    * Query handler for Content Provider
    * */
   @Override
   public Cursor query(Uri uri, String[] projection, String selection,
         String[] selectionArgs, String sortOrder) {

      /** Use a helper class to perform a query for us. */
      SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

      /** Match the passed-in URI to an expected URI format. */
      int uriType = sURIMatcher.match(uri);

      String id;
      SQLiteDatabase db;
      Cursor cursor;
      switch (uriType) {
      case EXERCISE_ENTRY_ID:

         /** Make sure the projection is proper before querying. */
         checkExerciseEntryColumns(projection);
         /** Fetch the last segment of the URI, which should be an id number. */
         id = uri.getLastPathSegment();
         queryBuilder.setTables(TimeToTrainTables.TABLE_EXERCISE_ENTRIES);
         queryBuilder.appendWhere(TimeToTrainTables.EXERCISE_ENTRIES_KEY_ID
               + "=" + id);
         db = this.database.getWritableDatabase();
         cursor = queryBuilder.query(db, projection, selection, null, null,
               null, null);
         cursor.setNotificationUri(getContext().getContentResolver(), uri);

         return cursor;
      case EXERCISE_ENTRIES_ID:
         /** We are fetching all entries for a session */
         String sessionId = uri.getLastPathSegment();

         /** Make sure the projection is proper before querying. */
         checkExerciseEntryColumns(projection);

         /** Add where statement to get only entries we need */
         queryBuilder
               .appendWhere(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID
                     + "=" + sessionId);

         db = this.database.getWritableDatabase();
         queryBuilder.setTables(TimeToTrainTables.TABLE_EXERCISE_ENTRIES);
         cursor = queryBuilder.query(db, projection, selection, null, null,
               null, null);
         cursor.setNotificationUri(getContext().getContentResolver(), uri);
         return cursor;
      case SESSIONS_ID:
         checkSessionColumns(projection);
         db = this.database.getWritableDatabase();
         queryBuilder.setTables(TimeToTrainTables.TABLE_SESSIONS);
         cursor = queryBuilder.query(db, projection, selection, null, null,
               null, null);
         cursor.setNotificationUri(getContext().getContentResolver(), uri);
         return cursor;
      case SESSION_ID:
         checkSessionColumns(projection);
         db = this.database.getWritableDatabase();
         id = uri.getLastPathSegment();
         queryBuilder.setTables(TimeToTrainTables.TABLE_SESSIONS);
         queryBuilder.appendWhere(TimeToTrainTables.SESSIONS_KEY_ID + "=" + id);
         cursor = queryBuilder.query(db, projection, selection, null, null,
               null, null);
         cursor.setNotificationUri(getContext().getContentResolver(), uri);
         return cursor;
         /*
          * case EXERCISE_ID: checkExerciseColumns(projection); db =
          * this.database.getWritableDatabase();
          * queryBuilder.setTables(TimeToTrainTables.TABLE_EXERCISES); cursor =
          * queryBuilder.query(db, projection, selection, null, null, null,
          * null); cursor.setNotificationUri(getContext().getContentResolver(),
          * uri); return cursor;
          */
      case SIMPLE_ENTRY_ID:
         db = this.database.getWritableDatabase();
         id = uri.getLastPathSegment();
         queryBuilder.setTables(TimeToTrainTables.TABLE_EXERCISE_ENTRIES);
         queryBuilder
               .appendWhere(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID
                     + "=" + id);
         cursor = queryBuilder.query(db, projection, selection, null, null,
               null, null);
         return cursor;
      default:
         throw new IllegalArgumentException("Unknown URI: " + uri + " "
               + uriType);
      }

   }

   /** We don't really care about this method for this application. */
   @Override
   public String getType(Uri uri) {
      return null;
   }

   /**
    * Inserts an entry into a table. Depending on the URI, The table will be a
    * session, entry, or exercise.
    * */
   @Override
   public Uri insert(Uri uri, ContentValues values) {

      /** Open the database for writing. */
      SQLiteDatabase sqlDB = database.getWritableDatabase();
      Uri ret;
      /** Will contain the ID of the inserted joke. */
      long id = 0;

      /** Match the passed-in URI to an expected URI format. */
      int uriType = sURIMatcher.match(uri);

      /** Inserts the row in the appropriate table */
      switch (uriType) {
      case EXERCISE_ENTRY_ID:
         id = sqlDB.insert(TimeToTrainTables.TABLE_EXERCISE_ENTRIES, null,
               values);
         ret = Uri.parse(BASE_PATH_EXERCISE_ENTRIES + "/" + id);
         break;
      /*
       * case EXERCISE_ID: id = sqlDB.insert(TimeToTrainTables.TABLE_EXERCISES,
       * null, values); ret = Uri.parse(BASE_PATH_EXERCISES + "/" + id); break;
       */
      case SESSION_ID:
         id = sqlDB.insert(TimeToTrainTables.TABLE_SESSIONS, null, values);
         ret = Uri.parse(BASE_PATH_SESSIONS + "/" + id);
         break;
      default:
         throw new IllegalArgumentException("Unknown URI: " + uri);
      }

      /**
       * Alert any watchers of an underlying data change for content/view
       * refreshing.
       */
      getContext().getContentResolver().notifyChange(uri, null);

      return ret;
   }

   /**
    * Removes a row from the joke table. Given a specific URI containing a joke
    * ID, removes rows in the table that match the ID and returns the number of
    * rows removed. Since IDs are automatically incremented on insertion, this
    * will only ever remove a single row from the joke table.<br>
    * <br>
    * 
    * Overrides the built-in version of <b>delete(...)</b> provided by
    * ContentProvider.<br>
    * <br>
    * 
    * For more information, read the documentation for the built-in version of
    * this method by hovering over the method name in the method signature below
    * this comment block in Eclipse and clicking <b>delete(...)</b> in the
    * Overrides details.
    * */
   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
      SQLiteDatabase db = database.getWritableDatabase();

      int rowsDeleted = 0;
      int uriCode = sURIMatcher.match(uri);

      switch (uriCode) {
      case EXERCISE_ENTRY_ID:
         String id = uri.getLastPathSegment();
         String where = TimeToTrainTables.EXERCISE_ENTRIES_KEY_ID + "=" + id;
         rowsDeleted = db.delete(TimeToTrainTables.TABLE_EXERCISE_ENTRIES,
               where, null);
         if (rowsDeleted > 0)
            getContext().getContentResolver().notifyChange(uri, null);
         break;
      case SESSION_ID:
         String session = uri.getLastPathSegment();
         String sessionWhere = TimeToTrainTables.SESSIONS_KEY_ID + "="
               + session;
         rowsDeleted = db.delete(TimeToTrainTables.TABLE_SESSIONS,
               sessionWhere, null);
         if (rowsDeleted > 0) {
            sessionWhere = TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID
                  + "=" + session;
            rowsDeleted += db.delete(TimeToTrainTables.TABLE_EXERCISE_ENTRIES,
                  sessionWhere, null);
            getContext().getContentResolver().notifyChange(uri, null);
         }
         break;
      default:
         throw new IllegalArgumentException("Unknown URI: " + uri);
      }

      return rowsDeleted;
   }

   /** Updates a row in the TTT tables */
   @Override
   public int update(Uri uri, ContentValues values, String selection,
         String[] selectionArgs) {
      SQLiteDatabase db = database.getWritableDatabase();
      String where;
      int rowsUpdated = 0;
      String id = uri.getLastPathSegment();

      int uriCode = sURIMatcher.match(uri);

      /** Inserts the row in the appropriate table */
      switch (uriCode) {
      case EXERCISE_ENTRY_ID:
         where = TimeToTrainTables.EXERCISE_ENTRIES_KEY_ID + "=" + id;
         rowsUpdated = db.update(TimeToTrainTables.TABLE_EXERCISE_ENTRIES,
               values, where, null);
         break;
      /*
       * case EXERCISE_ID: where = TimeToTrainTables.EXERCISES_KEY_ID + "=" +
       * id; rowsUpdated = db.update(TimeToTrainTables.TABLE_EXERCISES, values,
       * where, null); break;
       */
      case SESSION_ID:
         where = TimeToTrainTables.SESSIONS_KEY_ID + "=" + id;
         rowsUpdated = db.update(TimeToTrainTables.TABLE_SESSIONS, values,
               where, null);
         break;
      default:
         throw new IllegalArgumentException("Unknown URI: " + uri);
      }

      return rowsUpdated;
   }

   /**
    * Verifies the correct set of columns to return data from when performing a
    * query.
    * 
    * @param projection
    *           The set of columns about to be queried.
    */
   private void checkExerciseEntryColumns(String[] projection) {
      String[] available = { TimeToTrainTables.EXERCISE_ENTRIES_KEY_ID,
            TimeToTrainTables.EXERCISE_ENTRIES_KEY_COMMENT,
            TimeToTrainTables.EXERCISE_ENTRIES_KEY_DISTANCE,
            TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
            TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID,
            TimeToTrainTables.EXERCISE_ENTRIES_KEY_SETS,
            TimeToTrainTables.EXERCISE_ENTRIES_KEY_TIME,
            TimeToTrainTables.EXERCISE_ENTRIES_KEY_TYPE, };

      if (projection != null) {
         HashSet<String> requestedColumns = new HashSet<String>(
               Arrays.asList(projection));
         HashSet<String> availableColumns = new HashSet<String>(
               Arrays.asList(available));

         if (!availableColumns.containsAll(requestedColumns)) {
            throw new IllegalArgumentException("Unknown columns in projection");
         }
      }
   }

   /*
    * private void checkExerciseColumns(String[] projection) { String[]
    * available = { TimeToTrainTables.EXERCISE_ENTRIES_KEY_ID,
    * TimeToTrainTables.EXERCISES_KEY_ID, TimeToTrainTables.EXERCISES_KEY_NAME,
    * TimeToTrainTables.EXERCISES_KEY_TYPE,
    * TimeToTrainTables.EXERCISES_KEY_VARIANT };
    * 
    * if (projection != null) { HashSet<String> requestedColumns = new
    * HashSet<String>( Arrays.asList(projection)); HashSet<String>
    * availableColumns = new HashSet<String>( Arrays.asList(available));
    * 
    * if (!availableColumns.containsAll(requestedColumns)) { throw new
    * IllegalArgumentException("Unknown columns in projection"); } } }
    */

   private void checkSessionColumns(String[] projection) {
      String[] available = { TimeToTrainTables.EXERCISE_ENTRIES_KEY_ID,
            TimeToTrainTables.SESSIONS_KEY_DATE,
            TimeToTrainTables.SESSIONS_KEY_ID,
            TimeToTrainTables.SESSIONS_KEY_NAME };

      if (projection != null) {
         HashSet<String> requestedColumns = new HashSet<String>(
               Arrays.asList(projection));
         HashSet<String> availableColumns = new HashSet<String>(
               Arrays.asList(available));

         if (!availableColumns.containsAll(requestedColumns)) {
            throw new IllegalArgumentException("Unknown columns in projection");
         }
      }
   }
}
