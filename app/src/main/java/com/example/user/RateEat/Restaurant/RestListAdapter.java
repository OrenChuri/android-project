package com.example.user.RateEat.Restaurant;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.RateEat.Model.Listeners;
import com.example.user.RateEat.Model.Restaurant;
import com.example.user.RateEat.R;
import com.example.user.RateEat.Utils;

import java.util.ArrayList;
import java.util.List;

public class RestListAdapter extends BaseAdapter implements Listeners.StatusListener<List<Restaurant>> {
    public List<Restaurant> rests;
    private Context myContext;

    public RestListAdapter(Context context) {
        rests = new ArrayList<Restaurant>();
        myContext = context;
    }

    @Override
    public int getCount() {
        return rests.size();
    }

    @Override
    public Object getItem(int i) {
        return rests.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(myContext);
            view = inflater.inflate(R.layout.rest_item, null);
        }

        TextView name = (TextView) view.findViewById(R.id.restuarant_item_title);
        TextView location = (TextView) view.findViewById(R.id.restuarant_item_secondary);
        ImageView imgView = (ImageView) view.findViewById(R.id.restuarant_item_image);

        view.setTag(i);

        Restaurant rest = rests.get(i);

        if (rest != null) {
            name.setText(rest.name);
            location.setText(rest.location);
            imgView.setImageResource(R.drawable.ic_launcher_foreground);

            Utils.setImageView(imgView, rest.imageURL);
        }

        return view;
    }

    @Override
    public void onComplete(List<Restaurant> rests) {
        this.rests = rests;
        notifyDataSetChanged();
    }
}
