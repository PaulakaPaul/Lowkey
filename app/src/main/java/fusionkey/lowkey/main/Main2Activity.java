package fusionkey.lowkey.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import fusionkey.lowkey.LowKeyApplication;
import fusionkey.lowkey.R;

public class Main2Activity extends AppCompatActivity {
    private LoadingAsyncTask loadingAsyncTask;
    public static String currentUser="SebastianDevTeam";
    static public boolean SEARCH_STATE;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */

    private ViewPager mViewPager;
    ProgressBar progressBar;
    SharedPreferences sharedPreferences;
    private CardView searchCard;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        // Determine if this is first start - and whether to show app intro
        // Determine if the user is logged in
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        /**
         * @To-DO Tried to get the user Atributes
         */
        //Map<String,String> userDetails = UserManager.getInstance(getApplicationContext()).getUserDetails().getAttributes().getAttributes();
        // String currentUser = userDetails.get("username");
        /**
         * @return the EMAIL
         */
        // currentUser = UserManager.getInstance(this).getUser().getUserId();
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorHeight(0);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        progressBar = (ProgressBar) findViewById(R.id.loadingBar);
        searchCard = (CardView) findViewById(R.id.searchCard);
        imageView = findViewById(R.id.imageView8);


        if(loadState()==1)
            searchForHelp();
        if(loadState()==2)
            helpOthers();
        else
            doNothing();
    }

    private void searchForHelp(){
        loadingAsyncTask = new LoadingAsyncTask(currentUser, this, progressBar, true,searchCard);
        loadingAsyncTask.execute();
        saveState("step",0);
    }

    private void helpOthers(){
        searchCard.setVisibility(View.VISIBLE);
        loadingAsyncTask = new LoadingAsyncTask(currentUser, this, progressBar, false,searchCard);

        loadingAsyncTask.execute();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingAsyncTask.cancel(true);
                saveState("step",0);
                doNothing();
            }
        });
    }

    private void doNothing(){
        searchCard.setVisibility(View.GONE);
    }

    private void saveState(String key,int step){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, step);
        editor.apply();
    }
    private int loadState(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int i = sharedPreferences.getInt("step", 0);
        return i;
    }


    @Override
    public void onBackPressed(){

    }

    @Override
    protected void onDestroy() {
        if(loadingAsyncTask!=null)
        loadingAsyncTask.cancel(true);
        searchCard=null;
        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    ProfileTab Chat = new ProfileTab();
                    return Chat;
                case 1:
                    NewsFeedTab Contacts = new NewsFeedTab();
                    return Contacts;
                case 2:
                    StartTab Profile = new StartTab();
                    return Profile;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
        @Override
        public CharSequence getPageTitle(int position){
            switch (position){
                case 0:
                    return "Chat";
                case 1:
                    return "Contacts";
                case 2:
                    return "Profile";

            }
            return null;
        }
    }

}
