package edu.calpoly.dfjimene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * View Entry Activity for strength based exercises allows user to add sets and
 * notes
 * 
 * @author Douglas Jimenez
 * 
 */
public class ViewStrengthEntryActivity extends SherlockFragmentActivity {

	/** Json keys */
	private static final String JSON_ARR_KEY = "sets";
	private static final String JSON_WEIGHT_KEY = "weight";
	private static final String JSON_WEIGHT_UNITS = "units";
	private static final String JSON_REPS = "reps";

	/** Checked indicators */
	private static final int KG_CHECKED = 0;
	private static final int LB_CHECKED = 1;

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

	/** Set layout container */
	private LinearLayout m_setLayout;
	private LinearLayout m_editSetLayout;

	/** ArrayList containing sets */
	private ArrayList<Map<String, String>> m_listSets = new ArrayList<Map<String, String>>();

	/** Note layout container */
	private LinearLayout m_noteLayout = null;
	private LinearLayout m_editNoteLayout = null;

	/** Edit Text for editing notes */
	private EditText m_editor = null;

	/** Add Note Buttons */
	private Button m_addNoteButton;
	private Button m_submitButton;

	/** Add Set Buttons */
	private Button m_addSetButton;
	private Button m_submitSet;
	private RadioGroup m_editSetGroup;

	/** Selected units */
	private int m_checked = KG_CHECKED;

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

