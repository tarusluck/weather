package com.deskcode.forecasting.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deskcode.forecasting.R;
import com.deskcode.forecasting.activity.MainActivity;
import com.deskcode.forecasting.models.ForecastModel;

import java.util.ArrayList;
import java.util.List;

public class RecycleViewWeather extends RecyclerView.Adapter<RecycleViewWeather.MyViewHolder> {
    List<ForecastModel> usersArrayList;
    ArrayList<String> stringArrayList;
    Context context;


    public RecycleViewWeather(MainActivity context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View layoutInflater = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_view_weather, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(layoutInflater);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
//        myViewHolder.tvUserName.setText(usersArrayList.get(i).getTitle());
    }

    @Override
    public int getItemCount() {

        return 8;


    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTemp;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTemp = itemView.findViewById(R.id.tvTemp);
        }
    }
}
