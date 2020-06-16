package com.example.game;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    @Nullable
    private StompClient stompClient;
    @Nullable
    private Disposable disposableLifecyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView helloWorldTextView = findViewById(R.id.hello_world_text_view);
        helloWorldTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helloWorldTextViewOnClick();
            }
        });
    }

    @Override
    protected void onStop() {
        closeConnection();
        super.onStop();
    }

    private void helloWorldTextViewOnClick() {
        Log.d(TAG, "onClick");
        if (stompClient == null) {
            initWebSocket();
        } else if (stompClient.isConnected()) {
            Log.d(TAG, "onClick stompClient is connected");
        }
    }

    private void initWebSocket() {
        Log.d(TAG, "initWebsocket");

        // Make sure you connect localhost properly, https://stackoverflow.com/a/4779992
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://192.168.86.148:8080/ws/websocket");
        stompClient.withClientHeartbeat(3000); // Set heart beat 3s
        stompClient.connect();

        if (disposableLifecyle != null) {
            disposableLifecyle.dispose();
        }
        disposableLifecyle = stompClient.lifecycle().subscribe(lifecycleEvent -> {
            Log.d(TAG, "stompClient lifecycleEvent: " + lifecycleEvent.getType() + ", message: " + lifecycleEvent.getMessage());
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d(TAG, "stompClient connection opened");
                    break;

                case ERROR:
                    Log.e(TAG, "Error", lifecycleEvent.getException());
                    break;

                case CLOSED:
                    Log.d(TAG, "stompClient connection closed");
                    break;
            }
        });
    }

    private void closeConnection() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }
}