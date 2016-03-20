package com.andrewnwalker.mousetimes_california;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by andy500mufc on 30/01/2016.
 */
public class TimeRowAdapter extends RecyclerView.Adapter<TimeRowHolder> {
    private String[] timesArray = {"Closed", "0m", "5m", "10m", "15m", "20m", "25m", "30m", "35m", "40m", "45m", "50m", "55m", "60m", "65m", "70m", "75m", "80m+"};
    private Context context;
    private Attraction currentAttraction;
    private Park currentPark;

    public TimeRowAdapter(Context context, Park currentPark, Attraction currentAttraction) {
        this.context = context;
        this.currentAttraction = currentAttraction;
        this.currentPark = currentPark;
    }

    @Override
    public int getItemCount() {
        return (currentAttraction.hasWaitTime ? timesArray.length : 2);
    }

    @Override
    public void onBindViewHolder(TimeRowHolder holder, int position) {
        final String waitTime = timesArray[position];

        if (position == 0) {
            holder.title.setText(waitTime);
            holder.background.setBackgroundColor(Color.parseColor("#FF571D"));
        } else if (!currentAttraction.hasWaitTime) {
            holder.title.setText("Open");
            holder.background.setBackgroundColor(Color.parseColor("#00FA2E"));
        } else {
            holder.title.setText(waitTime);
            holder.background.setBackgroundColor(Color.parseColor("#FF9400"));
        }
    }

    @Override
    public TimeRowHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.time_row, null);
        final TimeRowHolder holder = new TimeRowHolder(view);

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = holder.getAdapterPosition();

                final String timeSelected = timesArray[position];
                String timeSelectedValid = timeSelected.replaceAll("[^\\d]", "");

                if (position == 0) {
                    timeSelectedValid = "Closed";
                } else if (position == 1 && !currentAttraction.hasWaitTime) {
                    timeSelectedValid = "Open";
                }

                final String finalTimeSelected = timeSelectedValid;

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                alertDialogBuilder.setTitle("Submit Time");
                alertDialogBuilder.setMessage("Are you sure you want to submit a time of " + timeSelectedValid + " minutes? Please only submit times that are accurate!");
                alertDialogBuilder.setPositiveButton("I'm sure", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DataManager.sendUpdateToParse(context, finalTimeSelected, currentPark, currentAttraction);
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        return holder;
    }
}