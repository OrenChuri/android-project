package com.example.user.RateEat;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.user.RateEat.Model.Listeners;
import com.example.user.RateEat.Model.Restaurant;
import com.example.user.RateEat.Restaurant.CreateRest;
import com.example.user.RateEat.Restaurant.RestPage;
import com.example.user.RateEat.Restaurant.RestaurantsViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * App's home fragment includes view of the restaurants in the database.
 */

public class HomeActivity extends Fragment {
    private FragmentActivity myContext;

    private static final int REQUEST_WRITE_STORAGE = 1338;

    private CustomAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean hasPermission = (ContextCompat.checkSelfPermission(MyApplication.getMyContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!hasPermission) {
            ActivityCompat.requestPermissions(myContext,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_home,
                container, false);
    }

    public static HomeActivity newInstance() {
        return new HomeActivity();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ListView restaurantsView = (ListView)view.findViewById(R.id.listRestaurants);
        adapter = new CustomAdapter();

        RestaurantsViewModel restaurantsViewModel = ViewModelProviders.of(this).get(RestaurantsViewModel.class);
        restaurantsViewModel.getList().observe(this, new Observer<List<Restaurant>>() {
            @Override
            public void onChanged(@Nullable List<Restaurant> rests) {
                adapter.restaurants = (ArrayList<Restaurant>)rests;
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });

        restaurantsView.setAdapter(adapter);

        // Set footer
        TextView footer = new TextView(view.getContext());
        footer.setText("Add new rest!");
        footer.setTextSize(30);
        footer.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        footer.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), CreateRest.class);
                        startActivity(intent);
                    }
                }
        );

        restaurantsView.addFooterView(footer);
    }

    @Override
    public void onAttach(Context context) {
        myContext = (FragmentActivity) context;
        super.onAttach(context);
    }

    class CustomAdapter extends BaseAdapter implements Listeners.StatusListener<List<Restaurant>> {

        public List<Restaurant> restaurants;

        CustomAdapter() {
            restaurants = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return restaurants.size();
        }

        @Override
        public Object getItem(int position) {
            return restaurants.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.rest_item,null);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(getClass().getName(), String.format("Click %s", restaurants.get((Integer)v.getTag()).id));

                    Fragment fragment = RestPage.newInstance(restaurants.get((Integer)v.getTag()));

                    FragmentTransaction tran = getFragmentManager().beginTransaction();
                    tran.replace(R.id.content, fragment).addToBackStack(RestPage.class.getName());
                    tran.commit();
                }
            });

            convertView.setTag(position);

            if (restaurants.size() > 0) {
                ImageView image = (ImageView) convertView.findViewById(R.id.restuarant_item_image);
                TextView name = (TextView) convertView.findViewById(R.id.restuarant_item_title);
                TextView location = (TextView) convertView.findViewById(R.id.restuarant_item_secondary);

                Restaurant rest = restaurants.get(position);

                Log.d(getClass().getName(), String.format("Rest name: %s, id: %s, url: %s", rest.name, rest.id, rest.imageURL));

                name.setText(rest.name);
                location.setText(rest.location);
                image.setImageResource(R.mipmap.ic_launcher_round);

                Utils.setImageView(image, rest.imageURL);
            }

            return convertView;
        }

        @Override
        public void onComplete(List<Restaurant> items) {
            this.restaurants = items;
            notifyDataSetChanged();
        }
    }
}
