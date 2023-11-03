package com.example.facedetection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BgAdapter extends RecyclerView.Adapter<BgAdapter.ViewHolder> {

    Context context;
    ArrayList<Integer> bgList;
    public  static  onSelectBG onSelectBG;

    public static void setOnSelectBG(BgAdapter.onSelectBG onSelectBG) {
        BgAdapter.onSelectBG = onSelectBG;
    }

    public BgAdapter(Context context, ArrayList<Integer> bgList) {
        this.context = context;
        this.bgList = bgList;

    }

    @NonNull
    @Override
    public BgAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.bg_adapter_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BgAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageResource(bgList.get(position));

    }

    @Override
    public int getItemCount() {
        return bgList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.bgImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSelectBG.onBGSelect(bgList.get(getAdapterPosition()));
                    Toast.makeText(context, "00", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public interface onSelectBG {
        void onBGSelect(int pos);
    }
}
