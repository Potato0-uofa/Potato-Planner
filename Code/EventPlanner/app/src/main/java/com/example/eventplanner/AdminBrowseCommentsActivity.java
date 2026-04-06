package com.example.eventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin screen that loads every comment from every event in a single list.
 * Admins can delete any comment regardless of who posted it.
 *
 * Firestore traversal:
 *   events/{eventId}/comments/{commentId}
 */
public class AdminBrowseCommentsActivity extends AppCompatActivity {

    /** RecyclerView used to display the flat list of comments from all events. */
    private RecyclerView recyclerView;

    /** Adapter that binds comment data to the RecyclerView. */
    private AdminCommentAdapter adapter;

    /** In-memory list of comment items from all events. */
    private final List<AdminCommentItem> commentItems = new ArrayList<>();

    /** Firestore database instance for querying events and comments. */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Repository used to delete comments. */
    private final CommentRepository commentRepository = new CommentRepository();

    /**
     * Initializes the activity, sets up the RecyclerView, and loads all comments.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_comments);

        findViewById(R.id.exit_button_admin_comments).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.admin_comments_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminCommentAdapter(commentItems, (eventId, commentId) -> {
            commentRepository.deleteComment(eventId, commentId,
                    new CommentRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(AdminBrowseCommentsActivity.this,
                                    "Comment deleted.", Toast.LENGTH_SHORT).show();
                            loadAllComments(); // refresh the list
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(AdminBrowseCommentsActivity.this,
                                    "Failed to delete: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        recyclerView.setAdapter(adapter);
        loadAllComments();
    }

    /**
     * Fetches every event, then for each event fetches its comments subcollection.
     * All results are flattened into a single list and displayed.
     */
    private void loadAllComments() {
        commentItems.clear();
        adapter.notifyDataSetChanged();

        db.collection("events").get()
                .addOnSuccessListener(eventSnapshots -> {
                    if (eventSnapshots.isEmpty()) return;

                    final int[] remaining = {eventSnapshots.size()};

                    for (QueryDocumentSnapshot eventDoc : eventSnapshots) {
                        String eventId   = eventDoc.getId();
                        String eventName = eventDoc.getString("name");

                        db.collection("events")
                                .document(eventId)
                                .collection("comments")
                                .orderBy("createdAt",
                                        com.google.firebase.firestore.Query.Direction.ASCENDING)
                                .get()
                                .addOnSuccessListener(commentSnapshots -> {
                                    for (QueryDocumentSnapshot commentDoc : commentSnapshots) {
                                        Comment comment = commentDoc.toObject(Comment.class);
                                        if (comment != null) {
                                            commentItems.add(new AdminCommentItem(
                                                    eventId, eventName, comment));
                                        }
                                    }
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load comments: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }


    /**
     * Wraps a Comment with the eventId and eventName it belongs to,
     * so the adapter can display context and perform deletes.
     */
    static class AdminCommentItem {
        final String eventId;
        final String eventName;
        final Comment comment;

        /**
         * Constructs an AdminCommentItem.
         *
         * @param eventId   the Firestore event ID
         * @param eventName the display name of the event
         * @param comment   the comment object
         */
        AdminCommentItem(String eventId, String eventName, Comment comment) {
            this.eventId   = eventId;
            this.eventName = eventName != null ? eventName : eventId;
            this.comment   = comment;
        }
    }


    /**
     * Listener interface for admin comment deletion actions.
     */
    interface OnAdminDeleteListener {
        /**
         * Called when the admin clicks delete on a comment.
         *
         * @param eventId   the event the comment belongs to
         * @param commentId the comment to delete
         */
        void onDelete(String eventId, String commentId);
    }

    static class AdminCommentAdapter
            extends RecyclerView.Adapter<AdminCommentAdapter.ViewHolder> {

        private final List<AdminCommentItem> items;
        private final OnAdminDeleteListener deleteListener;

        AdminCommentAdapter(List<AdminCommentItem> items,
                            OnAdminDeleteListener deleteListener) {
            this.items          = items;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AdminCommentItem item = items.get(position);

            // Show event name above author so admin knows which event the comment is from
            holder.authorText.setText(
                    "[" + item.eventName + "] " + item.comment.getAuthorName());
            holder.bodyText.setText(item.comment.getText());

            // Admin can always delete — no ownership check
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v ->
                    deleteListener.onDelete(item.eventId, item.comment.getCommentId()));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView authorText;
            TextView bodyText;
            Button   deleteButton;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                authorText   = itemView.findViewById(R.id.comment_author);
                bodyText     = itemView.findViewById(R.id.comment_text);
                deleteButton = itemView.findViewById(R.id.delete_comment_button);
            }
        }
    }
}