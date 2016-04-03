package com.example.yurab.player13;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


//RecyclerView ArrayList/DB adapter
public final class RvAdapter extends RecyclerView.Adapter<RvAdapter.myViewHolder> {
    private LayoutInflater inflater;
    private ArrayList<Track> data;
    private Context context;

    //Creating Adapter
    public RvAdapter(Context context, ArrayList<Track> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
    }


    //Adding ViewHolder
    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.track_card_view, parent, false);
        myViewHolder viewHolder = new myViewHolder(view);

        return viewHolder;
    }

    private String formatDuration(int millis) {
        return String.format("%02d:%02d ",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    @Override
    public void onBindViewHolder(myViewHolder holder, int position) {
        //Filling ViewHolder with Persons from ArrayList
        Track current = data.get(position);
        holder.title.setText(current.getTitle());
        holder.title.setTag(current.getId());
        holder.artist.setText(current.getArtist());
        holder.duration.setText(formatDuration(current.getDuration()));

        //Setting current id to cardView to use it later in Database update operation
        holder.cardView.setTag(position);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    //Creating ViewHolder class
    final class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title, artist, duration;
        private ImageButton pausePlay;
        private CardView cardView;

        public myViewHolder(View itemView) {
            super(itemView);
            //Init views
            title = (TextView) itemView.findViewById(R.id.twTitle_TCV);
            artist = (TextView) itemView.findViewById(R.id.twArtist_TCV);
            duration = (TextView) itemView.findViewById(R.id.twDuration_TCV);
            pausePlay = (ImageButton) itemView.findViewById(R.id.pausePlay_TCV);


            cardView = (CardView) itemView.findViewById(R.id.cardView);
            //Attaching listener
            cardView.setOnClickListener(this);

        }


        @Override
        public void onClick(View v) {

            int id = (int) v.getTag();

            EventHandler eventHandler = (EventHandler) context;
            eventHandler.play(id);


        }


    }
}