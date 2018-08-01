package com.example.user.RateEat.Profile;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


import com.example.user.RateEat.Model.Model;
import com.example.user.RateEat.Model.Restaurant;
import com.example.user.RateEat.Restaurant.RestListAdapter;
import com.example.user.RateEat.R;
import com.google.firebase.auth.FirebaseAuth;

import static android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS;


public class FavsSpec extends Fragment {
    private ListView restsView;
    private RestListAdapter adapter;
    private Context myContext;
    private String currProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_favs_spec, container, false);

        // rests list
        restsView = (ListView)v.findViewById(R.id.favs_list);
        adapter = new RestListAdapter(myContext);
        restsView.setAdapter(adapter);
        restsView.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

        currProfile = getArguments().getString("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Model.getInstance().restaurantModel.getUserFavs(currProfile, adapter);

        restsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                Log.d(getClass().getName(), "On item click" + position);
                int pos = parent.getPositionForView(v);
                final Restaurant currRest = (Restaurant)adapter.getItem(pos);

            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        myContext = (FragmentActivity) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myContext = null;
    }
}