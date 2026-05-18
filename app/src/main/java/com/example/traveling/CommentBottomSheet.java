package com.example.traveling;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_POST_ID = "post_id";

    private String postId;
    private MainActivity mainActivity;

    private RecyclerView rvComments;
    private EditText etComment;
    private ImageView btnSend;

    private final List<CommentItem> comments = new ArrayList<>();
    private CommentAdapter adapter;

    // callback so PostdetailFragment can update its comment count badge
    public interface OnCommentPostedListener {
        void onCommentPosted(int newCount);
    }
    private OnCommentPostedListener commentListener;

    public static CommentBottomSheet newInstance(String postId) {
        CommentBottomSheet sheet = new CommentBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_POST_ID, postId);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnCommentPostedListener(OnCommentPostedListener l) {
        this.commentListener = l;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        if (getArguments() != null) postId = getArguments().getString(ARG_POST_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragments_comment_bottom_sheet,
                container, false);

        rvComments = v.findViewById(R.id.rvComments);
        etComment  = v.findViewById(R.id.etComment);
        btnSend    = v.findViewById(R.id.btnSendComment);

        adapter = new CommentAdapter(comments);
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvComments.setAdapter(adapter);

        loadComments();

        btnSend.setOnClickListener(btn -> postComment());

        return v;
    }

    /*========================= LOAD =========================*/

    private void loadComments() {
        mainActivity.db.collection("comments")
                .whereEqualTo("postId", postId)
                .get()
                .addOnSuccessListener(qs -> {
                    comments.clear();
                    for (DocumentSnapshot doc : qs) {
                        CommentItem item = new CommentItem();
                        item.setId(doc.getId());
                        item.setUserId(doc.getString("userId"));
                        item.setText(doc.getString("text"));
                        Timestamp ts = doc.getTimestamp("timestamp");
                        if (ts != null) item.setTimestampMillis(ts.toDate().getTime());
                        comments.add(item);
                    }
                    adapter.notifyDataSetChanged();
                    // Resolve usernames + photos after list is built
                    for (int i = 0; i < comments.size(); i++) {
                        resolveCommentAuthor(comments.get(i), i);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                getString(R.string.comment_load_failed),
                                Toast.LENGTH_SHORT).show());
    }

    /*========================= POST =========================*/

    private void postComment() {
        String uid = mainActivity.mAuth.getCurrentUser() != null
                ? mainActivity.mAuth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(getContext(), getString(R.string.comment_login_required),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etComment.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        btnSend.setEnabled(false);

        Map<String, Object> comment = new HashMap<>();
        comment.put("postId",    postId);
        comment.put("userId",    uid);
        comment.put("text",      text);
        comment.put("timestamp", Timestamp.now());

        mainActivity.db.collection("comments").document()
                .set(comment)
                .addOnSuccessListener(unused -> {
                    etComment.setText("");
                    btnSend.setEnabled(true);
                    // Add optimistically to the list
                    CommentItem item = new CommentItem();
                    item.setUserId(uid);
                    item.setText(text);
                    item.setTimestampMillis(System.currentTimeMillis());
                    comments.add(item);
                    int pos = comments.size() - 1;
                    adapter.notifyItemInserted(pos);
                    rvComments.scrollToPosition(pos);
                    resolveCommentAuthor(item, pos);
                    // Notify parent of new count
                    if (commentListener != null) {
                        commentListener.onCommentPosted(comments.size());
                    }
                })
                .addOnFailureListener(e -> {
                    btnSend.setEnabled(true);
                    Toast.makeText(getContext(), getString(R.string.comment_send_failed), Toast.LENGTH_SHORT).show();
                });
    }

    /*========================= RESOLVE AUTHOR =========================*/

    private void resolveCommentAuthor(CommentItem item, int position) {
        if (item.getUserId() == null) return;
        mainActivity.db.collection("users").document(item.getUserId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        item.setAuthorName(doc.getString("username"));
                        item.setAuthorPhoto(doc.getString("photoUrl"));
                    }
                    adapter.notifyItemChanged(position);
                });
    }

    /*========================= INNER MODEL =========================*/

    public static class CommentItem {
        private String id, userId, text, authorName, authorPhoto;
        private long timestampMillis;

        public String getId()                        { return id; }
        public void setId(String id)                 { this.id = id; }
        public String getUserId()                    { return userId; }
        public void setUserId(String u)              { this.userId = u; }
        public String getText()                      { return text; }
        public void setText(String t)                { this.text = t; }
        public String getAuthorName()                { return authorName; }
        public void setAuthorName(String n)          { this.authorName = n; }
        public String getAuthorPhoto()               { return authorPhoto; }
        public void setAuthorPhoto(String p)         { this.authorPhoto = p; }
        public long getTimestampMillis()             { return timestampMillis; }
        public void setTimestampMillis(long ms)      { this.timestampMillis = ms; }
    }

    /*========================= INNER ADAPTER =========================*/

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.VH> {

        private final List<CommentItem> data;
        CommentAdapter(List<CommentItem> data) { this.data = data; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            CommentItem item = data.get(position);

            h.tvAuthor.setText(item.getAuthorName() != null
                    ? item.getAuthorName() : "…");
            h.tvText.setText(item.getText());

            if (item.getTimestampMillis() > 0) {
                h.tvDate.setText(new SimpleDateFormat("MMM dd, HH:mm",
                        Locale.getDefault())
                        .format(new Date(item.getTimestampMillis())));
            }

            if (item.getAuthorPhoto() != null && !item.getAuthorPhoto().isEmpty()) {
                Glide.with(CommentBottomSheet.this)
                        .load(item.getAuthorPhoto())
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.post_frame)
                        .into(h.ivPhoto);
            } else {
                h.ivPhoto.setImageResource(R.drawable.post_frame);
            }
        }

        @Override
        public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivPhoto;
            TextView tvAuthor, tvText, tvDate;
            VH(@NonNull View v) {
                super(v);
                ivPhoto   = v.findViewById(R.id.ivCommentAuthorPhoto);
                tvAuthor  = v.findViewById(R.id.tvCommentAuthor);
                tvText    = v.findViewById(R.id.tvCommentText);
                tvDate    = v.findViewById(R.id.tvCommentDate);
            }
        }
    }
}