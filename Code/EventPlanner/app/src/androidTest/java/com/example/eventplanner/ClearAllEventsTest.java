package com.example.eventplanner;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One-time instrumented test to wipe the "events" collection.
 * Run from Android Studio: right-click this file → Run.
 * DELETE THIS FILE after use.
 */
@RunWith(AndroidJUnit4.class)
public class ClearAllEventsTest {

    private static final String TAG = "ClearAllEvents";

    @Test
    public void deleteAllEvents() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AtomicInteger totalDeleted = new AtomicInteger(0);
        boolean moreToDelete = true;

        while (moreToDelete) {
            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] hasMore = {false};

            db.collection("events")
                    .limit(500)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.isEmpty()) {
                            latch.countDown();
                            return;
                        }

                        hasMore[0] = true;
                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot doc : snapshot) {
                            batch.delete(doc.getReference());
                        }

                        int batchSize = snapshot.size();
                        batch.commit().addOnSuccessListener(aVoid -> {
                            totalDeleted.addAndGet(batchSize);
                            Log.d(TAG, "Deleted batch of " + batchSize + " (total: " + totalDeleted.get() + ")");
                            latch.countDown();
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Batch commit failed", e);
                            latch.countDown();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Query failed", e);
                        latch.countDown();
                    });

            latch.await(30, TimeUnit.SECONDS);
            moreToDelete = hasMore[0];
        }

        Log.d(TAG, "DONE. Total events deleted: " + totalDeleted.get());
    }
}
