package com.example.user.RateEat.Restaurant;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.RateEat.ImagePicker;
import com.example.user.RateEat.Model.Listeners;
import com.example.user.RateEat.Model.Model;
import com.example.user.RateEat.Model.Restaurant;
import com.example.user.RateEat.Model.Taste;
import com.example.user.RateEat.R;
import com.example.user.RateEat.SingletonTasteViewModelFactory;
import com.example.user.RateEat.Taste.TasteListAdapter;
import com.example.user.RateEat.Taste.TastePage;
import com.example.user.RateEat.Taste.TasteViewModel;
import com.example.user.RateEat.Utils;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Represents restaurant's data and gives tastes adder.
 */

public class RestPage extends Fragment {
    private final int ADD_TASTE = 0xaa;

    ListView tastesView;
    private TextView _name;
    private TextView _address;
    private ImageView _imgView;
    private Restaurant currRest;
    private Bitmap image;

    public TasteListAdapter adapter;

    private Context myContext;

    public static RestPage newInstance(Restaurant rest) {
        RestPage myFragment = new RestPage();

        Bundle args = new Bundle();
        args.putSerializable("rest", rest);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.rest_page, container, false);

        initView(v);
        setCurrentRest();

        return v;
    }

    private void initView(View v) {
        FloatingActionButton fab = (FloatingActionButton)v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(myContext, TastePage.class);
                intent.putExtra("currRest", currRest);
                startActivityForResult(intent, ADD_TASTE);
            }
        });

        _name = (TextView)v.findViewById(R.id.rest_page_name);
        _address = (TextView)v.findViewById(R.id.rest_page_address);
        _imgView = (ImageView)v.findViewById(R.id.rest_page_image);

        // Tastes list
        tastesView = (ListView)v.findViewById(R.id.rest_page_tastes_list);
        adapter = new TasteListAdapter(myContext);
        tastesView.setAdapter(adapter);
    }

    private void setCurrentRest() {

        currRest = (Restaurant)getArguments().getSerializable("rest");

        Log.d(getClass().getName(), currRest.toString());

        _name.setText(currRest.name);
        _address.setText(currRest.location);

        if (currRest.imageURL != null) {
            Utils.setImageView(_imgView, currRest.imageURL);
        }

        _imgView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ImagePicker.class);
                startActivityForResult(intent, ImagePicker.GET_IMAGE);
            }
        });

        TasteViewModel viewModel = ViewModelProviders.of(this, SingletonTasteViewModelFactory.get()).get(TasteViewModel.class);
        viewModel.setRest(currRest);

        viewModel.getList().observe(this, new Observer<List<Taste>>() {
            @Override
            public void onChanged(@Nullable List<Taste> list) {
                adapter.tastes = (ArrayList<Taste>)list;
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });
    }

    private void saveImage() {
        Utils.saveImage(image, "rest/" + currRest.id, new Listeners.StatusListener<String>() {
            @Override
            public void onComplete(String item) {
                if (item == null) {
                    Toast.makeText(myContext, "Failed to save image", Toast.LENGTH_SHORT).show();
                    return;
                }

                Model.getInstance().restaurantModel.insert(currRest.id, currRest.name, currRest.location, item);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getName(), requestCode + " " + resultCode);

        if (requestCode == ADD_TASTE && resultCode == RESULT_OK) {
            adapter.notifyDataSetChanged();
        } else if (requestCode == ImagePicker.GET_IMAGE && resultCode == RESULT_OK) {
            image = BitmapFactory.decodeByteArray(data.getByteArrayExtra("image"),0,
                    data.getByteArrayExtra("image").length);
            _imgView.setImageBitmap(image);

            new AlertDialog.Builder(getContext())
                    .setTitle("Save image?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Utils.deleteImage(currRest.imageURL);
                            saveImage();
                        }})
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (currRest.imageURL != null) {
                                Utils.setImageView(_imgView, currRest.imageURL);
                            } else {
                                _imgView.setImageResource(R.mipmap.ic_launcher_round);
                            }
                        }
                    }).show();
        }
    }

    @Override
    public void onAttach(Context context) {
        myContext = (Activity) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myContext = null;
    }
}
