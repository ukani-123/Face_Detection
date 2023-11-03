package com.example.facedetection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class StickerOperationAdapter extends RecyclerView.Adapter<StickerOperationAdapter.ViewHolder> {
    Context context;
    ArrayList<StickerOperationModel> adjustList;
    int selectedItem = -1;
    StickerOperation adjust;


    public StickerOperationAdapter(Context context, ArrayList<StickerOperationModel> adjustList, StickerOperation adjust) {
        this.context = context;
        this.adjustList = adjustList;
        this.adjust = adjust;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate( R.layout.sticker_operation_adapter_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.imageView.setImageResource(adjustList.get(position).getIcon());
        holder.txtTool.setText(adjustList.get(position).getName());

        if (position == selectedItem) {
            Drawable drawable = holder.imageView.getDrawable();
            drawable.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
            holder.imageView.setImageDrawable(drawable);
        } else {
            Drawable drawable = holder.imageView.getDrawable();
            drawable.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
            holder.imageView.setImageDrawable(drawable);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjust.onClickAdjust(position);

                int previousSelectedItem = selectedItem;
                selectedItem = position;
                notifyItemChanged(previousSelectedItem);
                notifyItemChanged(selectedItem);

            }
        });

    }
    @Override
    public int getItemCount() {
        return adjustList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtTool;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageToolsView);
            txtTool = itemView.findViewById(R.id.txtTool);
        }
    }
}
