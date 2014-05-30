package edu.calpoly.dfjimene;

import java.util.Random;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.calpoly.dfjimene.data.TimeToTrainContentProvider;
import edu.calpoly.dfjimene.data.TimeToTrainTables;
import edu.calpoly.dfjimene.exerciseentry.ExerciseEntry;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
// import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Acitivty for viewing cardio entries and adding notes. In retrospect I
 * should've made a super class for view entry activities to extend.
 * 
 * @author Douglas Jimenez
 * 
 */
public class ViewCardioEntryActivity extends SherlockFragmentActivity {

	/** Session ID for this entry */
	private long m_sessionId;

	/** Entry this activity pertains to */
	private ExerciseEntry m_entry;

	/** Entry ID for this entry */
	private long m_entryId;

	/** Entry Uri string */
	private static final String ENTRY_CONTENT_STRING = TimeToTrainContentProvider.CONTENT_STRING_EXERCISE_ENTRIES;

	/** TextView holder for when there is no note to display */
	private TextView m_noteView;

	/** Layout for entire thing */
	private LinearLayout m_mainLayout;

	/** Note layout container */
	private LinearLayout m_noteLayout = null;
	private LinearLayout m_editNoteLayout = null;

	/** Edit Text for editing notes */
	private EditText m_editor = null;

	/** Add Note Buttons */
	private Button m_addNoteButton;
	private Button m_submitButton;

