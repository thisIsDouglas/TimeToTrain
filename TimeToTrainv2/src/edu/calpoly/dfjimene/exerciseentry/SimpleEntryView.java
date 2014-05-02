package edu.calpoly.dfjimene.exerciseentry;

import edu.calpoly.dfjimene.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SimpleEntryView extends LinearLayout {

   /** TextView containing exercise name */
   private TextView m_vwExerciseName;

   /** The contained SimpleEntry */
   private SimpleEntry m_entry;

   /** SimpleEntryChange Listener */
   private OnSimpleEntryChangeListener m_listener;

   /**
    * SimpleEntryView constructor
    * 
    * @param context
    *           the context
    * @param entry
    *           the simple entry to contain
    */
   public SimpleEntryView(Context context, SimpleEntry entry) {
      super(context);
      LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      inflater.inflate(R.layout.simple_entry_view, this, true);
      this.m_vwExerciseName = (TextView) findViewById(R.id.EntryText);
      this.m_listener = null;
      this.setSimpleEntry(entry);
   }

   public void setSimpleEntry(SimpleEntry entry) {
      this.m_entry = entry;
      m_vwExerciseName.setText(entry.getExerciseName());

   }

   public SimpleEntry getSimpleEntry() {
      return this.m_entry;
   }

   /**
    * Mutator to set OnChangeListener for this SimpleEntryView
    * 
    * @param listener
    *           OnSessionChangeListener
    */
   public void setOnSimpleEntryChangeListener(
         OnSimpleEntryChangeListener listener) {
      this.m_listener = listener;
   }

   public void notifyOnSimpleEntryChangeListener() {
      if (this.m_listener != null) {
         this.m_listener.onSimpleEntryChanged(this, this.m_entry);
      }
   }

   public static interface OnSimpleEntryChangeListener {
      /**
       * Called when the underlying Session in a SessionView object changes
       * state.
       * 
       * @param view
       *           The SimpleEntryView in which the entry was changed.
       * @param entry
       *           The entry that was changed.
       */
      public void onSimpleEntryChanged(SimpleEntryView view, SimpleEntry entry);
   }

}
