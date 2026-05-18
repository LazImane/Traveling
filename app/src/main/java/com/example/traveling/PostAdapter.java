package com.example.traveling;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(PostItem post); //called when a post is posted
        void onLikeClick(PostItem post, int position); //called when a post is liked
    }

    private final List<PostItem> posts;
    private final OnPostClickListener listener;

    public PostAdapter(List<PostItem> posts, OnPostClickListener listener) {
        this.posts    = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(v);
    }// creates empty view holders which is the equivalent of an empty frame

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder h, int position) {
        PostItem post = posts.get(position);

        Glide.with(h.itemView.getContext())
                .load(post.getImageUri())          // handles null automatically → shows placeholder
                .placeholder(R.drawable.post_frame)
                .error(R.drawable.post_frame)      // also shows placeholder if the URL fails to load
                .into(h.ivPostImage);

        // address / location label
        String address = post.getAddress();
        if (address != null && !address.isEmpty()) {
            h.tvAddress.setVisibility(View.VISIBLE);
            h.tvAddress.setText(address);
        } else {
            h.tvAddress.setVisibility(View.GONE);
        }

        // Title
        String title = post.getTitle();

        if (title != null && !title.isEmpty()) {
            h.tvTitle.setVisibility(View.VISIBLE);
            h.tvTitle.setText(title);
        } else {
            h.tvTitle.setVisibility(View.GONE);
        }

        // Likes
        h.tvLikeCount.setText(formatCount(post.getLikes()));

        // Tags chips
//        h.chipGroupTags.removeAllViews();
//        if (post.getTags() != null) {
//            for (String tag : post.getTags()) {
//                Chip chip = new Chip(h.chipGroupTags.getContext());
//                chip.setText("#" + tag);
//                chip.setClickable(false);
//                chip.setCheckable(false);
//                h.chipGroupTags.addView(chip);
//            }
//        }

        //  listeners
        h.itemView.setOnClickListener(v -> listener.onPostClick(post));
        h.btnLike.setSelected(post.isLikedByMe());
        h.btnLike.invalidate();
        updateLikeButton(h.btnLike, post.isLikedByMe());
        h.btnLike.setOnClickListener(v -> listener.onLikeClick(post, h.getAdapterPosition()));
    }

    @Override
    public int getItemCount() { return posts.size(); }

    /** Called from the fragment after a like update to refresh just the count */
    public void updateLikes(int position, long newCount) {
        posts.get(position).setLikes(newCount);
        notifyItemChanged(position, "likes");
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder h, int position,
                                 @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && "likes".equals(payloads.get(0))) {
            PostItem post = posts.get(position);
            h.tvLikeCount.setText(formatCount(post.getLikes()));
            updateLikeButton(h.btnLike, post.isLikedByMe());
        } else {
            super.onBindViewHolder(h, position, payloads);
        }
    }

    private void updateLikeButton(ImageView btn, boolean liked) {
        btn.setImageResource(liked ? R.drawable.ic_heart_fill : R.drawable.ic_heart);
    }

    private String formatCount(long n) {
        if (n >= 1_000_000) return String.format(Locale.US, "%.1fM", n / 1_000_000.0);
        if (n >= 1_000)     return String.format(Locale.US, "%.1fk", n / 1_000.0);
        return NumberFormat.getInstance().format(n);
    }

    // ---- ViewHolder ----

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView  ivPostImage;
        TextView   tvAddress;
        TextView tvTitle;
        ImageView  btnLike;
        TextView   tvLikeCount;
        //ChipGroup  chipGroupTags;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPostImage   = itemView.findViewById(R.id.ivPostImage);
            tvAddress     = itemView.findViewById(R.id.tvAddress);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            btnLike       = itemView.findViewById(R.id.btnLike);
            tvLikeCount   = itemView.findViewById(R.id.tvLikeCount);
            //chipGroupTags = itemView.findViewById(R.id.chipGroupTags);
        }
    }
}