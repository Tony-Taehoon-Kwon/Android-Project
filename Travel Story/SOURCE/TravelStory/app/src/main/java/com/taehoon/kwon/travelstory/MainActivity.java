package com.taehoon.kwon.travelstory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.taehoon.kwon.travelstory.Itinerary.ExploreStories.ExploreStoryContainerFragment;
import com.taehoon.kwon.travelstory.Itinerary.ExploreStories.ExploreStoryMainFragment;
import com.taehoon.kwon.travelstory.Itinerary.StoryBook.StoryBookContainerFragment;
import com.taehoon.kwon.travelstory.Itinerary.StoryBook.StoryBookMainFragment;
import com.taehoon.kwon.travelstory.Settings.SettingsContainerFragment;
import com.taehoon.kwon.travelstory.Settings.SettingsFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String current_fragment_tag;

    private AppBarConfiguration appBarConfiguration;
    public static NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_dashboard, R.id.nav_story_book,
                R.id.nav_search_story, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_dashboard_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_dashboard_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {

        Fragment activeFragment = getSupportFragmentManager().findFragmentByTag(current_fragment_tag);

        if (activeFragment instanceof StoryBookContainerFragment
                && current_fragment_tag != StoryBookMainFragment.FRAG_TAG) {
            ((StoryBookContainerFragment) activeFragment).onBackPressed();
        }
        else if (activeFragment instanceof ExploreStoryContainerFragment
                && current_fragment_tag != ExploreStoryMainFragment.FRAG_TAG) {
            ((ExploreStoryContainerFragment) activeFragment).onBackPressed();
        }
        else if (activeFragment instanceof SettingsContainerFragment
                && current_fragment_tag != SettingsFragment.FRAG_TAG) {
            ((SettingsContainerFragment) activeFragment).onBackPressed();
        }
        else {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for(Fragment f : fragments){
                if(f instanceof StoryBookContainerFragment)
                    ((StoryBookContainerFragment)f).onBackPressed();
            }
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        overridePendingTransition(0, 0);
    }
}
