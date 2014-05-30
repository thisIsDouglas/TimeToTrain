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
 * Provides a wrapper for the database so that the application can query,
 * insert, update or delete rows with ease
 */
public class TimeToTrainContentProvider extends ContentProvider {

	/** The TTT database. */
	private TimeToTrainDatabaseHelper database;

	/** Values for the URIMatcher. */
	/** All sessions */
	private static final int SESSIONS_ID = 10;
	/** Specific session */
	private static final int SESSION_ID = 11;
	/** All entries for a session */
	private static final int EXERCISE_ENTRIES_ID = 30;
	/** Specific entry */
	private static final int EXERCISE_ENTRY_ID = 31;
	/** Simple entry */
	private static final int SIMPLE_ENTRY_ID = 40;

	/** The authority for this content provider. */
	private static final String AUTHORITY = "edu.calpoly.dfjimene.data.contentprovider";

	/** Base path for Exercise Entries Table */
	private static final String BASE_PATH_EXERCISE_ENTRIES = TimeToTrainTables.TABLE_EXERCISE_ENTRIES;

	/** Base path for Session Table */
	private static final String BASE_PATH_SESSIONS = TimeToTrainTables.TABLE_SESSIONS;

	/** This provider's entries location */
	public static final String CONTENT_STRING_EXERCISE_ENTRIES = "content://"
			+ AUTHORITY + "/" + BASE_PATH_EXERCISE_ENTRIES;

	/** This provider's sessions location */
	public static final String CONTENT_STRING_SESSIONS = "content://"
			+ AUTHORITY + "/" + BASE_PATH_SESSIONS;

