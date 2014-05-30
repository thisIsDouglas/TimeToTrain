package edu.calpoly.dfjimene;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.ActionMode.Callback;

import edu.calpoly.dfjimene.data.TimeToTrainTables;
import edu.calpoly.dfjimene.session.Session;
import edu.calpoly.dfjimene.session.SessionCursorAdapter;
import edu.calpoly.dfjimene.session.SessionView;
import edu.calpoly.dfjimene.session.SessionView.OnSessionChangeListener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
// import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Activity that displays the session list for users to manage
 * 
 * @author Douglas Jimenez
 * 
 */
public class SessionListActivity extends SherlockFragmentActivity implements
		LoaderCallbacks<Cursor>, OnSessionChangeListener {

	/** Local context for class */
	private Context context = this;

	/** Adapter for SessionsList */
	private SessionCursorAdapter m_sessionAdapter;

	/** Session ListView */
	private ListView m_sessionListView;

	/** Button for adding a Session */
	private Button m_addSession;

	/** EditText Field for naming a session */
	private EditText m_vwSessionName;

	/** Loader ID for session content loader */
	private static final int LOADER_ID = 1;

	private String m_strSessionName = "";

	/** Target SessionView for callbacks */
	private SessionView m_sessionView;

	/** Uri String for querying for sessions */
	private static final String SESSION_CONTENT_STRING = "content://edu.calpoly.dfjimene.data.contentprovider/sessions/sessions/";

	/** Uri String for inserting sessions */
	private static final String CHANGE_SESSION_CONTENT_STRING = "content://edu.calpoly.dfjimene.data.contentprovider/sessions/session/";

	/** Label for string to be placed into intent */
	public static final String INTENT_SESSION_ID = "SESSION_ID";

	/** Selected Session position */
	private int m_sessionPosition;

	/**
	 * Used to handle Contextual Action Mode when long-clicking on a session.
	 */
	private Callback mActionModeCallback;
	private ActionMode mActionMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Always call super's onCreate
		super.onCreate(savedInstanceState);

		// Cancel this activity if it should not be started
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			finish();
			return;
		}

		// Initialize the adapter and the cursor loader and the session
		// adapter's session changed listener
		this.m_sessionAdapter = new SessionCursorAdapter(this, null, 0);
		getSupportLoaderManager().initLoader(LOADER_ID, null, this);
		this.m_sessionAdapter.setOnSessionChangeListener(this);

		// Initialize the layout
		initLayout();
	}

	/**
	 * Initializes the layout for the Session List Activity. Warnings and lint
	 * are checked within the code, so they are suppressed.
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	protected void initLayout() {

		// Initialize the view and bind UI components to member variables
		setContentView(R.layout.session_list);
		this.m_sessionListView = (ListView) findViewById(R.id.sessions_list);
		this.m_sessionListView.setAdapter(m_sessionAdapter);
		this.m_addSession = (Button) findViewById(R.id.view_add_session);

		// Initialize listeners and callback
		initAddSessionListener();
		this.m_sessionListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						if (mActionMode != null) {
							return false;
						}
						mActionMode = getSherlock().startActionMode(
								mActionModeCallback);
						m_sessionPosition = position;
						return true;
					}

				});
		this.m_sessionListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						// Create an intent adding the session id to the extras
						// and start the session details activity
						Intent intent = new Intent(context,
								SessionDetailsActivity.class);
						SessionView sView = (SessionView) view;
						intent.putExtra(INTENT_SESSION_ID, sView.getSession()
								.getID());
						startActivity(intent);

					}
				});
		this.mActionModeCallback = new Callback() {

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				mActionMode = null;

			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {

				// Inflate CAB menu into the action bar
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.session_action_menu, menu);
				return true;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				// Grab the session and decide which button was clicked
				m_sessionView = ((SessionView) m_sessionListView
						.getChildAt(m_sessionPosition));
				if (m_sessionView == null) {
					return false;
				}
				switch (item.getItemId()) {
				case R.id.delete_session:

					// Delete session was selected. Remove the session and end
					// the CAB
					removeSession(m_sessionView.getSession());
					m_sessionView = null;
					mode.finish();
					return true;
				case R.id.edit_session:

					// Build an alert dialog asking for the new session name
					AlertDialog.Builder builder = new AlertDialog.Builder(
							context);
					LayoutInflater inflater = getLayoutInflater();
					View dialogView = inflater.inflate(
							R.layout.session_edit_prompt, null);
					m_vwSessionName = (EditText) dialogView
							.findViewById(R.id.edit_session_name);

					// Set the positive, negative and neutral button actions.
					// The positive and neutral buttons will update the
					// session's name
					builder.setView(dialogView)
							.setPositiveButton(R.string.button_ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											m_strSessionName = m_vwSessionName
													.getText().toString();
											if (m_strSessionName != null
													&& !m_strSessionName
															.equals("")) {
												m_sessionView
														.getSession()
														.setName(
																m_strSessionName);
												m_sessionView
														.notifyOnSessionChangeListener();
												m_vwSessionName.setText("");
												dialog.dismiss();
											} else {
												Toast.makeText(
														context,
														"Please enter a name for your session or tap Skip",
														Toast.LENGTH_SHORT)
														.show();
												return;
											}
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											m_vwSessionName.setText("");
											dialog.dismiss();
										}
									})
							.setNeutralButton("Skip",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											m_sessionView.getSession().setName(
													"");
											m_sessionView
													.notifyOnSessionChangeListener();
											m_vwSessionName.setText("");
											dialog.dismiss();
										}
									}).create().show();
					mode.finish();
					return true;
				default:
					return false;
				}
			}
		};

		// Set the background appropriately
		LinearLayout layout = (LinearLayout) m_sessionListView.getParent();
		Resources res = getResources();
		Drawable d = res.getDrawable(R.drawable.background_icon);
		d.setAlpha(45);
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			layout.setBackgroundDrawable(d);
		} else {
			layout.setBackground(d);
		}

	}

	/**
	 * Initializes the add session button listeners to pop up a prompt asking
	 * for the session name
	 */
	private void initAddSessionListener() {
		this.m_addSession.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Create a new alert dialog asking for the session's name. The
				// layout used is inflated into it
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				LayoutInflater inflater = getLayoutInflater();
				View dialogView = inflater.inflate(R.layout.session_add_prompt,
						null);
				m_vwSessionName = (EditText) dialogView
						.findViewById(R.id.session_name);

				// Set the positive, negative and neutral button click actions.
				// The positive and neutral buttons will add a new session to
				// the DB and then take the user to the new session's details
				// activity
				builder.setView(dialogView)
						.setPositiveButton(R.string.button_ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										m_strSessionName = m_vwSessionName
												.getText().toString();
										if (m_strSessionName != null
												&& !m_strSessionName.equals("")) {
											long id = addSession(new Session(
													m_strSessionName));
											m_vwSessionName.setText("");
											dialog.dismiss();
											Intent intent = new Intent(
													context,
													SessionDetailsActivity.class);
											intent.putExtra(INTENT_SESSION_ID,
													id);
											startActivity(intent);
										} else {
											Toast.makeText(
													context,
													"Please enter a name for your session or tap Skip",
													Toast.LENGTH_SHORT).show();
											return;
										}
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										m_vwSessionName.setText("");
										dialog.dismiss();
									}
								})
						.setNeutralButton("Skip",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										m_vwSessionName.setText("");
										long id = addSession(new Session());
										dialog.dismiss();
										Intent intent = new Intent(context,
												SessionDetailsActivity.class);
										intent.putExtra(INTENT_SESSION_ID, id);
										startActivity(intent);
									}
								}).create().show();

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Initialize the action bar with the menu
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.session_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuhelp:
			// If help is selected, make toast providing a hint
			Toast.makeText(
					this,
					"Please select an existing workout session to review and add entries."
							+ " You can also create a new workout session by selecting "
							+ "\"Tap to Add a New Workout Session\". You can also edit or "
							+ "delete a session by tapping and holding the desired session",
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.menusurvey:
			// If the survey button is clicked, send the user to the browser
			// loading the google doc survey
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
	 * Method that adds a Session to the DB and refreshes the data
	 * 
	 * @param session
	 *            the session to be added
	 */
	private long addSession(Session session) {

		// Initialize a URI and ContentValues for column values to add the
		// session
		Uri uri = Uri.parse(CHANGE_SESSION_CONTENT_STRING + "0");
		ContentValues values = new ContentValues();
		values.put(TimeToTrainTables.SESSIONS_KEY_NAME,
				session.getSessionName());
		values.put(TimeToTrainTables.SESSIONS_KEY_DATE,
				Session.SESSION_DATE_FORMAT.format(session.getSessionDate()));

		// Set the session ID to the ID returned from inserting the new session
		session.setId((Long.parseLong(getContentResolver().insert(uri, values)
				.getLastPathSegment())));

		// Refresh the data and return the new ID
		fillData();
		return session.getID();
	}

	/**
	 * Deletes a session from the DB and refreshes the list of sessions
	 * 
	 * @param session
	 */
	protected void removeSession(Session session) {
		Uri uri = Uri.parse(CHANGE_SESSION_CONTENT_STRING + session.getID());
		getContentResolver().delete(uri, null, null);
		fillData();
	}

	/**
	 * Refreshes the session list's data
	 */
	public void fillData() {
		getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
		m_sessionListView.setAdapter(m_sessionAdapter);
	}

	@Override
	public void onSessionChanged(SessionView view, Session session) {

		// When a session changes, update the session information and sync it
		// with the DB
		Uri uri = Uri.parse(CHANGE_SESSION_CONTENT_STRING + session.getID());
		ContentValues values = new ContentValues();
		values.put(TimeToTrainTables.SESSIONS_KEY_DATE,
				Session.SESSION_DATE_FORMAT.format(session.getSessionDate()));
		values.put(TimeToTrainTables.SESSIONS_KEY_NAME,
				session.getSessionName());
		getContentResolver().update(uri, values, null, null);

		// Remove the listener briefly and refresh the data
		this.m_sessionAdapter.setOnSessionChangeListener(null);
		fillData();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		// Initialize the projection and the loader using the projection and URI
		String[] projection = { TimeToTrainTables.SESSIONS_KEY_ID,
				TimeToTrainTables.SESSIONS_KEY_NAME,
				TimeToTrainTables.SESSIONS_KEY_DATE };
		Uri uri = Uri.parse(SESSION_CONTENT_STRING + "1");
		CursorLoader loader = new CursorLoader(this, uri, projection, null,
				null, null);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		
		// Swap the cursor and set the change listener
		this.m_sessionAdapter.swapCursor(arg1);
		this.m_sessionAdapter.setOnSessionChangeListener(this);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		this.m_sessionAdapter.swapCursor(null);

	}
}
