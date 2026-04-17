package com.tfg.tcgserver.persistence.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Repository;

import java.util.concurrent.CompletableFuture;

@Repository
public class FirebaseRealtimeDatabaseRepository {

    private final FirebaseDatabase firebaseDatabase;

    public FirebaseRealtimeDatabaseRepository(FirebaseDatabase firebaseDatabase) {
        this.firebaseDatabase = firebaseDatabase;
    }

    public DatabaseReference reference(String path) {
        return firebaseDatabase.getReference(path);
    }

    public CompletableFuture<Void> save(String path, Object value) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        reference(path).setValue(value, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                future.completeExceptionally(databaseError.toException());
                return;
            }

            future.complete(null);
        });

        return future;
    }

    public <T> CompletableFuture<T> find(String path, Class<T> valueType) {
        CompletableFuture<T> future = new CompletableFuture<>();

        reference(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                future.complete(snapshot.getValue(valueType));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    public <T> CompletableFuture<T> find(String path, GenericTypeIndicator<T> valueType) {
        CompletableFuture<T> future = new CompletableFuture<>();

        reference(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                future.complete(snapshot.getValue(valueType));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    public CompletableFuture<Void> delete(String path) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        reference(path).removeValue((databaseError, databaseReference) -> {
            if (databaseError != null) {
                future.completeExceptionally(databaseError.toException());
                return;
            }

            future.complete(null);
        });

        return future;
    }
}
