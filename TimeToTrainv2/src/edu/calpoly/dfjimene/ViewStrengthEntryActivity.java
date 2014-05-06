package edu.calpoly.dfjimene;

import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.calpoly.dfjimene.data.TimeToTrainContentProvider;
import edu.calpoly.dfjimene.data.TimeToTrainTables;
import edu.calpoly.dfjimene.exerciseentry.ExerciseEntry;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;

public class ViewStrengthEntryActivity extends SherlockFragmentActivity {

   /** Indicator for current mode */
   private int mode;

   /** Session ID for this entry */
   private long m_sessionId;

   /** Entry this activity pertains to */
   private ExerciseEntry m_entry;

   /** Entry ID for this entry */
   private long m_entryId;

   /** Entry Uri string */
   private static final String ENTRY_CONTENT_STRING = TimeToTrainContentProvider.CONTENT_STRING_EXERCISE_ENTRIES;
   
   /** Projection for strength entry attributes */
   public static final String PROJECTION[] = {
      TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME,
      TimeToTrainTables.EXERCISE_ENTRIES_KEY_SETS,
      TimeToTrainTables.EXERCISE_ENTRIES_KEY_COMMENT };

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Bundle extras = getIntent().getExtras();
      m_sessionId = extras.getLong("session");
      m_entryId = extras.getLong("entry");
      buildStrengthExerciseEntryFromId();
      initStrengthView();
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
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

   public void buildStrengthExerciseEntryFromId() {
      Uri uri = Uri.parse(ENTRY_CONTENT_STRING + "/entry/" + m_entryId);
      Cursor cursor = getContentResolver().query(uri, PROJECTION, null, null,
            null);
      if (cursor.getCount() != 1) {
         Log.e(ViewStrengthEntryActivity.class.getName(),
               "Rows returned should be 1! The ids are unique!");
         Intent i = new Intent(this, SessionDetailsActivity.class);
         i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
         NavUtils.navigateUpTo(this, i);
         finish();
         cursor.close();
         return;
      }
      cursor.moveToFirst();
      m_entry = new ExerciseEntry(m_entryId, m_sessionId, cursor.getString(0),
            ExerciseEntry.TYPE_STRENGTH);
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
   
   public void initStrengthView(){
      setTitle(m_entry.getExreciseName());
      setContentView(R.layout.view_entry_strength);
   }
}
