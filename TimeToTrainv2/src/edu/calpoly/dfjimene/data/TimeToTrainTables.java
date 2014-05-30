package edu.calpoly.dfjimene.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * This class contains all of the table information for the TTT database. This
 * class also manages the specification of the tables based on database version
 * 
 * @author Douglas Jimenez
 * 
 */
public class TimeToTrainTables {

	/** Name for Exercise Entry Table */
	public static final String TABLE_EXERCISE_ENTRIES = "exercise_entries";

	/** Name for Session Table */
	public static final String TABLE_SESSIONS = "sessions";

	/** Name for Exercise Table */
	public static final String TABLE_EXERCISES = "exercises";

	/** Column names and IDs for Exercise entry Table */
	public static final String EXERCISE_ENTRIES_KEY_ID = "_id";
	public static final int EXERCISE_ENTRIES_COL_ID = 0;

	public static final String EXERCISE_ENTRIES_KEY_EXERCISE_NAME = "exercise_name";
	public static final int EXERCISE_ENTRIES_COL_EXERCISE_NAME = EXERCISE_ENTRIES_COL_ID + 2;

	// Foreign Key to Session table
	public static final String EXERCISE_ENTRIES_KEY_SESSION_ID = "session_id";
	public static final int EXERCISE_ENTRIES_COL_SESSION_ID = EXERCISE_ENTRIES_COL_ID + 1;

	// Keys for Exercise entry. Different fields are populated based on type
	public static final String EXERCISE_ENTRIES_KEY_TYPE = "type";
	public static final int EXERCISE_ENTRIES_COL_TYPE = EXERCISE_ENTRIES_COL_ID + 3;

	public static final String EXERCISE_ENTRIES_KEY_SETS = "sets";
	public static final int EXERCISE_ENTRIES_COL_SETS = EXERCISE_ENTRIES_COL_ID + 4;

	public static final String EXERCISE_ENTRIES_KEY_TIME = "time";
	public static final int EXERCISE_ENTRIES_COL_TIME = EXERCISE_ENTRIES_COL_ID + 5;

	public static final String EXERCISE_ENTRIES_KEY_DISTANCE = "distance";
	public static final int EXERCISE_ENTRIES_COL_DISTANCE = EXERCISE_ENTRIES_COL_ID + 6;

	public static final String EXERCISE_ENTRIES_KEY_COMMENT = "comments";
	public static final int EXERCISE_ENTRIES_COL_COMMENT = EXERCISE_ENTRIES_COL_ID + 7;

	/** Column names and IDs for Sessions Table */
	public static final String SESSIONS_KEY_ID = "_id";
	public static final int SESSIONS_COL_ID = 0;

	public static final String SESSIONS_KEY_NAME = "name";
	public static final int SESSIONS_COL_NAME = SESSIONS_COL_ID + 1;

	public static final String SESSIONS_KEY_DATE = "session_date";
	public static final int SESSIONS_COL_DATE = SESSIONS_COL_ID + 2;

	/** Column numbers for simple entry Table */
	public static final int SIMPLE_ENTRIES_COL_ID = 0;

	public static final int SIMPLE_ENTRIES_COL_SESSION_ID = SIMPLE_ENTRIES_COL_ID + 1;

	public static final int SIMPLE_ENTRIES_COL_EXERCISE_NAME = SIMPLE_ENTRIES_COL_ID + 2;

	public static final int SIMPLE_ENTRIES_COL_TYPE = SIMPLE_ENTRIES_COL_ID + 3;

	/** Raw SQLite for creating Entry table */
	public static final String CREATE_EXERCISE_ENTRY_TABLE = "CREATE TABLE IF NOT"
			+ " EXISTS "
			+ TABLE_EXERCISE_ENTRIES
			+ " ("
			+ EXERCISE_ENTRIES_KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ EXERCISE_ENTRIES_KEY_SESSION_ID
			+ " INTEGER NOT NULL, "
			+ EXERCISE_ENTRIES_KEY_EXERCISE_NAME
			+ " TEXT NOT NULL, "
			+ EXERCISE_ENTRIES_KEY_TYPE
			+ " INTEGER NOT NULL, "
			+ EXERCISE_ENTRIES_KEY_SETS
			+ " TEXT DEFAULT 0, "
			+ EXERCISE_ENTRIES_KEY_TIME
			+ " INTEGER DEFAULT 0, "
			+ EXERCISE_ENTRIES_KEY_DISTANCE
			+ " REAL DEFAULT 0, "
			+ EXERCISE_ENTRIES_KEY_COMMENT
			+ " TEXT, "
			+ "FOREIGN KEY("
			+ EXERCISE_ENTRIES_KEY_SESSION_ID
			+ ") REFERENCES "
			+ TABLE_SESSIONS + " (" + SESSIONS_KEY_ID + "));";

	/** Raw SQLite for creating Session table */
	public static final String CREATE_SESSION_TABLE = "CREATE TABLE IF NOT"
			+ " EXISTS " + TABLE_SESSIONS + " (" + SESSIONS_KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + SESSIONS_KEY_NAME
			+ " TEXT, " + SESSIONS_KEY_DATE + " TEXT NOT NULL);";

	/**
	 * SQLite database table removal statements. Only used if upgrading
	 * database.
	 */
	public static final String TABLE_EXERCISE_ENTRIES_DROP = "drop table if exists "
			+ TABLE_EXERCISE_ENTRIES;
	public static final String TABLE_EXERCISES_DROP = "drop table if exists "
			+ TABLE_EXERCISES;
	public static final String TABLE_SESSIONS_DROP = "drop table if exists "
			+ TABLE_SESSIONS;

	/**
	 * Initializes the database.
	 * 
	 * @param database
	 *            The database to initialize.
	 */
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_SESSION_TABLE);
		database.execSQL(CREATE_EXERCISE_ENTRY_TABLE);
	}

	/**
	 * Upgrades the database to a new version.
	 * 
	 * @param database
	 *            The database to upgrade.
	 * @param oldVersion
	 *            The old version of the database.
	 * @param newVersion
	 *            The new version of the database.
	 */
	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(TimeToTrainTables.class.getName(),
				"The database is being destroyed and upgraded...");
		database.execSQL(TABLE_EXERCISE_ENTRIES_DROP);
		database.execSQL(TABLE_EXERCISES_DROP);
		database.execSQL(TABLE_SESSIONS_DROP);
		onCreate(database);
	}
}
