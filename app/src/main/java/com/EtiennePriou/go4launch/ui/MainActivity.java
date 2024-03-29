package com.EtiennePriou.go4launch.ui;

import com.EtiennePriou.go4launch.BuildConfig;
import com.EtiennePriou.go4launch.R;
import com.EtiennePriou.go4launch.SplashActivity;
import com.EtiennePriou.go4launch.base.BaseActivity;
import com.EtiennePriou.go4launch.di.DI;
import com.EtiennePriou.go4launch.di.ViewModelFactory;
import com.EtiennePriou.go4launch.models.Workmate;
import com.EtiennePriou.go4launch.services.firebase.helpers.UserHelper;
import com.EtiennePriou.go4launch.ui.details.DetailPlaceActivity;
import com.EtiennePriou.go4launch.ui.fragments.MapFragment;
import com.EtiennePriou.go4launch.ui.fragments.place_view.PlaceFragment;
import com.EtiennePriou.go4launch.ui.fragments.workmates_list.WorkmateFragment;
import com.EtiennePriou.go4launch.utils.CheckDate;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private int AUTOCOMPLETE_REQUEST_CODE = 1;

    private MainViewModel mMainViewModel;

    private TextView mtv_Menu_Mail;
    private TextView mtv_Menu_Name;
    private ImageView imgMenuProfile;
    private MenuItem itemSearchPlace, itemSearchPlaceW;
    private BottomNavigationView bottomNavigationView;

    @Override
    public int getLayoutContentViewID() { return R.layout.activity_main; }

    @Override
    protected void setupUi(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        imgMenuProfile = headerView.findViewById(R.id.img_menu_profile);
        mtv_Menu_Mail = headerView.findViewById(R.id.tv_menu_mail);
        mtv_Menu_Name = headerView.findViewById(R.id.tv_menu_name);
    }

    @Override
    protected void withOnCreate() {
        configureViewModel();
        setupBottomNavigation();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = auth.getCurrentUser();
        Places.initialize(this.getApplicationContext(), BuildConfig.PlaceApiKey);
        if (currentUser != null) {
            mFireBaseApi.setCurrentUser(currentUser);
            UserHelper.getUser(currentUser.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    mFireBaseApi.setActualUser(documentSnapshot.toObject(Workmate.class));
                    setupMenuInfo();
                }
            });
        }
        showFragment(mMainViewModel.getWichFrag());
    }

    private void configureViewModel(){
        ViewModelFactory viewModelFactory = DI.provideViewModelFactory();
        mMainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
    }

    private void setupBottomNavigation (){
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_map:
                        changeSearchVisibility(true);
                        mMainViewModel.setWichFrag(1);
                        showFragment(1);
                        return true;
                    case R.id.navigation_list_view:
                        changeSearchVisibility(true);
                        mMainViewModel.setWichFrag(2);
                        showFragment(2);
                        return true;
                    case R.id.navigation_workmates:
                        changeSearchVisibility(false);
                        mMainViewModel.setWichFrag(3);
                        showFragment(3);
                        return true;
                }
                return false;
            }
        });
    }
    private void changeSearchVisibility (Boolean aBoolean){
        itemSearchPlace.setVisible(aBoolean);
        itemSearchPlaceW.setVisible(!aBoolean);
    }
    private void showFragment(int wichFrag) {
        Fragment fragment;
        switch (wichFrag) {
            case 2 :
                fragment = PlaceFragment.newInstance(mMainViewModel);
                break;
            case 3 :
                fragment = WorkmateFragment.newInstance(mMainViewModel);
                break;
            default:
                fragment = MapFragment.newInstance(mMainViewModel);
                break;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void setupMenuInfo(){
        mtv_Menu_Mail.setText(mFireBaseApi.getCurrentUser().getEmail());
        if (mFireBaseApi.getActualUser() == null){
            mtv_Menu_Mail.setText(mFireBaseApi.getCurrentUser().getDisplayName());
        }else {
            mtv_Menu_Name.setText(mFireBaseApi.getActualUser().getUsername());
        }
        if (mFireBaseApi.getCurrentUser().getPhotoUrl() != null){
            Glide.with(imgMenuProfile.getContext())
                    .load(mFireBaseApi.getCurrentUser().getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(imgMenuProfile);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        itemSearchPlace = menu.findItem(R.id.app_bar_search);
        itemSearchPlaceW = menu.findItem(R.id.app_bar_search_workmate);
        SearchView searchViewWork = (SearchView) menu.findItem(R.id.app_bar_search_workmate).getActionView();
        itemSearchPlace.setVisible(true);
        itemSearchPlaceW.setVisible(false);
        searchViewWork.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mMainViewModel.setCurrentName(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.app_bar_search){
            setSearch(); }
        return true;
    }

    private void setSearch() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.TYPES);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setCountry("FR")
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE){
            assert data != null;
            if (resultCode == RESULT_OK){
                Place place = Autocomplete.getPlaceFromIntent(data);
                Intent intent = new Intent(this, DetailPlaceActivity.class);
                intent.putExtra("placeReference",place.getId());
                startActivity(intent);
            }else if (resultCode == AutocompleteActivity.RESULT_ERROR){
                Status status = Autocomplete.getStatusFromIntent(data);
                assert status.getStatusMessage() != null;
                Log.i("TAG", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, mContext.getString(R.string.cancel), Toast.LENGTH_SHORT).show();
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mMainViewModel.getWichFrag() == 2 || mMainViewModel.getWichFrag() == 3){
            bottomNavigationView.setSelectedItemId(R.id.navigation_map);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mFireBaseApi.getActualUser() != null){
            setupMenuInfo();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()){
            case R.id.nav_your_lunch:
                UserHelper.getUser(mFireBaseApi.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Workmate user = documentSnapshot.toObject(Workmate.class);

                        if (user.getPlaceToGo() != null && !CheckDate.isDatePast(user.getPlaceToGo().getDateCreated())){
                            Intent intent = new Intent(getApplicationContext(), DetailPlaceActivity.class);
                            intent.putExtra(getString(R.string.PLACEREFERENCE), Objects.requireNonNull(Objects.requireNonNull(user.getPlaceToGo().getPlaceRef())));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplication().startActivity(intent);
                        }else Toast.makeText(mContext, getString(R.string.noChoosePlace), Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.nav_setting:
                Intent intentSetting = new Intent(this,SettingsActivity.class);
                this.startActivity(intentSetting);
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this,SplashActivity.class);
                startActivity(intent);
                this.finish();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }
}
