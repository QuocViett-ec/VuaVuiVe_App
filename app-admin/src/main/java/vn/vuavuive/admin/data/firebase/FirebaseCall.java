package vn.vuavuive.admin.data.firebase;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseCall<T> implements Call<T> {
    private final T data;
    private final Throwable error;

    public FirebaseCall(T data) {
        this.data = data;
        this.error = null;
    }

    public FirebaseCall(Throwable error) {
        this.data = null;
        this.error = error;
    }

    @NonNull
    @Override
    public Response<T> execute() throws IOException {
        if (error != null) {
            throw new IOException(error);
        }
        return Response.success(data);
    }

    @Override
    public void enqueue(@NonNull Callback<T> callback) {
        if (error != null) {
            callback.onFailure(this, error);
        } else {
            callback.onResponse(this, Response.success(data));
        }
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {}

    @Override
    public boolean isCanceled() {
        return false;
    }

    @NonNull
    @Override
    public Call<T> clone() {
        return this;
    }

    @NonNull
    @Override
    public Request request() {
        return new Request.Builder().url("https://firebase.google.com").build();
    }

    @NonNull
    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }
}
