package com.example.concordia_campus_guide.Activities;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.concordia_campus_guide.Database.AppDatabase;
import com.example.concordia_campus_guide.Fragments.InfoCardFragment.InfoCardFragment;
import com.example.concordia_campus_guide.Fragments.LocationFragment.LocationFragment;
import com.example.concordia_campus_guide.Fragments.POIFragment.POIFragment;
import com.example.concordia_campus_guide.Global.ApplicationState;
import com.example.concordia_campus_guide.Global.SelectingToFromState;
import com.example.concordia_campus_guide.Models.Buildings;
import com.example.concordia_campus_guide.Models.Floors;
import com.example.concordia_campus_guide.Models.Rooms;
import com.example.concordia_campus_guide.Models.Shuttles;
import com.example.concordia_campus_guide.Models.WalkingPoints;
import com.example.concordia_campus_guide.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MainActivity extends AppCompatActivity {

    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;
    InfoCardFragment infoCardFragment;
    LocationFragment locationFragment;
    POIFragment poiFragment;
    MainActivityViewModel mViewModel;
    private BottomSheetBehavior swipeableInfoCard;
    private BottomSheetBehavior swipeablePOICard;
    Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpDb();
        setContentView(R.layout.activity_main);
        initComponents();
        setSupportActionBar(myToolbar);
        showPOICard();
    }

    private void initComponents() {
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        fragmentManager = getSupportFragmentManager();
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        MainActivity.this.setTitle("ConUMaps");
        View infoCard = findViewById(R.id.bottom_card_scroll_view);
        View poiCard = findViewById(R.id.explore_bottom_card_scroll_view);
        swipeableInfoCard = BottomSheetBehavior.from(infoCard);
        swipeablePOICard = BottomSheetBehavior.from(poiCard);
        locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentById(R.id.locationFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.search) {
            Intent openSearch = new Intent(MainActivity.this,
                    SearchActivity.class);

            SelectingToFromState.setMyCurrentLocation(getMyCurrentLocation());

            startActivity(openSearch);
            return false;
        }

        return true;
    }

    /**
     * Show the info card fragment in the view
     *
     * @param buildingCode: the Building code
     */
    public void showInfoCard(String buildingCode){
        resetBottomCard();
        infoCardFragment = new InfoCardFragment(buildingCode);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.bottom_card_frame, infoCardFragment);
        fragmentTransaction.commit();
    }

    public void showPOICard(){
        resetBottomCard();
        poiFragment = new POIFragment();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.explore_bottom_card_frame, poiFragment);
        fragmentTransaction.commit();
    }

    public void resetBottomCard(){
        for (Fragment fragment : fragmentManager.getFragments()){
            if (fragment instanceof InfoCardFragment) {
                fragmentManager.beginTransaction().remove(fragment).commit();
            }
        }
        swipeableInfoCard.setState(BottomSheetBehavior.STATE_COLLAPSED);

    }

    /**
     * Defines the desired behavior on backpress
     */
    @Override
    public void onBackPressed(){
        Fragment fragment = fragmentManager.findFragmentById(R.id.bottom_card_frame);
        if(fragment!=null){
            showPOICard();
        }
        else{
            super.onBackPressed();
        }
    }

    private void setUpDb(){
        if(!ApplicationState.getInstance(this).isDbIsSet()){
            //delete previous db
            getApplication().getApplicationContext().deleteDatabase(AppDatabase.DB_NAME);

            //load buildings
            Buildings buildings = ApplicationState.getInstance(this).getBuildings();
            AppDatabase appDb = AppDatabase.getInstance(this);
            appDb.buildingDao().insertAll(buildings.getBuildings());

            //load floors
            Floors floors = ApplicationState.getInstance(this).getFloors();
            appDb.floorDao().insertAll(floors.getFloors());

            //load rooms
            Rooms rooms = ApplicationState.getInstance(this).getRooms();
            appDb.roomDao().insertAll(rooms.getRooms());

            // Load shuttle schedule
            Shuttles shuttles = ApplicationState.getInstance(this).getShuttles();
            appDb.shuttleDao().insertAll(shuttles.getShuttles());

            // Load walking points
            WalkingPoints walkingPoints = ApplicationState.getInstance(this).getWalkingPoints();
            appDb.walkingPointDao().insertAll(walkingPoints.getWalkingPoints());

            ApplicationState.getInstance(this).setDbIsSetToTrue();
        }
    }

    public Location getMyCurrentLocation() {
        return this.locationFragment.getCurrentLocation();
    }

    private float dpToPx(float dip){
        Resources r = getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
    }
}