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

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
      super.onCreate(savedInstanceState);

      this.m_sessionAdapter = new SessionCursorAdapter(this, null, 0);
      getSupportLoaderManager().initLoader(LOADER_ID, null, this);
      this.m_sessionAdapter.setOnSessionChangeListener(this);

      initLayout();
   }

   protected void initLayout() {
      setContentView(R.layout.session_list);
      this.m_sessionListView = (ListView) findViewById(R.id.sessions_list);
      this.m_sessionListView.setAdapter(m_sessionAdapter);
      this.m_addSession = (Button) findViewById(R.id.view_add_session);
      initAddSessionListener();
      this.m_sessionListView
            .setOnItemLongClickListener(new OnItemLongClickListener() {

               @Override
               public boolean onItemLongClick(AdapterView<?> parent, View view,
                     int position, long id) {
                  if (mActionMode != null) {
                     return false;
                  }
                  mActionMode = getSherlock().startActionMode(
                        mActionModeCallback);
                  m_sessionPosition = position;
                  return true;
               }

            });
      this.m_sessionListView.setOnItemClickListener(new OnItemClickListener() {

         @Override
         public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
            Intent intent = new Intent(context, SessionDetailsActivity.class);
            SessionView sView = (SessionView) view;
            intent.putExtra(INTENT_SESSION_ID, sView.getSession().getID());
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
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.session_action_menu, menu);
            return true;
         }

         @Override
         public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            m_sessionView = ((SessionView) m_sessionListView
                  .getChildAt(m_sessionPosition));
            if (m_sessionView == null) {
               Log.e(SessionListActivity.class.getName(),
                     "m_sessionView should always be set in this situation");
               return false;
            }
            switch (item.getItemId()) {
            case R.id.delete_session:
               Log.i(SessionListActivity.class.getName(),
                     "Removing session with ID "
                           + m_sessionView.getSession().getID()
                           + " from the DB...");
               removeSession(m_sessionView.getSession());
               m_sessionView = null;
               mode.finish();
               return true;
            case R.id.edit_session:
               AlertDialog.Builder builder = new AlertDialog.Builder(context);
               LayoutInflater inflater = getLayoutInflater();
               View dialogView = inflater.inflate(R.layout.session_edit_prompt,
                     null);
               m_vwSessionName = (EditText) dialogView
                     .findViewById(R.id.edit_session_name);
               builder
                     .setView(dialogView)
                     .setPositiveButton(R.string.button_ok,
                           new DialogInterface.OnClickListener() {

                              @Override
                              public void onClick(DialogInterface dialog,
                                    int which) {
                                 m_strSessionName = m_vwSessionName.getText()
                                       .toString();
                                 if (m_strSessionName != null
                                       && !m_strSessionName.equals("")) {
                                    Log.i(SessionListActivity.class.getName(),
                                          "Editing session with ID "
                                                + m_sessionView.getSession()
                                                      .getID() + " in DB");
                                    m_sessionView.getSession().setName(
                                          m_strSessionName);
                                    m_sessionView
                                          .notifyOnSessionChangeListener();
                                    m_vwSessionName.setText("");
                                    dialog.dismiss();
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
                                 Log.i(SessionListActivity.class.getName(),
                                       "Editing session with ID "
                                             + m_sessionView.getSession()
                                                   .getID() + " in DB");
                                 m_sessionView.getSession().setName("");
                                 m_sessionView.notifyOnSessionChangeListener();
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

   }

   /**
    * Initializes the add session button listeners to pop up a prompt asking for
    * the session name
    */
   private void initAddSessionListener() {
      this.m_addSession.setOnClickListener(new OnClickListener() {

         @Override
         public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.session_add_prompt,
                  null);
            m_vwSessionName = (EditText) dialogView
                  .findViewById(R.id.session_name);
            builder
                  .setView(dialogView)
                  .setPositiveButton(R.string.button_ok,
                        new DialogInterface.OnClickListener() {

                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                              m_strSessionName = m_vwSessionName.getText()
                                    .toString();
                              if (m_strSessionName != null
                                    && !m_strSessionName.equals("")) {
                                 Log.i(SessionListActivity.class.getName(),
                                       "Adding " + m_strSessionName
                                             + " session to DB");
                                 long id = addSession(new Session(
                                       m_strSessionName));
                                 m_vwSessionName.setText("");
                                 dialog.dismiss();
                                 Intent intent = new Intent(context,
                                       SessionDetailsActivity.class);
                                 intent.putExtra(INTENT_SESSION_ID, id);
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
                           public void onClick(DialogInterface dialog, int which) {
                              m_vwSessionName.setText("");
                              dialog.dismiss();
                           }
                        })
                  .setNeutralButton("Skip",
                        new DialogInterface.OnClickListener() {

                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                              Log.i(SessionListActivity.class.getName(),
                                    "Adding nameless session to DB");
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
      MenuInflater inflater = this.getSupportMenuInflater();
      inflater.inflate(R.menu.session_menu, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.menuhelp:
         Toast.makeText(
               this,
               "Please select an existing workout session to review and add entries."
                     + " You can also create a new workout session by selecting "
                     + "\"Tap to Add a New Workout Session\". You can also edit or " +
                     "delete a session by tapping and holding the desired session",
               Toast.LENGTH_LONG).show();
         return true;
      default:
         return super.onOptionsItemSelected(item);
      }
   }

   /**
    * Method that adds a Session to the DB and refreshes the data
    * 
    * @param session
    *           the session to be added
    */
   private long addSession(Session session) {
      Uri uri = Uri.parse(CHANGE_SESSION_CONTENT_STRING + "0");
      ContentValues values = new ContentValues();
      values.put(TimeToTrainTables.SESSIONS_KEY_NAME, session.getSessionName());
      values.put(TimeToTrainTables.SESSIONS_KEY_DATE,
            Session.SESSION_DATE_FORMAT.format(session.getSessionDate()));
      session.setId((Long.valueOf(getContentResolver().insert(uri, values)
            .getLastPathSegment())));
      fillData();
      return session.getID();
   }

   protected void removeSession(Session session) {
      Uri uri = Uri.parse(CHANGE_SESSION_CONTENT_STRING + session.getID());
      getContentResolver().delete(uri, null, null);
      fillData();
   }

   public void fillData() {
      getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
      m_sessionListView.setAdapter(m_sessionAdapter);
   }

   @Override
   public void onSessionChanged(SessionView view, Session session) {
      Uri uri = Uri.parse(CHANGE_SESSION_CONTENT_STRING + session.getID());
      ContentValues values = new ContentValues();
      values.put(TimeToTrainTables.SESSIONS_KEY_DATE,
            Session.SESSION_DATE_FORMAT.format(session.getSessionDate()));
      values.put(TimeToTrainTables.SESSIONS_KEY_NAME, session.getSessionName());
      getContentResolver().update(uri, values, null, null);
      this.m_sessionAdapter.setOnSessionChangeListener(null);
      fillData();
   }

   @Override
   public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
      String[] projection = { TimeToTrainTables.SESSIONS_KEY_ID,
            TimeToTrainTables.SESSIONS_KEY_NAME,
            TimeToTrainTables.SESSIONS_KEY_DATE };
      Uri uri = Uri.parse(SESSION_CONTENT_STRING + "1");
      CursorLoader loader = new CursorLoader(this, uri, projection, null, null,
            null);
      return loader;
   }

   @Override
   public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
      this.m_sessionAdapter.swapCursor(arg1);
      this.m_sessionAdapter.setOnSessionChangeListener(this);

   }

   @Override
   public void onLoaderReset(Loader<Cursor> arg0) {
      this.m_sessionAdapter.swapCursor(null);

   }
}
