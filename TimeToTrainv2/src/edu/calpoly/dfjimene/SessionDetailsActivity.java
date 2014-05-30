package edu.calpoly.dfjimene;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
// import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.calpoly.dfjimene.data.TimeToTrainContentProvider;
import edu.calpoly.dfjimene.data.TimeToTrainTables;
import edu.calpoly.dfjimene.exerciseentry.ExerciseEntry;
import edu.calpoly.dfjimene.exerciseentry.SimpleEntry;
import edu.calpoly.dfjimene.exerciseentry.SimpleEntryCursorAdapter;
import edu.calpoly.dfjimene.exerciseentry.SimpleEntryView;
import edu.calpoly.dfjimene.exerciseentry.SimpleEntryView.OnSimpleEntryChangeListener;

/**
 * Activity for viewing a session and entries associated with it.
 * 
 * @author Douglas Jimenez
 * 
 */
public class SessionDetailsActivity extends SherlockFragmentActivity implements
		LoaderCallbacks<Cursor>, OnSimpleEntryChangeListener {

	/** Session ID this activity concerns */
	private long m_sessionId;

	/** Adapter for Simple Entries */
	private SimpleEntryCursorAdapter m_simpleEntryAdapter;

	/** View holding simple entries */
	private ListView m_simpleEntryList;

	/** The activity's context */
	private Context m_context;

	/** Extras key for entry id */
	public static final String ENTRY_ID = "entry_id";

	/** Intent Session ID key string to pass in the intent */
	public static final String SESSION_ID = "session_id";

	/** Uri String for querying for the session's title */
	private static final String SESSION_CONTENT_STRING = "content://edu.calpoly.dfjimene.data.contentprovider/sessions/session/";

	/** Uri String for getting simple entry information */
	private static final String SIMPLE_ENTRY_CONTENT_STRING = "content://edu.calpoly.dfjimene.data.contentprovider/simpleentry/";

	/** Loader ID for Session details activity */
	private static final int LOADER_ID = 2;

	/** Entry position for deleting */
	private int m_entryPosition = -1;

	/** Selected simple entry view */
	private SimpleEntryView m_simpleEntryView;

	/** CAB for deleting entries */
	private ActionMode m_actionMode;
	private Callback m_actionModeCallback;

	/** Button for adding an entry */
	private Button m_buttonAdd;

	/** Content uri string for entries */
	private static final String ENTRY_CONTENT_STRING = TimeToTrainContentProvider.CONTENT_STRING_EXERCISE_ENTRIES
			+ "/entry/";

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// Always call super's onCreate
		super.onCreate(savedInstanceState);

		// Grab the session ID from the extras and customize the title based on
		// the session's name
		this.m_sessionId = getIntent().getLongExtra(
				SessionListActivity.INTENT_SESSION_ID, -1);
		changeTitleForSession(m_sessionId);

		// Initialize the entry adapter with a new cursor adapter and initialize
		// the cursor loader
		this.m_simpleEntryAdapter = new SimpleEntryCursorAdapter(this, null, 0);
		getSupportLoaderManager().initLoader(LOADER_ID, null, this);
		this.m_simpleEntryAdapter.setOnSimpleEntryChangeListener(this);

		// Initialize the view and initialize the controller for UI components
		setContentView(R.layout.session_entry_list);
		this.m_context = this;
		this.m_simpleEntryList = (ListView) findViewById(R.id.entry_list);
		this.m_simpleEntryList.setAdapter(m_simpleEntryAdapter);
		this.m_buttonAdd = (Button) findViewById(R.id.view_add_entry);

		// Set the background
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		LinearLayout layout = (LinearLayout) m_simpleEntryList.getParent();
		Resources res = getResources();
		Drawable d = res.getDrawable(R.drawable.background_icon);
		d.setAlpha(45);
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			layout.setBackgroundDrawable(d);
		} else {
			layout.setBackground(d);
		}
		initListeners();

	}
	
	@Override
	protected void onResume(Bundle savedInstanceState){
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Initialize action bar
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.details_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Behave according to the action bar item clicked
		switch (item.getItemId()) {
		case R.id.details_help:

			// Help was clicked. Make a toast message with a tip and return
			Toast.makeText(
					this,
					"Please select an entry to view and modify."
							+ "To delete an existing entry, tap and hold the entry"
							+ " you no longer want and tap \"Delete Entry\"",
					Toast.LENGTH_LONG).show();
			return true;
		case android.R.id.home:

			// Home was clicked. Navigate to parent
			NavUtils.navigateUpTo(this, new Intent(this,
					SessionListActivity.class));
			return true;
		case R.id.detailsmenusurvey:

			// The survey button was clicked. Start the browser navigating to
			// the google docs form
			Intent browserIntent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://docs.google.com/forms/d/1PC11epczRfKb4XZgrmHp0NlY8CrUCMXYR350pH5NpqM/viewform?usp=send_form"));
			startActivity(browserIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Initialize all listeners associated with UI components
	 */
	private void initListeners() {

		// Set the on click listener for the add new entry button to initialize
		// an intent for starting the add entry activity and then starting it
		this.m_buttonAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(m_context, AddEntryActivity.class);
				i.putExtra(SESSION_ID, m_sessionId);
				startActivity(i);
			}
		});

		// Set the on long click listener for the entry list view to open a new
		// callback allowing the user to delete entries
		this.m_simpleEntryList
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						if (m_actionMode != null) {
							return false;
						}
						m_actionMode = getSherlock().startActionMode(
								m_actionModeCallback);
						m_entryPosition = position;
						return true;
					}
				});

		// Initialize the callback
		this.m_actionModeCallback = new Callback() {

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				m_actionMode = null;
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {

				// Inflate the contextual menu into the action bar
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.details_action_menu, menu);
				return true;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				// Grab the entry and check which button was pressed
				m_simpleEntryView = ((SimpleEntryView) m_simpleEntryList
						.getChildAt(m_entryPosition));
				if (m_simpleEntryView == null) {
					return false;
				}
				switch (item.getItemId()) {
				case R.id.delete_entry:
					// Remove the entry from the DB and refresh the data
					Uri uri = Uri.parse(ENTRY_CONTENT_STRING
							+ m_simpleEntryView.getSimpleEntry().getEntryID());
					getContentResolver().delete(uri, null, null);
					fillData();
					m_simpleEntryView = null;
					m_entryPosition = -1;
					mode.finish();
					return true;
				default:
					return false;
				}
			}
		};
		// Initialize the entry list's on item click listener which allows
		// entries to be selected and viewed
		this.m_simpleEntryList
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						// Grab the appropriate entry and initialize an intent
						// based on the exercise type of the entry
						SimpleEntryView sView = (SimpleEntryView) view;
						Intent intent;
						if (sView.getSimpleEntry().getType() == ExerciseEntry.TYPE_CARDIO) {
							intent = new Intent(m_context,
									ViewCardioEntryActivity.class);
						} else {
							intent = new Intent(m_context,
									ViewStrengthEntryActivity.class);
						}
						// Add the entry ID and session ID to the extras and
						// then go to the appropriate entry view
						intent.putExtra("entry", sView.getSimpleEntry()
								.getEntryID());
						intent.putExtra("session", sView.getSimpleEntry()
								.getSessionID());
						startActivity(intent);

					}
				});
	}

	/**
	 * Changes the title to be the session name or the date if no name is
	 * provided
	 * 
	 * @param sessionId
	 *            the session ID
	 */
	private void changeTitleForSession(long sessionId) {

		// Initialize the projection of columns I want and then query for the
		// session
		String[] projection = { TimeToTrainTables.SESSIONS_KEY_ID,
				TimeToTrainTables.SESSIONS_KEY_NAME,
				TimeToTrainTables.SESSIONS_KEY_DATE };
		Cursor cursor = getContentResolver().query(
				Uri.parse(SESSION_CONTENT_STRING + sessionId), projection,
				null, null, null);
		// Set the title based on if the name is set
		cursor.moveToFirst();
		String newTitle = cursor.getString(TimeToTrainTables.SESSIONS_COL_NAME);
		if (newTitle != null && !newTitle.equals(""))
			setTitle(newTitle);
		else
			setTitle(cursor.getString(TimeToTrainTables.SESSIONS_COL_DATE));
		cursor.close();
	}

	/**
	 * Refreshes the data being fed to the adapter
	 */
	public void fillData() {
		getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
		m_simpleEntryList.setAdapter(m_simpleEntryAdapter);
	}

	@Override
	public void onSimpleEntryChanged(SimpleEntryView view, SimpleEntry entry) {
		// For now we do not need a listener for the simple entry. I put
		// this here for in the event that I do, I can add to it.

	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		// Create the projection and initialize the loader using the URI and
		// projection
		String projection[] = { TimeToTrainTables.EXERCISE_ENTRIES_KEY_ID,
				TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID,
				TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
				TimeToTrainTables.EXERCISE_ENTRIES_KEY_TYPE };
		Uri uri = Uri.parse(SIMPLE_ENTRY_CONTENT_STRING + m_sessionId);
		CursorLoader loader = new CursorLoader(this, uri, projection, null,
				null, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		
		// Swap the cursors in the cursor adapter
		this.m_simpleEntryAdapter.swapCursor(arg1);
		this.m_simpleEntryAdapter.setOnSimpleEntryChangeListener(this);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// Remove old cursor
		this.m_simpleEntryAdapter.swapCursor(null);

	}

}
