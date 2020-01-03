package com.hexmeet.hjt.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.hexmeet.hjt.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NewOwnerAdapter  extends RecyclerView.Adapter<NewOwnerAdapter.MyViewHolder>{

    private Context context;
    private List<String>  list;

    public NewOwnerAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_new_owner_search, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        Glide.with(context).load(R.drawable.girl).apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(holder.mNewOwnerImg);
        holder.mNewOwnerName.setText(list.get(position));
        //点击监听
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onClick(view, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView mNewOwnerImg;
        TextView mNewOwnerName;
        public MyViewHolder(View itemView) {
            super(itemView);

            mNewOwnerImg = (ImageView) itemView.findViewById(R.id.new_owner_img);
            mNewOwnerName = (TextView) itemView.findViewById(R.id.new_owner_name);

        }

    }


    /**
     * Recyclerview的点击监听接口
     */
    public interface onItemClickListener {
        void onClick(View view, int pos);
    }

    private onItemClickListener onItemClickListener;

    public void setOnItemClickListener(NewOwnerAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
