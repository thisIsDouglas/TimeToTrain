package edu.calpoly.dfjimene;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class ViewEntryActivity extends SherlockFragmentActivity {

   /** Session ID for this entry */
   private long m_sessionId;
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.view_entry_cardio);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      Bundle extras = getIntent().getExtras();   
      m_sessionId = extras.getLong("session");
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case android.R.id.home:
         Intent i = new Intent(this,
               SessionDetailsActivity.class);
         i.putExtra(SessionListActivity.INTENT_SESSION_ID, m_sessionId);
         NavUtils.navigateUpTo(this, i);
         finish();
         return true;

      default:
         return super.onOptionsItemSelected(item);
      }
   }

}
