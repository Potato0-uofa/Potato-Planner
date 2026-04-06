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

    /** Repository for comment CRUD operations. */
    private CommentRepository commentRepository;

    /** Real-time Firestore listener for the comments subcollection. */
    private ListenerRegistration commentsListener;

    /** RecyclerView displaying the comment list. */
    private RecyclerView recyclerView;

    /** Adapter binding comment data to the RecyclerView. */
    private CommentAdapter adapter;

    /** In-memory list of comments currently displayed. */
    private final List<Comment> commentList = new ArrayList<>();

    /** Input field for composing a new comment. */
    private EditText commentInput;

    /** The Firestore event ID whose comments are being viewed. */
    private String eventId;

    /** The current user's device ID, used for authorship checks. */
    private String deviceId;

    /**
     * Initializes the activity, sets up the comment list and input field,
     * and starts listening for real-time comment updates.
     *
     * @param savedInstanceState previously saved activity state, if any
     */
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

        new EventRepository().fetchEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Events event) {
                adapter.setOrganizerId(event.getOrganizerId());
                adapter.setCoOrganizerIds(event.getCoOrganizerIds());
            }
            @Override
            public void onFailure(Exception e) { /* organizer check just won't work, silent fail */ }
        });

        startListeningToComments();
    }

    /**
     * Removes the Firestore comments listener to avoid memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentsListener != null) commentsListener.remove();
    }

    /**
     * Attaches a real-time listener to the comments subcollection and updates the UI
     * whenever comments are added, modified, or removed.
     */
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

    /** RecyclerView adapter for displaying comments with delete support for the author and organizer. */
    static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

        private final List<Comment> comments;
        private final String currentDeviceId;
        private final OnDeleteClickListener deleteListener;
        private String organizerId = "";
        private List<String> coOrganizerIds = new ArrayList<>();

        public void setOrganizerId(String organizerId) {
            this.organizerId = organizerId;
            notifyDataSetChanged();
        }

        public void setCoOrganizerIds(List<String> coOrganizerIds) {
            this.coOrganizerIds = coOrganizerIds != null ? coOrganizerIds : new ArrayList<>();
            notifyDataSetChanged();
        }

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

            //Only shows delete button for organizer
            if (currentDeviceId.equals(comment.getDeviceId())||currentDeviceId.equals(organizerId)||
                    coOrganizerIds.contains(currentDeviceId)) {
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