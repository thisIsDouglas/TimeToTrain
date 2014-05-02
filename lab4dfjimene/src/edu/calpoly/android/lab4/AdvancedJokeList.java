package edu.calpoly.android.lab4;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import edu.calpoly.android.lab4.JokeView.OnJokeChangeListener;

public class AdvancedJokeList extends SherlockFragmentActivity implements
      LoaderCallbacks<Cursor>, OnJokeChangeListener {

   /** Contains the name of the Author for the jokes. */
   protected String m_strAuthorName;

   /** Adapter used to bind an AdapterView to List of Jokes. */
   protected JokeCursorAdapter m_jokeAdapter;

   /** ViewGroup used for maintaining a list of Views that each display Jokes. */
   protected ListView m_vwJokeLayout;

   /**
    * EditText used for entering text for a new Joke to be added to
    * m_arrJokeList.
    */
   protected EditText m_vwJokeEditText;

   /**
    * Button used for creating and adding a new Joke to m_arrJokeList using the
    * text entered in m_vwJokeEditText.
    */
   protected Button m_vwJokeButton;

   /** Menu used for filtering Jokes. */
   protected Menu m_vwMenu;

   /** Value used to filter which jokes get displayed to the user. */
   protected int m_nFilter;

   /**
    * Key used for storing and retrieving the value of m_nFilter in
    * savedInstanceState.
    */
   protected static final String SAVED_FILTER_VALUE = "m_nFilter";

   /**
    * Key used for storing and retrieving the text in m_vwJokeEditText in
    * savedInstanceState.
    */
   protected static final String SAVED_EDIT_TEXT = "m_vwJokeEditText";

   /** Menu/Submenu MenuItem IDs. */
   protected static final int FILTER = Menu.FIRST;
   protected static final int FILTER_LIKE = SubMenu.FIRST;
   protected static final int FILTER_DISLIKE = SubMenu.FIRST + 1;
   protected static final int FILTER_UNRATED = SubMenu.FIRST + 2;
   protected static final int FILTER_SHOW_ALL = SubMenu.FIRST + 3;

   /**
    * Used to handle Contextual Action Mode when long-clicking on a single Joke.
    */
   private Callback mActionModeCallback;
   private ActionMode mActionMode;

   /** The Joke that is currently focused after long-clicking. */
   private int selected_position;

   /**
    * The ID of the CursorLoader to be initialized in the LoaderManager and used
    * to load a Cursor.
    */
   private static final int LOADER_ID = 1;

   /**
    * The String representation of the Show All filter. The Show All case needs
    * a String representation of a value that is different from Joke.LIKE,
    * Joke.DISLIKE and Joke.UNRATED. The actual value doesn't matter as long as
    * it's different, since the WHERE clause is set to null when making database
    * operations under this setting.
    */
   public static final String SHOW_ALL_FILTER_STRING = "" + FILTER_SHOW_ALL;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);

      this.m_jokeAdapter = new JokeCursorAdapter(this, null, 0);
      getSupportLoaderManager().initLoader(LOADER_ID, null, this);
      this.m_jokeAdapter.setOnJokeChangeListener(this);
      this.m_strAuthorName = this.getResources()
            .getString(R.string.author_name);
      this.m_nFilter = FILTER_SHOW_ALL;
      initLayout();
      initAddJokeListeners();

      m_vwJokeEditText.setText(prefs.getString(SAVED_EDIT_TEXT, ""));

   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      outState.putInt(SAVED_FILTER_VALUE, m_nFilter);
      super.onSaveInstanceState(outState);
   }

   @Override
   public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      if (savedInstanceState != null
            && savedInstanceState.containsKey(SAVED_FILTER_VALUE)) {
         m_nFilter = savedInstanceState.getInt(SAVED_FILTER_VALUE);
         filterJokeList(m_nFilter);
      }

   }

   @Override
   protected void onPause() {
      super.onPause();
      SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
      Editor editor = prefs.edit();

      editor.putString(SAVED_EDIT_TEXT, m_vwJokeEditText.getText().toString());
      editor.commit();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = this.getSupportMenuInflater();
      inflater.inflate(R.menu.mainmenu, menu);
      this.m_vwMenu = menu;
      return true;
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      MenuItem item = menu.findItem(R.id.menu_filter);
      item.setTitle(getMenuTitleChange());
      m_vwMenu = menu;
      return super.onPrepareOptionsMenu(menu);

   }

   /**
    * Method is used to encapsulate the code that initializes and sets the
    * Layout for this Activity.
    */
   protected void initLayout() {
      this.setContentView(R.layout.advanced);
      this.m_vwJokeLayout = (ListView) this
            .findViewById(R.id.jokeListViewGroup);
      this.m_vwJokeLayout.setAdapter(m_jokeAdapter);

      this.m_vwJokeLayout
            .setOnItemLongClickListener(new OnItemLongClickListener() {

               public boolean onItemLongClick(AdapterView<?> parent, View view,
                     int position, long id) {
                  if (mActionMode != null) {
                     return false;
                  }
                  mActionMode = getSherlock().startActionMode(
                        mActionModeCallback);
                  selected_position = position;
                  return true;
               }
            });

      this.m_vwJokeEditText = (EditText) this
            .findViewById(R.id.newJokeEditText);
      this.m_vwJokeButton = (Button) this.findViewById(R.id.addJokeButton);

      mActionModeCallback = new Callback() {
         public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.actionmenu, menu);
            return true;
         }

         public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
         }

         public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.menu_remove:
               removeJoke(((JokeView) m_vwJokeLayout
                     .getChildAt(selected_position)).getJoke());
               mode.finish();
               return true;

            default:
               return false;
            }
         }

         public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
         }
      };
   }

   /**
    * Method is used to encapsulate the code that initializes and sets the Event
    * Listeners which will respond to requests to "Add" a new Joke to the list.
    */
   protected void initAddJokeListeners() {
      this.m_vwJokeButton.setOnClickListener(new OnClickListener() {
         public void onClick(View view) {
            String jokeText = m_vwJokeEditText.getText().toString();
            if (jokeText != null && !jokeText.equals("")) {
               addJoke(new Joke(jokeText, m_strAuthorName));
               m_vwJokeEditText.setText("");
               InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
               imm.hideSoftInputFromWindow(m_vwJokeEditText.getWindowToken(), 0);
            }
         }
      });

      this.m_vwJokeEditText.setOnKeyListener(new OnKeyListener() {
         public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                  && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
               String jokeText = m_vwJokeEditText.getText().toString();
               if (jokeText != null && !jokeText.equals("")) {
                  addJoke(new Joke(jokeText, m_strAuthorName));
                  m_vwJokeEditText.setText("");
                  return true;
               }
            }
            if (event.getAction() == KeyEvent.ACTION_UP
                  && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
               InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
               imm.hideSoftInputFromWindow(m_vwJokeEditText.getWindowToken(), 0);
               return true;
            }
            return false;
         }
      });
   }

   /**
    * Method used for encapsulating the logic necessary to properly add a new
    * Joke to m_arrJokeList, and display it on screen.
    * 
    * @param joke
    *           The Joke to add to list of Jokes.
    */
   protected void addJoke(Joke joke) {
      Uri uri = Uri
            .parse("content://edu.calpoly.android.lab4.contentprovider/joke_table/jokes/"
                  + joke.getID());
      ContentValues values = new ContentValues();
      values.put(JokeTable.JOKE_KEY_RATING, joke.getRating());
      values.put(JokeTable.JOKE_KEY_AUTHOR, joke.getAuthor());
      values.put(JokeTable.JOKE_KEY_TEXT, joke.getJoke());
      joke.setID(Long.valueOf(getContentResolver().insert(uri, values)
            .getLastPathSegment()));
      fillData();
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      MenuItem other = m_vwMenu.findItem(R.id.menu_filter);
      switch (item.getItemId()) {
      case R.id.submenu_like:
         filterJokeList(FILTER_LIKE);
         break;

      case R.id.submenu_dislike:
         filterJokeList(FILTER_DISLIKE);
         break;

      case R.id.submenu_unrated:
         filterJokeList(FILTER_UNRATED);
         break;

      case R.id.submenu_show_all:
         filterJokeList(FILTER_SHOW_ALL);
         break;

      default:
         return super.onOptionsItemSelected(item);
      }
      other = m_vwMenu.findItem(R.id.menu_filter);
      other.setTitle(getMenuTitleChange());
      return true;
   }

   private void filterJokeList(int filter) {
      this.m_nFilter = filter;
      fillData();
   }

   protected void removeJoke(Joke jv) {
      Uri uri = Uri
            .parse("content://edu.calpoly.android.lab4.contentprovider/joke_table/jokes/"
                  + jv.getID());
      getContentResolver().delete(uri, null, null);
      fillData();
   }

   public String getMenuTitleChange() {
      Resources res = this.getResources();

      switch (m_nFilter) {
      case FILTER_SHOW_ALL:
         return res.getString(R.string.show_all_menuitem);
      case FILTER_DISLIKE:
         return res.getString(R.string.dislike_menuitem);
      case FILTER_LIKE:
         return res.getString(R.string.like_menuitem);
      case FILTER_UNRATED:
         return res.getString(R.string.unrated_menuitem);
      }
      return res.getString(R.string.filter_menuitem);
   }

   @Override
   public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
      String[] projection = { JokeTable.JOKE_KEY_ID, JokeTable.JOKE_KEY_TEXT,
            JokeTable.JOKE_KEY_RATING, JokeTable.JOKE_KEY_AUTHOR };
      Uri uri = Uri
            .parse("content://edu.calpoly.android.lab4.contentprovider/joke_table/filters/"
                  + getFilterId());
      CursorLoader loader = new CursorLoader(this, uri, projection, null, null,
            null);
      return loader;
   }

   @Override
   public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
      m_jokeAdapter.swapCursor(arg1);
      this.m_jokeAdapter.setOnJokeChangeListener(this);

   }

   @Override
   public void onLoaderReset(Loader<Cursor> arg0) {
      m_jokeAdapter.swapCursor(null);

   }

   public String getFilterId() {
      switch (m_nFilter) {
      case FILTER_LIKE:
         return "" + Joke.LIKE;
      case FILTER_DISLIKE:
         return "" + Joke.DISLIKE;
      case FILTER_UNRATED:
         return "" + Joke.UNRATED;
      default:
         return "" + SHOW_ALL_FILTER_STRING;
      }
   }

   public void fillData() {
      getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
      m_vwJokeLayout.setAdapter(m_jokeAdapter);
   }

   @Override
   public void onJokeChanged(JokeView view, Joke joke) {
      Uri uri = Uri
            .parse("content://edu.calpoly.android.lab4.contentprovider/joke_table/jokes/"
                  + joke.getID());
      ContentValues values = new ContentValues();
      values.put(JokeTable.JOKE_KEY_RATING, joke.getRating());
      values.put(JokeTable.JOKE_KEY_AUTHOR, joke.getAuthor());
      values.put(JokeTable.JOKE_KEY_TEXT, joke.getJoke());
      getContentResolver().update(uri, values, null, null);
      this.m_jokeAdapter.setOnJokeChangeListener(null);
      fillData();

   }
}