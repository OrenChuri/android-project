package com.example.user.RateEat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.RateEat.Login.LoginActivity;
import com.example.user.RateEat.Model.AppLocalStore;
import com.example.user.RateEat.Model.Listeners;
import com.example.user.RateEat.Model.Model;
import com.example.user.RateEat.Model.User;
import com.example.user.RateEat.Profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Main activity is the first view to be shown to a registered user
 * guests will be redirected to the login screen.
 */

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // For DEBUG purposes
//        FirebaseAuth.getInstance().signOut();
//        AppLocalStore.deleteDataBase();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent, null);
        }

        initToolbar();
        setupDrawerLayout();

        // Set home as first
        if (savedInstanceState == null) {
            Fragment fragment = HomeActivity.newInstance();
            FragmentTransaction tran = getSupportFragmentManager().beginTransaction();
            tran.replace(R.id.content, fragment);
            tran.commit();
        }
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                return selectItem(menuItem);
            }
        });

        View navView = mNavigationView.getHeaderView(0);

        TextView username = (TextView)navView.findViewById(R.id.nav_header_username);
        TextView email = (TextView)navView.findViewById(R.id.nav_header_email);
        final ImageView image = (ImageView)navView.findViewById(R.id.nav_header_avatar);

        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currUser != null)
        {
            username.setText(currUser.getDisplayName());
            email.setText(currUser.getEmail());
            image.setImageResource(R.mipmap.ic_launcher_round);


            Model.getInstance().userModel.getById(currUser.getUid(), new Listeners.StatusListener<User>() {
                @Override
                public void onComplete(User item) {
                    if (item != null) {
                        Utils.setImageView(image, item.imageURL);
                    }
                }
            });
        }
    }

    /**
     * Swaps fragments in the main content view
     */
    private boolean selectItem(MenuItem menuItem) {
        menuItem.setChecked(true);
        Fragment fragment;
        FragmentTransaction tran;

        switch (menuItem.getItemId()) {

            case R.id.drawer_home:
                fragment = HomeActivity.newInstance();
                tran = getSupportFragmentManager().beginTransaction();
                tran.replace(R.id.content, fragment).addToBackStack(HomeActivity.class.getName());
                tran.commit();
                break;

            case R.id.drawer_profile:
                fragment = ProfileActivity.newInstance(FirebaseAuth.getInstance().getCurrentUser().getUid());
                tran = getSupportFragmentManager().beginTransaction();
                tran.replace(R.id.content, fragment).addToBackStack(ProfileActivity.class.getName());
                tran.commit();
                break;

            case R.id.drawer_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;

            default:

        }

        mDrawerLayout.closeDrawers();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Always returns to the home fragment from whatever fragment we are on.
     */
    @Override
    public void onBackPressed() {
        Log.d(getClass().getName(), "On back pressed " + getSupportFragmentManager().getBackStackEntryCount());

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            Log.d(getClass().getName(), "Backtrack: " + getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName());
            getSupportFragmentManager().popBackStack();
        }
    }
}
