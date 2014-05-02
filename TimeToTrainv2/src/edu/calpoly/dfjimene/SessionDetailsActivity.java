package edu.calpoly.dfjimene;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import edu.calpoly.dfjimene.exerciseentry.SimpleEntry;
import edu.calpoly.dfjimene.exerciseentry.SimpleEntryCursorAdapter;
import edu.calpoly.dfjimene.exerciseentry.SimpleEntryView;
import edu.calpoly.dfjimene.exerciseentry.SimpleEntryView.OnSimpleEntryChangeListener;

public class SessionDetailsActivity extends SherlockFragmentActivity implements
      LoaderCallbacks<Cursor>, OnSimpleEntryChangeListener {

   /** Session ID this activity concerns */
   private long m_sessionId;

   /** Adapter for Simple Entries */
   private SimpleEntryCursorAdapter m_simpleEntryAdapter;

   /** View holding simple entries */
   private ListView m_simpleEntryList;

   /** Extras key for entry id */
   public static final String ENTRY_ID = "entry_id";

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

   /** Content uri string for entries */
   private static final String ENTRY_CONTENT_STRING = TimeToTrainContentProvider.CONTENT_STRING_EXERCISE_ENTRIES
         + "/entry/";

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      m_sessionId = getIntent().getLongExtra(
            SessionListActivity.INTENT_SESSION_ID, -1);
      if (m_sessionId < 0)
         Log.e(this.getClass().getName(), "Negative session ID");
      changeTitleForSession(m_sessionId);
      this.m_simpleEntryAdapter = new SimpleEntryCursorAdapter(this, null, 0);
      getSupportLoaderManager().initLoader(LOADER_ID, null, this);
      m_simpleEntryAdapter.setOnSimpleEntryChangeListener(this);
      setContentView(R.layout.session_entry_list);
      m_simpleEntryList = (ListView) findViewById(R.id.entry_list);
      m_simpleEntryList.setAdapter(m_simpleEntryAdapter);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      initListeners();

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = this.getSupportMenuInflater();
      inflater.inflate(R.menu.details_menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.menuhelp:
         Toast.makeText(
               this,
               "Please select an entry to view and modify."
                     + "To delete an existing entry, tap and hold the entry" +
                     " you no longer want and tap \"Delete Entry\"",
               Toast.LENGTH_LONG).show();
         return true;
      case android.R.id.home:
         NavUtils.navigateUpTo(this,
               new Intent(this, SessionListActivity.class));
         return true;

      default:
         return super.onOptionsItemSelected(item);
      }
   }

   private void initListeners() {
      this.m_simpleEntryList
            .setOnItemLongClickListener(new OnItemLongClickListener() {

               @Override
               public boolean onItemLongClick(AdapterView<?> parent, View view,
                     int position, long id) {
                  if (m_actionMode != null) {
                     return false;
                  }
                  m_actionMode = getSherlock().startActionMode(
                        m_actionModeCallback);
                  m_entryPosition = position;
                  return true;
               }
            });
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
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.details_action_menu, menu);
            return true;
         }

         @Override
         public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            m_simpleEntryView = ((SimpleEntryView) m_simpleEntryList
                  .getChildAt(m_entryPosition));
            if (m_simpleEntryView == null) {
               Log.e(SessionDetailsActivity.class.getName(),
                     "m_simpleEntryView should always be set in this situation");
               return false;
            }
            switch (item.getItemId()) {
            case R.id.delete_entry:
               Log.i(SessionDetailsActivity.class.getName(),
                     "Deleting entry with ID "
                           + m_simpleEntryView.getSimpleEntry().getEntryID()
                           + "From Entries...");
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
   }

   private void changeTitleForSession(long sessionId) {
      String[] projection = { TimeToTrainTables.SESSIONS_KEY_ID,
            TimeToTrainTables.SESSIONS_KEY_NAME,
            TimeToTrainTables.SESSIONS_KEY_DATE };
      Cursor cursor = getContentResolver().query(
            Uri.parse(SESSION_CONTENT_STRING + sessionId), projection, null,
            null, null);
      if (cursor.getCount() != 1) {
         Log.e(this.getClass().getName(), "Bad query.");
      }
      Log.d(this.getClass().getName(), "" + sessionId);
      cursor.moveToFirst();
      String newTitle = cursor.getString(TimeToTrainTables.SESSIONS_COL_NAME);
      if (newTitle != null && !newTitle.equals(""))
         setTitle(newTitle);
      else
         setTitle(cursor.getString(TimeToTrainTables.SESSIONS_COL_DATE));
   }

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
      String projection[] = { TimeToTrainTables.EXERCISE_ENTRIES_KEY_ID,
            TimeToTrainTables.EXERCISE_ENTRIES_KEY_SESSION_ID,
            TimeToTrainTables.EXERCISE_ENTRIES_KEY_EXERCISE_NAME };
      Uri uri = Uri.parse(SIMPLE_ENTRY_CONTENT_STRING + m_sessionId);
      CursorLoader loader = new CursorLoader(this, uri, projection, null, null,
            null);
      return loader;
   }

   @Override
   public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
      this.m_simpleEntryAdapter.swapCursor(arg1);
      this.m_simpleEntryAdapter.setOnSimpleEntryChangeListener(this);

   }

   @Override
   public void onLoaderReset(Loader<Cursor> arg0) {
      this.m_simpleEntryAdapter.swapCursor(null);

   }

}
