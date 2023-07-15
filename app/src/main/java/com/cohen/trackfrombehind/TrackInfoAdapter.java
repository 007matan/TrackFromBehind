package com.cohen.trackfrombehind;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TrackInfoAdapter extends RecyclerView.Adapter<MyTrackInfoViewHolder> {

    Context context;
    TrackInfoList trackInfoList;

    protected MyTrackInfoViewHolder.OnTrackInfoListener mOnTrackInfoListener;

    public TrackInfoAdapter(Context context, TrackInfoList trackInfoList, MyTrackInfoViewHolder.OnTrackInfoListener mOnTrackInfoListener) {
        this.context = context;
        this.trackInfoList = trackInfoList;
        this.mOnTrackInfoListener = mOnTrackInfoListener;
    }

    @NonNull
    @Override
    public MyTrackInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_view, parent, false);
        MyTrackInfoViewHolder myTrackInfoViewHolder = new MyTrackInfoViewHolder(view, mOnTrackInfoListener);
        return myTrackInfoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyTrackInfoViewHolder holder, int position) {
        holder.id_TXT_distance.setText(holder.id_TXT_distance.getText().toString() + " " + String.valueOf(trackInfoList.getTracksInfo().get(position).getDistance()));
        holder.id_TXT_calories.setText(holder.id_TXT_calories.getText().toString() + " " + String.valueOf(trackInfoList.getTracksInfo().get(position).getCalories()));
        holder.id_TXT_time.setText(holder.id_TXT_time.getText().toString() + " " + trackInfoList.getTracksInfo().get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return trackInfoList.getTracksInfo() == null ? 0 : trackInfoList.getTracksInfo().size();
    }
}