	/** On click listeners for submitting a note */
	private OnClickListener m_addNoteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			buildNoteEditor();

		}
	};
	private OnClickListener m_submitNewNoteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			submitEditedNote();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(m_editor.getWindowToken(), 0);
		}
	};
	private OnClickListener m_editNoteListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			editNote();
		}
	};

	/** Cardio Entry projection */
	public static final String PROJECTION[] = {
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_TIME,
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_DISTANCE,
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_COMMENT };

	@Override
	public void onCreate(Bundle savedInstanceState) {

		// Always call super's onCreate
		super.onCreate(savedInstanceState);

		// Grab extras and set entry ID and session ID and set members
		// appropriately
		Bundle extras = getIntent().getExtras();
		m_sessionId = extras.getLong("session");
		m_entryId = extras.getLong("entry");

		// Build the cardio entry member variables and initialize the layout
		buildCardioExerciseEntryFromId();
		initCardioView();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			// Home was selected. Return to details activity
			Intent i = new Intent(this, SessionDetailsActivity.class);
			i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
			NavUtils.navigateUpTo(this, i);
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Queries the DB for the full set of cardio information from the ID
	 * gathered in the onCreate method and sets the members appropriately
	 */
	public void buildCardioExerciseEntryFromId() {

		// Build the URI and then run the query
		Uri uri = Uri.parse(ENTRY_CONTENT_STRING + "/entry/" + m_entryId);
		Cursor cursor = getContentResolver().query(uri, PROJECTION, null, null,
				null);
		if (cursor.getCount() != 1) {
			// There should be extacly one entry. Something went wrong. Return
			// to details activity
			Intent i = new Intent(this, SessionDetailsActivity.class);
			i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
			NavUtils.navigateUpTo(this, i);
			finish();
			cursor.close();
			return;
		}
		cursor.moveToFirst();

		// Set member fields from results
		m_entry = new ExerciseEntry(m_entryId, m_sessionId,
				cursor.getString(0), ExerciseEntry.TYPE_CARDIO);
		if (!cursor.isNull(3))
			m_entry.setComments(cursor.getString(3));
		else
			m_entry.setComments(null);
		if (!cursor.isNull(2))
			m_entry.setDistance(cursor.getDouble(2));
		else
			m_entry.setDistance(-1);
		if (!cursor.isNull(1))
			m_entry.setTime(cursor.getInt(1));
		else
			m_entry.setTime(-1);
		cursor.close();
	}

	/**
	 * Initializes the view. Warnings and Lint are checked and handled in the
	 * code
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public void initCardioView() {

		// Get a random background image (from 2 options)
		Resources res = getResources();
		Random rand = new Random();
		Drawable img;
		if (rand.nextInt() % 2 == 0) {
			img = res.getDrawable(R.drawable.cardio_1);
		} else {
			img = res.getDrawable(R.drawable.cardio_2);
		}
		img.setAlpha(45);

		// Set the title and initialize the view
		setTitle(m_entry.getExreciseName());
		setContentView(R.layout.view_entry_cardio);

		// Grab the time and distance views. Then remove the views with no data
		// to accept from the main layout. Fill the views that do have data to
		// accept
		TextView time = (TextView) findViewById(R.id.cardio_time_view);
		TextView dist = (TextView) findViewById(R.id.cardio_dist_view);
		LinearLayout layout = (LinearLayout) time.getParent();
		if (m_entry.getTime() != -1) {
			int min = m_entry.getTime() / 60;
			int sec = m_entry.getTime() % 60;
			time.setText(min + ":" + (sec >= 10 ? sec : ("0" + sec)));
		} else {
			layout.removeView(time);
			layout.removeView(findViewById(R.id.your_time));
		}
		if (m_entry.getDistance() != -1) {
			dist.setText(m_entry.getDistance() + "mi");
		} else {
			layout.removeView(dist);
			layout.removeView(findViewById(R.id.your_distance));
		}

		// If comments exist, bind the comments to the comments TextView and
		// show the appropriate button to edit it. Otherwise, Keep the add note
		// button and leave the TextView blank
		if (m_entry.getComments() != null) {
			m_noteView = (TextView) findViewById(R.id.cardio_note_view);
			m_noteLayout = (LinearLayout) findViewById(R.id.add_cardio_notes);
			m_noteView.setText(m_entry.getComments());
			m_addNoteButton = (Button) findViewById(R.id.add_cardio_note);
			m_addNoteButton.setText("Edit Note");
			m_addNoteButton.setOnClickListener(m_editNoteListener);
		} else {
			m_noteView = (TextView) findViewById(R.id.cardio_note_view);
			m_noteLayout = (LinearLayout) m_noteView.getParent();
			m_noteLayout.removeView(m_noteView);
			m_addNoteButton = (Button) findViewById(R.id.add_cardio_note);
			m_addNoteButton.setOnClickListener(m_addNoteListener);
		}

		// Actually set the background
		m_mainLayout = (LinearLayout) m_noteLayout.getParent();
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			m_mainLayout.setBackgroundDrawable(img);
		} else {
			m_mainLayout.setBackground(img);
		}
	}

	/**
	 * Builds the note editor when someone wants to add or edit a note
	 */
	public void buildNoteEditor() {

		// Initialize the note editor if it hasn't been used yet
		if (m_editNoteLayout == null) {

			// Inflate the edit note layout and bind the inflated views to their
			// appropriate members
			LayoutInflater inflater = getLayoutInflater();
			m_editNoteLayout = (LinearLayout) inflater.inflate(
					R.layout.add_note, null);
			m_editor = (EditText) m_editNoteLayout.findViewById(R.id.edit_note);
			m_submitButton = (Button) m_editNoteLayout
					.findViewById(R.id.submit_button);

			// Set the listener for if a user hits enter to close the editor and
			// add the note
			m_editor.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN
							&& (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
						submitEditedNote();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(m_editor.getWindowToken(),
								0);
						return true;
					}
					if (event.getAction() == KeyEvent.ACTION_UP
							&& keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(m_editor.getWindowToken(),
								0);
						return true;
					}
					return false;
				}
			});

			// Set the click listener for the edit note's button
			m_submitButton.setOnClickListener(m_submitNewNoteListener);
		}

		// Add the editor to the layout and request focus
		m_noteLayout.addView(m_editNoteLayout);
		((LinearLayout) m_addNoteButton.getParent())
				.removeView(m_addNoteButton);
		m_editor.requestFocus();

	}

	/**
	 * Submits the edited note to the DB and clears the editor
	 */
	public void submitEditedNote() {

		// Remove the editor from the main layout and change the add a note
		// button to say edit note
		m_noteLayout.removeView(m_editNoteLayout);
		m_addNoteButton.setText("Edit Note");

		// Set the button's listener to the edit note listener
		m_addNoteButton.setOnClickListener(m_editNoteListener);

		// Update the comment column of the specified entry
		ContentValues values = new ContentValues();
		values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_COMMENT, m_editor
				.getText().toString());
		m_entry.setComments(m_editor.getText().toString());
		Uri uri = Uri.parse(ENTRY_CONTENT_STRING + "/entry/" + m_entryId);
		getContentResolver().update(uri, values, null, null);

		// Set the note view's text to the new note and re add it to the layout
		m_noteView.setText(m_entry.getComments());
		m_noteLayout.addView(m_noteView);
		m_addNoteButton.setText("Edit Note");
		m_addNoteButton.setOnClickListener(m_editNoteListener);
		m_mainLayout.addView(m_addNoteButton);

	}

	/**
	 * Enables the user to edit the note they have previously added
	 */
	public void editNote() {

		// If the editor has not been initialized, initialize it
		if (m_editNoteLayout == null) {
			LayoutInflater inflater = getLayoutInflater();
			m_editNoteLayout = (LinearLayout) inflater.inflate(
					R.layout.add_note, null);
			m_editor = (EditText) m_editNoteLayout.findViewById(R.id.edit_note);
			m_submitButton = (Button) m_editNoteLayout
					.findViewById(R.id.submit_button);
			m_editor.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN
							&& (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
						submitEditedNote();
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(m_editor.getWindowToken(),
								0);
						return true;
					}
					if (event.getAction() == KeyEvent.ACTION_UP
							&& keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(m_editor.getWindowToken(),
								0);
						return true;
					}
					return false;
				}
			});
			m_submitButton.setOnClickListener(m_submitNewNoteListener);
		}

		// Remove the unnecessary views and add the appropriate ones
		m_noteLayout.removeView(m_noteView);
		m_noteLayout.addView(m_editNoteLayout);
		((LinearLayout) m_addNoteButton.getParent())
				.removeView(m_addNoteButton);
		m_editor.setText(m_entry.getComments());
		m_editor.requestFocus();
	}
}
