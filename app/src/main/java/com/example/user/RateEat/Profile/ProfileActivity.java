package com.example.user.RateEat.Profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.RateEat.ImagePicker;
import com.example.user.RateEat.Model.Listeners;
import com.example.user.RateEat.Model.Model;
import com.example.user.RateEat.Model.User;
import com.example.user.RateEat.R;
import com.example.user.RateEat.Utils;
import com.google.firebase.auth.FirebaseAuth;

import static android.app.Activity.RESULT_OK;

/**
 * User's profile, shows and manipulates the user's data.
 */

public class ProfileActivity extends Fragment {
    private FragmentActivity myContext;
    private FragmentTabHost tabHost;

    private TextView _name;
    private ImageView _img;
    private Bitmap image;

    private String currProfileID;
    private User currUser;

    public static ProfileActivity newInstance(String UID) {
        ProfileActivity myFragment = new ProfileActivity();

        Bundle args = new Bundle();
        args.putString("UID", UID);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_profile,
                container, false);

        currProfileID = getArguments().getString("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Model.getInstance().userModel.getById(currProfileID, new Listeners.StatusListener<User>() {
            @Override
            public void onComplete(User item) {
                currUser = item;

                _name.setText(currUser.displayName);

                if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(currProfileID)) {
                    _img.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ImagePicker.class);
                        startActivityForResult(intent, ImagePicker.GET_IMAGE);
                        }
                    });
                }

                Utils.setImageView(_img, currUser.imageURL);
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        _name = (TextView)view.findViewById(R.id.profile_name);
        _img = (ImageView) view.findViewById(R.id.profile_image);

        // Setup tabhost
        tabHost = (FragmentTabHost)view.findViewById(R.id.profile_tabs);
        tabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);

        TabHost.TabSpec specHistory = tabHost.newTabSpec("profile_history_spec");
        specHistory.setIndicator("History", null);
        tabHost.addTab(specHistory, HistorySpec.class, getArguments());

        TabHost.TabSpec specFriends = tabHost.newTabSpec("friends");
        specFriends.setIndicator("Friends", ContextCompat.getDrawable(myContext, R.drawable.friends));
        tabHost.addTab(specFriends, FriendsSpec.class, getArguments());

        tabHost.addTab(tabHost.newTabSpec("favs").setIndicator("Favs", null),
                FavsSpec.class, getArguments());

        tabHost.setCurrentTabByTag("profile_history_spec");
    }

    private void saveImage() {
        Model.getInstance().saveImage(image, "user/" + currProfileID, new Listeners.StatusListener<String>() {
            @Override
            public void onComplete(String item) {
                if (item == null) {
                    Toast.makeText(myContext, "Failed to save image", Toast.LENGTH_SHORT).show();
                    return;
                }

                currUser.imageURL = item;
                Model.getInstance().userModel.insert(currUser);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getName(), requestCode + " " + resultCode);

        if (requestCode == ImagePicker.GET_IMAGE && resultCode == RESULT_OK) {

            image = BitmapFactory.decodeByteArray(data.getByteArrayExtra("image"),0,
                                                  data.getByteArrayExtra("image").length);
            _img.setImageBitmap(image);

            new AlertDialog.Builder(getContext())
                    .setTitle("Save image?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Utils.deleteImage(currUser.imageURL);
                            saveImage();
                        }})
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (currUser.imageURL != null) {
                                Utils.setImageView(_img, currUser.imageURL);
                            } else {
                                _img.setImageResource(R.mipmap.ic_launcher_round);
                            }
                        }
                    }).show();
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        tabHost = null;
    }
};
