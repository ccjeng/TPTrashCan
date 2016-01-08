package com.ccjeng.tptrashcan.adapter;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ccjeng.tptrashcan.R;
import com.ccjeng.tptrashcan.ui.TrashCanItem;
import com.ccjeng.tptrashcan.utils.Utils;
import com.parse.ParseQueryAdapter;

/**
 * Created by andycheng on 2016/1/6.
 */
public class TrashCanAdapter extends ParseRecyclerQueryAdapter<TrashCanItem, TrashCanAdapter.CustomViewHolder>{

    private static final String TAG = "TrashCanAdapter";
    private TrashCanItem mTrash;
    private Location mLocation;
    private Context mContext;

/*
    public TrashCanAdapter(boolean hasStableIds) {
        super(TrashCanItem.class, hasStableIds);
    }
*/
    public TrashCanAdapter(ParseQueryAdapter.QueryFactory<TrashCanItem> factory, boolean hasStableIds, Location myLoc) {
        super(factory, hasStableIds);
        this.mLocation = myLoc;
    }

/*
    public TrashCanAdapter(Context context, TrashCanItem trash, Location myLoc) {
        this.mTrash = trash;
        this.mLocation = myLoc;
        this.mContext = context;
    }
*/

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trashcan_item, parent, false);

        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {

        TrashCanItem trash = getItem(i);

        //Log.d(TAG, "item = " + i);

        //Setting text view title
        customViewHolder.regionView.setText(trash.getRegion());
        customViewHolder.distanceView.setText(trash.getDistance(Utils.geoPointFromLocation(mLocation)));
        customViewHolder.addressView.setText(trash.getFullAddress());
    }

    @Override
    public int getItemCount() {
        return getItemCount();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        //protected ImageView imageView;
        protected TextView regionView;
        protected TextView distanceView;
        protected TextView addressView;

        public CustomViewHolder(View view) {
            super(view);
            //this.imageView = (ImageView) view.findViewById(R.id.icon);
            this.regionView = (TextView) view.findViewById(R.id.region_view);
            this.distanceView = (TextView) view.findViewById(R.id.distance_view);
            this.addressView = (TextView) view.findViewById(R.id.address_view);
        }

    }
}
