package edu.calpoly.dfjimene;

import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.calpoly.dfjimene.data.TimeToTrainContentProvider;
import edu.calpoly.dfjimene.data.TimeToTrainTables;
import edu.calpoly.dfjimene.exerciseentry.ExerciseEntry;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class AddEntryActivity extends SherlockFragmentActivity implements
      OnCheckedChangeListener {

   /** Constants for altering the view */
   private static final int STRENGTH = 0;
   private static final int CARDIO = 1;
   private static final int UNCHECKED = -1;

   /** Session ID of session the entry will be added to */
   private long m_sessionId;

   /** Radio group for the add entry activity */
   private RadioGroup m_radioGroup;

   /** Edit text for the exercise name */
   private EditText m_exerciseName;

   /** Edit texts for time and distance */
   private EditText m_distance;
   private EditText m_timeSeconds;
   private EditText m_timeMinutes;

   /** Layout for extra cardio portion */
   private LinearLayout m_cardioLayout;

   /** Strength tip view */
   private TextView m_strTip;

   /** Currently selected type */
   private int m_selected = STRENGTH;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Bundle extras = getIntent().getExtras();
      m_sessionId = extras.getLong(SessionDetailsActivity.SESSION_ID);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      initLayout();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = this.getSupportMenuInflater();
      inflater.inflate(R.menu.add_entry_menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.menu_next:
         addNewEntry();
         return true;
      case android.R.id.home:
         Intent i = new Intent(this, SessionDetailsActivity.class);
         i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
         NavUtils.navigateUpTo(this, i);
         finish();
         return true;

      default:
         return super.onOptionsItemSelected(item);
      }
   }

   private void initLayout() {
      setContentView(R.layout.add_entry_view);
      setTitle("Add New Entry");
      LayoutInflater inflater = getLayoutInflater();
      m_cardioLayout = (LinearLayout) inflater.inflate(
            R.layout.add_entry_cardio_append, null);
      m_distance = (EditText) m_cardioLayout.findViewById(R.id.edit_distance);
      m_timeMinutes = (EditText) m_cardioLayout.findViewById(R.id.edit_minutes);
      m_timeSeconds = (EditText) m_cardioLayout.findViewById(R.id.edit_seconds);
      m_radioGroup = (RadioGroup) findViewById(R.id.group_exercise_type);
      m_radioGroup.check(R.id.radio_strength);
      m_exerciseName = (EditText) findViewById(R.id.edit_entry_name);
      m_strTip = (TextView) findViewById(R.id.strength_tip);
      m_radioGroup.setOnCheckedChangeListener(this);

   }

   @Override
   public void onCheckedChanged(RadioGroup group, int checkedId) {
      switch (checkedId) {
      case R.id.radio_strength:
         alterLayout(STRENGTH);
         m_selected = STRENGTH;
         break;
      case R.id.radio_cardio:
         alterLayout(CARDIO);
         m_selected = CARDIO;
         break;
      case -1:
         alterLayout(UNCHECKED);
         m_selected = UNCHECKED;
         break;
      }

   }

   private void addNewEntry() {
      ContentValues values = new ContentValues();
      if (m_exerciseName.getText().toString().equals("")) {
         Toast.makeText(this, "Please enter an exercise name",
               Toast.LENGTH_SHORT).show();
         return;
      }
      if (m_selected == CARDIO) {
         Integer min = Integer.parseInt(m_timeMinutes.getText().toString());
         Integer sec = Integer.parseInt(m_timeSeconds.getText().toString());
         Double dist;
         try {
            dist = Double.parseDouble(m_distance.getText().toString());
         } catch (NumberFormatException e) {
            dist = null;
         }
         int time;
         if (min == null && sec == null && dist == null)
            return;
         else {
            values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_DISTANCE, dist);
            time = (min == null ? 0 : min.intValue()) * 60
                  + (sec == null ? 0 : sec.intValue());
            values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_TIME,
                  (time == 0 ? null : time));
            values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_TYPE, ExerciseEntry.TYPE_CARDIO);
            values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
                  m_exerciseName.getText().toString());
            values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID,
                  m_sessionId);
            Uri uri = Uri
                  .parse(TimeToTrainContentProvider.CONTENT_STRING_EXERCISE_ENTRIES
                        + "/entry/0");
            uri = getContentResolver().insert(uri, values);
            if (uri == null)
               Log.e(AddEntryActivity.class.getName(), "Null? Why?");
            String str = uri.getLastPathSegment();
            if (str == null) {
               Log.e(AddEntryActivity.class.getName(),
                     "Null string makes no sense");
               Intent i = new Intent(this, SessionDetailsActivity.class);
               i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
               NavUtils.navigateUpTo(this, i);
               finish();
               return;
            }
            long entryId;
            try {
               entryId = Long.parseLong(str);
            } catch (NumberFormatException e) {
               Log.e(AddEntryActivity.class.getName(),
                     "Formatted incorrectly somehow...");
               Intent i = new Intent(this, SessionDetailsActivity.class);
               i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
               NavUtils.navigateUpTo(this, i);
               finish();
               return;
            }
            Intent i = new Intent(this, ViewCardioEntryActivity.class);
            i.putExtra("session", m_sessionId);
            i.putExtra("entry", entryId);
            startActivity(i);
            m_distance.setText("");
            m_timeMinutes.setText("");
            m_timeSeconds.setText("");
            m_exerciseName.setText("");
            finish();
         }
      } else {
         values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SETS,
               new JSONObject().toString());
         values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_TYPE, ExerciseEntry.TYPE_STRENGTH);
         values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
               m_exerciseName.getText().toString());
         values.put(TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID,
               m_sessionId);

         Uri uri = Uri
               .parse(TimeToTrainContentProvider.CONTENT_STRING_EXERCISE_ENTRIES
                     + "/entry/0");
         uri = getContentResolver().insert(uri, values);
         if (uri == null)
            Log.e(AddEntryActivity.class.getName(), "Null? Why?");
         String str = uri.getLastPathSegment();
         if (str == null) {
            Log.e(AddEntryActivity.class.getName(),
                  "Null string makes no sense");
            Intent i = new Intent(this, SessionDetailsActivity.class);
            i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
            NavUtils.navigateUpTo(this, i);
            finish();
            return;
         }
         long entryId;
         try {
            entryId = Long.parseLong(str);
         } catch (NumberFormatException e) {
            Log.e(AddEntryActivity.class.getName(),
                  "Formatted incorrectly somehow...");
            Intent i = new Intent(this, SessionDetailsActivity.class);
            i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
            NavUtils.navigateUpTo(this, i);
            finish();
            return;
         }
         Intent i = new Intent(this, ViewStrengthEntryActivity.class);
         i.putExtra("session", m_sessionId);
         i.putExtra("entry", entryId);
         startActivity(i);
         m_distance.setText("");
         m_timeMinutes.setText("");
         m_timeSeconds.setText("");
         m_exerciseName.setText("");
         finish();
      }
   }

   private void alterLayout(int type) {
      LinearLayout layout;
      if (m_selected == CARDIO && (type == STRENGTH || type == UNCHECKED)) {
         layout = ((LinearLayout) m_cardioLayout.getParent());
         layout.removeView(m_cardioLayout);
         layout.addView(m_strTip);
      } else if (m_selected == STRENGTH && type == CARDIO) {

         layout = ((LinearLayout) this.findViewById(R.id.add_entry_linear));
         layout.removeView(m_strTip);
         layout.addView(m_cardioLayout);
      }
   }

}
