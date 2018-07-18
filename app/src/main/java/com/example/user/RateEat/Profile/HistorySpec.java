package com.example.user.RateEat.Profile;

import android.content.Context;
import android.os.Bundle;
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

import com.example.user.RateEat.Model.Model;
import com.example.user.RateEat.Model.Taste;
import com.example.user.RateEat.Model.TasteRepository;
import com.example.user.RateEat.R;
import com.example.user.RateEat.Taste.TasteListAdapter;
import com.google.firebase.auth.FirebaseAuth;

import static android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS;

/**
 * Taste history of a user - shows and gives deletion option.
 */

public class HistorySpec extends Fragment {
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
                                Toast.makeText(myContext, "For future use", Toast.LENGTH_SHORT).show();
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
