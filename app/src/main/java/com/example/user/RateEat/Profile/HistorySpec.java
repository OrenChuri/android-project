package com.example.user.RateEat.Profile;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.user.RateEat.EditTaste;
import com.example.user.RateEat.Model.Model;
import com.example.user.RateEat.Model.Restaurant;
import com.example.user.RateEat.Model.RestaurantFirebase;
import com.example.user.RateEat.Model.Taste;
import com.example.user.RateEat.Model.TasteRepository;
import com.example.user.RateEat.R;
import com.example.user.RateEat.Restaurant.RestaurantsViewModel;
import com.example.user.RateEat.SingletonTasteViewModelFactory;
import com.example.user.RateEat.Taste.TasteListAdapter;
import com.example.user.RateEat.Taste.TasteViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS;

/**
 * Taste history of a user - shows and gives deletion option.
 */

public class HistorySpec extends Fragment {
    private static final int EDIT_TASTE = 12345;
    private ListView tastesView;
    private TasteListAdapter adapter;
    private Context myContext;

    private String currProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_history_spec, container, false);

        // Tastes list
        tastesView = (ListView)v.findViewById(R.id.history_list);
        adapter = new TasteListAdapter(myContext);
        tastesView.setAdapter(adapter);
        tastesView.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);

        currProfile = getArguments().getString("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());

        Model.getInstance().tasteModel.getUserTastes(currProfile, adapter);

        tastesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                Log.d(getClass().getName(), "On item click" + position);

                int pos = parent.getPositionForView(v);
                final Taste currTaste = (Taste)adapter.getItem(pos);

                PopupMenu popup = new PopupMenu(myContext, v);
                popup.getMenuInflater().inflate(R.menu.taste_menu, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            // Next version!
//                            case R.id.menu_taste_profile:
//                                Fragment fragment = ProfileActivity.newInstance(currProfile);
//                                FragmentTransaction tran = getFragmentManager().beginTransaction();
//                                tran.replace(R.id.content, fragment);
//                                tran.commit();
                            case R.id.menu_taste_edit:
                                //Toast.makeText(myContext, "this feature will be available in next version", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getContext(),EditTaste.class);
                                intent.putExtra("author", currTaste.author);
                                intent.putExtra("authorId", currTaste.authorId);
                                intent.putExtra("date", currTaste.date);
                                intent.putExtra("description", currTaste.description);
                                intent.putExtra("id", currTaste.id);
                                intent.putExtra("imageURL", currTaste.imageURL);
                                intent.putExtra("restId", currTaste.restId);
                                intent.putExtra("starCount", currTaste.starCount);
                                intent.putExtra("title", currTaste.title);

                                startActivityForResult(intent, EDIT_TASTE);
                                break;
                            case R.id.menu_taste_delete:
                                if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(currProfile)) {
                                    TasteRepository.delete(currTaste);
                                } else {
                                    Toast.makeText(myContext, "Only author can delete his taste", Toast.LENGTH_SHORT).show();
                                }

                                break;
                            default:
                                Toast.makeText(myContext, "Default", Toast.LENGTH_SHORT).show();
                                break;
                        }

                        return true;
                    }
                });
            }
        });


        RestaurantsViewModel restaurantsViewModel = ViewModelProviders.of(this).get(RestaurantsViewModel.class);
        List<Restaurant> resList = restaurantsViewModel.getList().getValue();

        TasteViewModel viewModel = ViewModelProviders.of(this, SingletonTasteViewModelFactory.get()).get(TasteViewModel.class);

        for (Restaurant r : resList) {
            viewModel.setRest(r);

            viewModel.getList().observe(this, new Observer<List<Taste>>() {
                @Override
                public void onChanged(@Nullable List<Taste> list) {
                    adapter.tastes = (ArrayList<Taste>) list;
                    Log.d("Taste","len: "+list.size() + " deleted");
                    for (Taste taste : list) {
                        Log.d("Taste","id: "+taste.id+ " deleted: " + taste.isDeleted);
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                }
            });
        }

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EDIT_TASTE){
            adapter.notifyDataSetChanged();
        }
    }
}