	/** URI matcher for the content provider */
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH_SESSIONS + "/sessions/#",
				SESSIONS_ID);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH_SESSIONS + "/session/#",
				SESSION_ID);
		sURIMatcher.addURI(AUTHORITY,
				BASE_PATH_EXERCISE_ENTRIES + "/entries/#", EXERCISE_ENTRIES_ID);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH_EXERCISE_ENTRIES + "/entry/#",
				EXERCISE_ENTRY_ID);
		sURIMatcher.addURI(AUTHORITY, "simpleentry/#", SIMPLE_ENTRY_ID);
	}

	@Override
	public boolean onCreate() {
		// Create the database when the provider is created
		database = new TimeToTrainDatabaseHelper(this.getContext(),
				TimeToTrainDatabaseHelper.DB_NAME, null,
				TimeToTrainDatabaseHelper.DB_VER);
		return true;
	}

	/**
	 * Handles queries based on the URI passed
	 * */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Declare and initialize queryBuilder and attempt to match the URI to
		// an accepted URI
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		/** Match the passed-in URI to an expected URI format. */
		int uriType = sURIMatcher.match(uri);

		String id;
		SQLiteDatabase db;
		Cursor cursor;

		// Execute appropriate query based on passed URI
		switch (uriType) {
		case EXERCISE_ENTRY_ID:

			// Check projection for correct
			checkExerciseEntryColumns(projection);

			// Use last path segment as the entry ID argument for the WHERE
			// phrase and query. Return the resulting cursor
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

			// We are fetching all entries for a session
			String sessionId = uri.getLastPathSegment();

			// Make sure the projection is proper before querying
			checkExerciseEntryColumns(projection);

			// Add where statement to get only entries we need for the session
			queryBuilder
					.appendWhere(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID
							+ "=" + sessionId);

			// Query and return the cursor
			db = this.database.getWritableDatabase();
			queryBuilder.setTables(TimeToTrainTables.TABLE_EXERCISE_ENTRIES);
			cursor = queryBuilder.query(db, projection, selection, null, null,
					null, null);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		case SESSIONS_ID:

			// Check sessions table columns, then query and return the cursor
			checkSessionColumns(projection);
			db = this.database.getWritableDatabase();
			queryBuilder.setTables(TimeToTrainTables.TABLE_SESSIONS);
			cursor = queryBuilder.query(db, projection, selection, null, null,
					null, null);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		case SESSION_ID:

			// We are querying for a specific session. Check the columns, append
			// the WHERE statement for session ID, query, and then return the
			// cursor
			checkSessionColumns(projection);
			db = this.database.getWritableDatabase();
			id = uri.getLastPathSegment();
			queryBuilder.setTables(TimeToTrainTables.TABLE_SESSIONS);
			queryBuilder.appendWhere(TimeToTrainTables.SESSIONS_KEY_ID + "="
					+ id);
			cursor = queryBuilder.query(db, projection, selection, null, null,
					null, null);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		case SIMPLE_ENTRY_ID:

			// Getting an abridged query of entries with reduced columns for a
			// session. Query and return the cursor
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
	 * Inserts a row into a table. Depending on the URI, The table will be a
	 * session, entry, or exercise.
	 * */
	@Override
	public Uri insert(Uri uri, ContentValues values) {

		// Open the database for writing
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		Uri ret;

		long id = 0;

		// Match the passed-in URI to an expected URI format
		int uriType = sURIMatcher.match(uri);

		// Insert based on URI type
		switch (uriType) {
		case EXERCISE_ENTRY_ID:

			// Inserting an entry. Call insert using the content values and
			// return the resulting URI containing the new entry ID
			id = sqlDB.insert(TimeToTrainTables.TABLE_EXERCISE_ENTRIES, null,
					values);
			ret = Uri.parse(BASE_PATH_EXERCISE_ENTRIES + "/" + id);
			break;
		case SESSION_ID:

			// Inserting a session. Call insert using the content values and
			// return the resulting URI containing the ID
			id = sqlDB.insert(TimeToTrainTables.TABLE_SESSIONS, null, values);
			ret = Uri.parse(BASE_PATH_SESSIONS + "/" + id);
			break;

		// unknown URI
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
	 * Removes a row from the specified table and then returns the number of
	 * rows affected
	 * */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		// Open the DB and match the URI type
		SQLiteDatabase db = database.getWritableDatabase();

		int rowsDeleted = 0;
		int uriCode = sURIMatcher.match(uri);

		switch (uriCode) {
		case EXERCISE_ENTRY_ID:

			// Deleting an entry. Append the entry ID as a WHERE and call
			// delete. Return rows deleted
			String id = uri.getLastPathSegment();
			String where = TimeToTrainTables.EXERCISE_ENTRIES_KEY_ID + "=" + id;
			rowsDeleted = db.delete(TimeToTrainTables.TABLE_EXERCISE_ENTRIES,
					where, null);
			if (rowsDeleted > 0)
				getContext().getContentResolver().notifyChange(uri, null);
			break;
		case SESSION_ID:

			// Session is being deleted. First, delete all entries associated
			// with the session, then delete the session. Return rows deleted
			String session = uri.getLastPathSegment();
			String sessionWhere = TimeToTrainTables.SESSIONS_KEY_ID + "="
					+ session;
			rowsDeleted = db.delete(TimeToTrainTables.TABLE_SESSIONS,
					sessionWhere, null);
			if (rowsDeleted > 0) {
				sessionWhere = TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID
						+ "=" + session;
				rowsDeleted += db.delete(
						TimeToTrainTables.TABLE_EXERCISE_ENTRIES, sessionWhere,
						null);
				getContext().getContentResolver().notifyChange(uri, null);
			}
			break;

		// Unknown URI
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		return rowsDeleted;
	}

	/** Updates a row in the TTT tables */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		// Open the DB, grab the ID, and match the URI
		SQLiteDatabase db = database.getWritableDatabase();
		String where;
		int rowsUpdated = 0;
		String id = uri.getLastPathSegment();

		int uriCode = sURIMatcher.match(uri);

		/** Inserts the row in the appropriate table */
		switch (uriCode) {
		case EXERCISE_ENTRY_ID:

			// Updating an entry. Append the ID to the WHERE clause and query
			// with the content values provided. Then return the number of rows
			// updated
			where = TimeToTrainTables.EXERCISE_ENTRIES_KEY_ID + "=" + id;
			rowsUpdated = db.update(TimeToTrainTables.TABLE_EXERCISE_ENTRIES,
					values, where, null);
			break;
		case SESSION_ID:

			// Updating a session. Append the ID to the WHERE clause and query
			// with the content values provided. Then return the number of rows
			// updated
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
	 *            The set of columns about to be queried.
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
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}

	/**
	 * Verifies the correct set of columns to return data from when performing a
	 * query.
	 * 
	 * @param projection
	 *            The set of columns about to be queried.
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
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
