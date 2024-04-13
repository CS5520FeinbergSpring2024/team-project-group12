package edu.northeastern.group12_finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        if (commentList != null && position < commentList.size()) {
            Comment comment = commentList.get(position);
            holder.usernameTextView.setText(comment.getUsername());
            holder.timestampTextView.setText(comment.getTimestampDifference(comment.getTimestamp()));
            holder.commentTextView.setText(comment.getText());
        } else {
            holder.commentTextView.setText("No comments. Add a comment below!");
            holder.usernameTextView.setVisibility(View.GONE);
            holder.timestampTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    public void updateComments(List<Comment> newCommentList) {
        commentList.clear();
        commentList.addAll(newCommentList);
        notifyDataSetChanged();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView timestampTextView;
        TextView commentTextView;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.comment_username);
            timestampTextView = itemView.findViewById(R.id.comment_timestamp);
            commentTextView = itemView.findViewById(R.id.comment_text);
        }
    }
}