package edu.calpoly.dfjimene;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.os.Bundle;

public class SelectAndEditEntryActivity extends SherlockFragmentActivity {

   @Override
   public void onCreate(Bundle savedInstanceState){
      if(getIntent().hasExtra(SessionDetailsActivity.ENTRY_ID));
      super.onCreate(savedInstanceState);
   }


}
