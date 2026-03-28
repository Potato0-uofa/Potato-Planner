package com.example.eventplanner;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the comment section for a specific event.
 * Allows any user to post a comment and delete their own comments.
 * Comments persist in Firestore at: events/{eventId}/comments/{commentId}
 *
 * Launch with:
 *   Intent intent = new Intent(this, CommentsActivity.class);
 *   intent.putExtra("eventId", eventId);
 *   startActivity(intent);
 */
public class CommentsActivity extends AppCompatActivity {

    private CommentRepository commentRepository;
    private ListenerRegistration commentsListener;

    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private final List<Comment> commentList = new ArrayList<>();

    private EditText commentInput;
    private String eventId;
    private String deviceId;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentRepository = new CommentRepository();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        eventId  = getIntent().getStringExtra("eventId");

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "No event ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ── Bind views ────────────────────────────────────────────────────────
        commentInput = findViewById(R.id.comment_input);
        recyclerView = findViewById(R.id.comments_recycler_view);

        findViewById(R.id.exit_button_comments).setOnClickListener(v -> finish());

        // ── RecyclerView setup ────────────────────────────────────────────────
        adapter = new CommentAdapter(commentList, deviceId, (comment) -> {
            // Delete callback — only called when user taps Delete on their own comment
            commentRepository.deleteComment(eventId, comment.getCommentId(),
                    new CommentRepository.SimpleCallback() {
                        @Override public void onSuccess() {
                            Toast.makeText(CommentsActivity.this,
                                    "Comment deleted.", Toast.LENGTH_SHORT).show();
                        }
                        @Override public void onFailure(Exception e) {
                            Toast.makeText(CommentsActivity.this,
                                    "Failed to delete: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // ── Post button ───────────────────────────────────────────────────────
        findViewById(R.id.post_comment_button).setOnClickListener(v -> {
            String text = commentInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Comment cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use deviceId as author name for now — swap for user.getName() if available
            new UserRepository().getUserByDeviceId(deviceId, new UserRepository.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    String authorName = (user.getName() != null && !user.getName().isEmpty())
                            ? user.getName()
                            : deviceId; // fallback if name not set

                    commentRepository.addComment(eventId, deviceId, authorName, text,
                            new CommentRepository.SimpleCallback() {
                                @Override public void onSuccess() {
                                    commentInput.setText("");
                                }
                                @Override public void onFailure(Exception e) {
                                    Toast.makeText(CommentsActivity.this,
                                            "Failed to post: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }

                @Override
                public void onFailure(Exception e) {
                    // Fallback — post with deviceId if user fetch fails
                    commentRepository.addComment(eventId, deviceId, deviceId, text,
                            new CommentRepository.SimpleCallback() {
                                @Override public void onSuccess() { commentInput.setText(""); }
                                @Override public void onFailure(Exception ex) {
                                    Toast.makeText(CommentsActivity.this,
                                            "Failed to post: " + ex.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        });

        // ── Start listening ───────────────────────────────────────────────────
        startListeningToComments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsListener != null) commentsListener.remove();
    }

    private void startListeningToComments() {
        commentsListener = commentRepository.listenToComments(eventId,
                new CommentRepository.CommentsCallback() {
                    @Override
                    public void onUpdate(List<Comment> comments) {
                        commentList.clear();
                        commentList.addAll(comments);
                        adapter.notifyDataSetChanged();
                        // Scroll to latest comment
                        if (!commentList.isEmpty()) {
                            recyclerView.scrollToPosition(commentList.size() - 1);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(CommentsActivity.this,
                                "Failed to load comments: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RecyclerView Adapter (inner class — keeps everything in one file)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Callback interface for the delete button tap.
     */
    interface OnDeleteClickListener {
        void onDelete(Comment comment);
    }

    static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

        private final List<Comment> comments;
        private final String currentDeviceId;
        private final OnDeleteClickListener deleteListener;

        CommentAdapter(List<Comment> comments,
                       String currentDeviceId,
                       OnDeleteClickListener deleteListener) {
            this.comments        = comments;
            this.currentDeviceId = currentDeviceId;
            this.deleteListener  = deleteListener;
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
            Comment comment = comments.get(position);
            holder.authorText.setText(comment.getAuthorName());
            holder.bodyText.setText(comment.getText());

            // Only show Delete button for the comment's author
            if (currentDeviceId.equals(comment.getDeviceId())) {
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setOnClickListener(v -> deleteListener.onDelete(comment));
            } else {
                holder.deleteButton.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return comments.size(); }

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