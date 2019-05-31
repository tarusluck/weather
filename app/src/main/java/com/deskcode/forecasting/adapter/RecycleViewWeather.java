package com.deskcode.forecasting.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.deskcode.forecasting.R;
import com.deskcode.forecasting.activity.MainActivity;
import com.deskcode.forecasting.constants.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecycleViewWeather extends RecyclerView.Adapter<RecycleViewWeather.MyViewHolder> {
    ArrayList<String> todayDataList, todayTimeList;
    Context context;
    String iconName;


    public RecycleViewWeather(MainActivity context, ArrayList<String> todayDataList, ArrayList<String> todayTimeList) {
        this.context = context;
        this.todayDataList = todayDataList;
        this.todayTimeList = todayTimeList;
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
        try {
            JSONArray jsonArray = new JSONArray(todayDataList.get(i));
            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject jsonObj = jsonArray.getJSONObject(j);
                iconName = jsonObj.getString("icon");
                Glide.with(context).load(Constants.IMAGE_URL + iconName + Constants.IMAGE_TYPE).into(myViewHolder.ivWeatherList);
                myViewHolder.tvTime.setText(todayTimeList.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return todayDataList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        ImageView ivWeatherList;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivWeatherList = itemView.findViewById(R.id.ivWeatherList);
        }
    }
}
