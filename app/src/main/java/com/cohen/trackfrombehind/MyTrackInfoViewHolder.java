package com.cohen.trackfrombehind;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyTrackInfoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected Button id_BTN_choose;
    protected TextView id_TXT_distance;
    protected TextView id_TXT_calories;
    protected TextView id_TXT_time;

    protected OnTrackInfoListener onTrackInfoListener;

    public MyTrackInfoViewHolder(@NonNull View itemView, OnTrackInfoListener onTrackInfoListener) {
        super(itemView);
        id_BTN_choose = itemView.findViewById(R.id.id_BTN_choose);
        id_TXT_calories = itemView.findViewById(R.id.id_TXT_calories);
        id_TXT_distance = itemView.findViewById(R.id.id_TXT_distance);
        id_TXT_time = itemView.findViewById(R.id.id_TXT_time);

        this.onTrackInfoListener = onTrackInfoListener;
        id_BTN_choose.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onTrackInfoListener.inTrackInfoClick(getAdapterPosition());
    }

    public interface OnTrackInfoListener{
        void inTrackInfoClick(int position);
    }
}
