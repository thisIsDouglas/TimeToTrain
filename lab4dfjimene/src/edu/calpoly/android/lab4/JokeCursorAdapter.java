package edu.calpoly.android.lab4;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import edu.calpoly.android.lab4.JokeView.OnJokeChangeListener;

/**
 * Class that functions similarly to JokeListAdapter, but instead uses a Cursor.
 * A Cursor is a list of rows from a database that acts as a medium between the
 * database and a ViewGroup (in this case, a SQLite database table containing
 * rows of jokes and a ListView containing JokeViews).
 */
public class JokeCursorAdapter extends CursorAdapter {

   /**
    * The OnJokeChangeListener that should be connected to each of the JokeViews
    * created/managed by this Adapter.
    */
   private OnJokeChangeListener m_listener;

   /**
    * Parameterized constructor that takes in the application Context in which
    * it is being used and the Collection of Joke objects to which it is bound.
    * 
    * @param context
    *           The application Context in which this JokeListAdapter is being
    *           used.
    * 
    * @param jokeCursor
    *           A Database Cursor containing a result set of Jokes which should
    *           be bound to JokeViews.
    * 
    * @param flags
    *           A list of flags that decide this adapter's behavior.
    */
   public JokeCursorAdapter(Context context, Cursor jokeCursor, int flags) {
      super(context, jokeCursor, flags);
   }

   /**
    * Mutator method for changing the OnJokeChangeListener.
    * 
    * @param listener
    *           The OnJokeChangeListener that will be notified when the internal
    *           state of any Joke contained in one of this Adapters JokeViews is
    *           changed.
    */
   public void setOnJokeChangeListener(OnJokeChangeListener mListener) {
      this.m_listener = mListener;
   }

   @Override
   public void bindView(View arg0, Context arg1, Cursor arg2) {
      Joke joke = new Joke(arg2.getString(JokeTable.JOKE_COL_TEXT),
            arg2.getString(JokeTable.JOKE_COL_AUTHOR),
            arg2.getInt(JokeTable.JOKE_COL_RATING),
            arg2.getInt(JokeTable.JOKE_COL_ID));
      JokeView view = (JokeView) arg0;
      view.setOnJokeChangeListener(null);
      view.setJoke(joke);
      view.setOnJokeChangeListener(this.m_listener);

   }

   @Override
   public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
      Joke joke = new Joke(arg1.getString(JokeTable.JOKE_COL_TEXT),
            arg1.getString(JokeTable.JOKE_COL_AUTHOR),
            arg1.getInt(JokeTable.JOKE_COL_RATING),
            arg1.getLong(JokeTable.JOKE_COL_ID));
      JokeView view = new JokeView(arg0, joke);
      view.setOnJokeChangeListener(this.m_listener);
      return view;
   }
}