	/** Listeners for adding a set */
	private OnClickListener m_addSetListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			buildSetEditor();

		}
	};
	private OnClickListener m_submitSetListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			submitNewSet();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			EditText view = (EditText) m_editSetLayout
					.findViewById(R.id.edit_reps);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	};

	/** Projection for strength entry attributes */
	public static final String PROJECTION[] = {
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_SETS,
			TimeToTrainTables.EXERCISE_ENTRIES_KEY_COMMENT };

	@Override
	public void onCreate(Bundle savedInstanceState) {

		// Always call super's onCreate
		super.onCreate(savedInstanceState);

		// Get extras and set the session and entry member IDs
		Bundle extras = getIntent().getExtras();
		m_sessionId = extras.getLong("session");
		m_entryId = extras.getLong("entry");

		// Initialize the layout after grabbing the entry from the DB
		buildStrengthExerciseEntryFromId();
		initStrengthView();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			// Home was just selected. Return to parent activity
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
	 * Grabs the strength based entry from the DB and initializes its members
	 * based on the returned columns
	 */
	public void buildStrengthExerciseEntryFromId() {

		// Create the URI striung and query for the ONLY entry
		Uri uri = Uri.parse(ENTRY_CONTENT_STRING + "/entry/" + m_entryId);
		Cursor cursor = getContentResolver().query(uri, PROJECTION, null, null,
				null);
		if (cursor.getCount() != 1) {

			// There should only be one unique entry. This is bad. Let's go back
			// to the parent
			Intent i = new Intent(this, SessionDetailsActivity.class);
			i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
			NavUtils.navigateUpTo(this, i);
			finish();
			cursor.close();
			return;
		}

		// Set the members appropriately
		cursor.moveToFirst();
		m_entry = new ExerciseEntry(m_entryId, m_sessionId,
				cursor.getString(0), ExerciseEntry.TYPE_STRENGTH);
		if (!cursor.isNull(2))
			m_entry.setComments(cursor.getString(2));
		else
			m_entry.setComments(null);
		if (!cursor.isNull(1))
			m_entry.setSets(cursor.getString(1));
		else
			m_entry.setSets(new JSONObject().toString());
		cursor.close();
	}

	/**
	 * Initializes the strength exercise view and its note and sets. Handles
	 * warnings and lint in line
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void initStrengthView() {

		// Grab a random image for the background (Between 2 options)
		Resources res = getResources();
		Random rand = new Random();
		Drawable img;
		if (rand.nextInt() % 2 == 0) {
			img = res.getDrawable(R.drawable.strength_1);
		} else {
			img = res.getDrawable(R.drawable.strength_2);
		}
		img.setAlpha(45);

		// Set the title and initializes the layout. Bind the sublayouts to
		// their appropriate member variables
		setTitle(m_entry.getExreciseName());
		setContentView(R.layout.view_entry_strength);
		m_noteLayout = (LinearLayout) findViewById(R.id.strength_notes_layout);
		m_setLayout = (LinearLayout) findViewById(R.id.add_strength_set_container);
		m_mainLayout = (LinearLayout) m_noteLayout.getParent();

		// Set the background
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			((ScrollView) m_mainLayout.getParent()).setBackgroundDrawable(img);
		} else {
			((ScrollView) m_mainLayout.getParent()).setBackground(img);
		}

		// Initialize the note layouts and bind them to their appropriate member
		// variables
		m_addSetButton = (Button) findViewById(R.id.add_strength_set);
		m_setLayout.removeAllViews();
		if (m_entry.getComments() != null) {
			m_noteView = (TextView) findViewById(R.id.strength_note_view);
			m_noteLayout = (LinearLayout) findViewById(R.id.strength_notes_layout);
			m_noteView.setText(m_entry.getComments());
			m_addNoteButton = (Button) findViewById(R.id.add_strength_note);
			m_addNoteButton.setText("Edit Note");
			m_addNoteButton.setOnClickListener(m_editNoteListener);
		} else {
			m_noteView = (TextView) findViewById(R.id.strength_note_view);
			m_noteLayout = (LinearLayout) findViewById(R.id.strength_notes_layout);
			m_noteLayout.removeView(m_noteView);
			m_addNoteButton = (Button) findViewById(R.id.add_strength_note);
			m_addNoteButton.setOnClickListener(m_addNoteListener);
		}

		// Pull all previously existing sets from the JSON string
		JSONObject json;
		JSONArray jsonArr;

		// Do all json work inside this try/catch statement
		try {
			json = new JSONObject(m_entry.getSets());

			if (json.has(JSON_ARR_KEY)) {
				jsonArr = json.getJSONArray(JSON_ARR_KEY);

				for (int i = 0; i < jsonArr.length(); i++) {
					JSONObject arrObj;
					arrObj = jsonArr.getJSONObject(i);
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(JSON_REPS, arrObj.getString(JSON_REPS));
					map.put(JSON_WEIGHT_KEY, arrObj.getString(JSON_WEIGHT_KEY));
					map.put(JSON_WEIGHT_UNITS,
							arrObj.getString(JSON_WEIGHT_UNITS));
					m_listSets.add(map);
				}
			}
		} catch (JSONException e) {
			// JSON exception. Whoops. Return to parent
			e.printStackTrace();
			Intent i = new Intent(this, SessionDetailsActivity.class);
			i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
			NavUtils.navigateUpTo(this, i);
			finish();
			return;
		}

		// Add sets to the set layout after initializing the view they will be
		// put in
		LayoutInflater inflater = getLayoutInflater();
		for (int i = 0; i < m_listSets.size(); i++) {
			Map<String, String> m = m_listSets.get(i);
			LinearLayout layout = (LinearLayout) inflater.inflate(
					R.layout.set_view, null);

			TextView view = (TextView) layout.findViewById(R.id.set_num_title);
			view.setText("Set " + (i + 1));

			view = (TextView) layout.findViewById(R.id.weight_title);
			view.setText(m.get(JSON_WEIGHT_KEY));

			view = (TextView) layout.findViewById(R.id.units_for_set);
			view.setText(m.get(JSON_WEIGHT_UNITS));

			view = (TextView) layout.findViewById(R.id.reps_for_set);
			view.setText(m.get(JSON_REPS));

			m_setLayout.addView(layout);
		}

		// Add the add set button to the set layout and set its on click
		// listener
		m_setLayout.addView(m_addSetButton);
		m_addSetButton.setOnClickListener(m_addSetListener);
	}

	/**
	 * Builds the note editor for the user to add notes.
	 */
	public void buildNoteEditor() {

		// If the note layout is not already initialized, initialize it
		if (m_editNoteLayout == null) {

			// Inflate the editor layout and bind its components to activity
			// members
			LayoutInflater inflater = getLayoutInflater();
			m_editNoteLayout = (LinearLayout) inflater.inflate(
					R.layout.add_note, null);
			m_editor = (EditText) m_editNoteLayout.findViewById(R.id.edit_note);
			m_submitButton = (Button) m_editNoteLayout
					.findViewById(R.id.submit_button);

			// Set the EditText's on key listener to submit when enter is
			// pressed
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

			// Initialize submit button's on click listener
			m_submitButton.setOnClickListener(m_submitNewNoteListener);
		}

		// Add the editor to the note layout and request focus
		m_noteLayout.addView(m_editNoteLayout);
		((LinearLayout) m_addNoteButton.getParent())
				.removeView(m_addNoteButton);
		m_editor.requestFocus();

	}

	/**
	 * Updates the entry in the DB and refreshes the views
	 */
	public void submitEditedNote() {

		// Update the comments column for the entry and set the TextView's text
		// to the new note
		m_noteLayout.removeView(m_editNoteLayout);
		m_addNoteButton.setText("Edit Note");
		m_addNoteButton.setOnClickListener(m_editNoteListener);
		ContentValues values = new ContentValues();
		values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_COMMENT, m_editor
				.getText().toString());
		m_entry.setComments(m_editor.getText().toString());
		Uri uri = Uri.parse(ENTRY_CONTENT_STRING + "/entry/" + m_entryId);
		getContentResolver().update(uri, values, null, null);
		m_noteView.setText(m_entry.getComments());

		// Remove the editor and add the TextView
		m_noteLayout.addView(m_noteView);
		m_addNoteButton.setText("Edit Note");
		m_addNoteButton.setOnClickListener(m_editNoteListener);
		m_mainLayout.addView(m_addNoteButton);

	}

	/**
	 * Creates the display allowing the user to edit their notes
	 */
	public void editNote() {

		// If the editor is not initialized, initialize it
		if (m_editNoteLayout == null) {

			// Inflate the editor and bind its components to the activity's
			// members
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

		// Remove unnecessary views and post relevant ones
		m_noteLayout.removeView(m_noteView);
		m_noteLayout.addView(m_editNoteLayout);
		((LinearLayout) m_addNoteButton.getParent())
				.removeView(m_addNoteButton);
		m_editor.setText(m_entry.getComments());
		m_editor.requestFocus();
	}

	/**
	 * Builds the set editor for adding new sets to a strength entry
	 */
	public void buildSetEditor() {

		// If the set editor is uninitialized, initialize it
		if (m_editSetLayout == null) {

			// Inflate the set editor and bind its views to the activity's
			// members
			LayoutInflater inflater = getLayoutInflater();
			m_editSetLayout = (LinearLayout) inflater.inflate(R.layout.set_add,
					null);
			m_submitSet = (Button) m_editSetLayout
					.findViewById(R.id.confirm_set);
			m_submitSet.setOnClickListener(m_submitSetListener);
			m_editSetGroup = (RadioGroup) m_editSetLayout
					.findViewById(R.id.units_group);

			// Set checked change listener for units and set a default
			m_editSetGroup
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(RadioGroup group,
								int checkedId) {
							switch (checkedId) {
							case R.id.kg_button:
								m_checked = KG_CHECKED;
								return;
							case R.id.lb_button:
								m_checked = LB_CHECKED;
								return;
							}
						}
					});
			m_editSetGroup.check(R.id.kg_button);
		}

		// Remove the add set button and add the editor
		m_setLayout.removeView(m_addSetButton);
		m_setLayout.addView(m_editSetLayout);

	}

	/**
	 * Adds the new set to the JSON string, updates the DB, and then refreshes
	 * the set layout
	 */
	public void submitNewSet() {

		// Check if the reps and weight have been populated. If not, remove the
		// editor, add the add set button again, and return
		EditText reps, weight;
		reps = (EditText) m_setLayout.findViewById(R.id.edit_reps);
		weight = (EditText) m_setLayout.findViewById(R.id.edit_weight);

		if (reps.getText().toString().equals("")
				|| weight.getText().toString().equals("")) {
			reps.setText("");
			weight.setText("");
			m_setLayout.removeView(m_editSetLayout);
			m_setLayout.addView(m_addSetButton);
			return;
		}

		// Create a hashmap for the set and add its values to the map based on
		// the appropriate key
		HashMap<String, String> map = new HashMap<String, String>();
		if (m_checked == KG_CHECKED)
			map.put(JSON_WEIGHT_UNITS, "kg");
		else
			map.put(JSON_WEIGHT_UNITS, "lb");
		map.put(JSON_WEIGHT_KEY, weight.getText().toString());
		map.put(JSON_REPS, reps.getText().toString());
		weight.setText("");
		reps.setText("");

		// Remove the editor and add the hashmap to the JSON string
		m_setLayout.removeView(m_editSetLayout);
		addSetToList(map);
		m_setLayout.addView(m_addSetButton);
	}

	/**
	 * Adds the set to the set list, then updates the DB
	 * 
	 * @param map
	 *            the hashmap to be added to the list
	 */
	public void addSetToList(Map<String, String> map) {

		// Inflate the set view layout and add the map to the list. Then bind
		// the map's values to the set view's fields
		LayoutInflater inflater = getLayoutInflater();
		m_listSets.add(map);
		LinearLayout setLayout = (LinearLayout) inflater.inflate(
				R.layout.set_view, null);
		TextView view = (TextView) setLayout.findViewById(R.id.set_num_title);
		view.setText("Set " + m_listSets.size());

		view = (TextView) setLayout.findViewById(R.id.weight_title);
		view.setText(map.get(JSON_WEIGHT_KEY));

		view = (TextView) setLayout.findViewById(R.id.units_for_set);
		view.setText(map.get(JSON_WEIGHT_UNITS));

		view = (TextView) setLayout.findViewById(R.id.reps_for_set);
		view.setText(map.get(JSON_REPS));

		// Add the view to the layout and sync
		m_setLayout.addView(setLayout);
		syncSetListWithDb();

	}

	/**
	 * Syncs the updated JSON string with the sets column of the entry
	 */
	public void syncSetListWithDb() {

		// Convert the entire array to a JSON array
		JSONObject json;
		// Do all json work inside this try/catch statement
		try {
			JSONArray jsonArr = new JSONArray();
			for (Map<String, String> m : m_listSets) {
				JSONObject arrObj = new JSONObject();

				arrObj.put(JSON_WEIGHT_KEY, m.get(JSON_WEIGHT_KEY));
				arrObj.put(JSON_REPS, m.get(JSON_REPS));
				arrObj.put(JSON_WEIGHT_UNITS, m.get(JSON_WEIGHT_UNITS));
				jsonArr.put(arrObj);
			}
			json = new JSONObject();
			json.put(JSON_ARR_KEY, jsonArr);

		} catch (JSONException e) {
			// Uh oh. Bad JSON? Return to parent activity
			e.printStackTrace();
			Intent i = new Intent(this, SessionDetailsActivity.class);
			i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
			NavUtils.navigateUpTo(this, i);
			finish();
			return;
		}

		// Set the sets string
		m_entry.setSets(json.toString());

		// Update the value in the DB
		ContentValues values = new ContentValues();
		values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SETS,
				m_entry.getSets());
		Uri uri = Uri.parse(ENTRY_CONTENT_STRING + "/entry/" + m_entryId);
		getContentResolver().update(uri, values, null, null);
	}
}
