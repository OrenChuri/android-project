package com.example.user.RateEat.Taste;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.user.RateEat.Model.Listeners;
import com.example.user.RateEat.Model.Taste;
import com.example.user.RateEat.R;
import com.example.user.RateEat.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * List adapter represents tastes
 */

public class TasteListAdapter extends BaseAdapter implements Listeners.StatusListener<List<Taste>> {
    public List<Taste> tastes;
    private Context myContext;

    public TasteListAdapter(Context context) {
        tastes = new ArrayList<Taste>();
        myContext = context;
    }

    @Override
    public int getCount() {
        return tastes.size();
    }

    @Override
    public Object getItem(int i) {
        return tastes.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            LayoutInflater inflater = LayoutInflater.from(myContext);
            view = inflater.inflate(R.layout.taste_item,null);
        }

        TextView author = (TextView) view.findViewById(R.id.taste_item_author);
        TextView title = (TextView) view.findViewById(R.id.taste_item_title);
        TextView desc = (TextView) view.findViewById(R.id.taste_item_description);
        TextView date = (TextView) view.findViewById(R.id.taste_item_date);
        ImageView imgView = (ImageView) view.findViewById(R.id.taste_item_img);
        RatingBar stars = (RatingBar) view.findViewById(R.id.taste_item_stars);

        view.setTag(i);

        Taste taste = tastes.get(i);

        if (taste != null) {
            author.setText(taste.author);
            title.setText(taste.title);
            title.setText(taste.title);
            desc.setText(taste.description);
            stars.setRating(taste.starCount);
            date.setText(taste.date);
            imgView.setImageResource(R.drawable.taster);

            Utils.setImageView(imgView, taste.imageURL);
        }

        return view;
    }

    @Override
    public void onComplete(List<Taste> tastes) {
        this.tastes = tastes;
        notifyDataSetChanged();
    }
}