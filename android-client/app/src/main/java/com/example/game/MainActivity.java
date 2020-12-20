package com.example.game;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private TextView statusTextView;

    @Nullable
    private StompClient stompClient;
    @Nullable
    private Disposable disposableLifecycle;
    private Map<String, Disposable> disposableMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button connectWebSocketButton = findViewById(R.id.connect_websocket_button);
        Button sendGreetButton = findViewById(R.id.send_greeting_button);
        Button sendMessageButton = findViewById(R.id.send_message_button);
        Button disconnectButton = findViewById(R.id.disconnect_button);
        statusTextView = findViewById(R.id.status_text_view);


        connectWebSocketButton.setOnClickListener((View v) -> {
            Log.d(TAG, "connectWebSocketButton clicked");
            if (stompClient == null) {
                initWebSocket();
            } else if (stompClient.isConnected()) {
                Log.d(TAG, "onClick stompClient is connected");
                updateStatus("Already connected");
            }
        });

        sendGreetButton.setOnClickListener((View v) -> {
            subscribeAndSendGreeting();
        });

        sendMessageButton.setOnClickListener((View v) -> {
            subscribeAndSendMessage();
        });

        disconnectButton.setOnClickListener((View v) -> {
            closeConnection();
        });
    }

    @Override
    protected void onStop() {
        closeConnection();
        super.onStop();
    }

    private void initWebSocket() {
        Log.d(TAG, "initWebSocket");
        if (stompClient != null) {
            Log.d(TAG, "stompClient has already been initialized.");
            return;
        }

        // Make sure you connect localhost properly, https://stackoverflow.com/a/4779992
//        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://192.168.86.148:8080/ws/websocket");

        // If you're using emulator, you need to use "10.0.2.2" instead of "localhost" or "127.0.0.1".
        // see https://developer.android.com/studio/run/emulator-networking
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/ws/websocket");
        stompClient.withClientHeartbeat(5000); // Set heart beat 5s
        stompClient.connect();

        if (disposableLifecycle != null) {
            disposableLifecycle.dispose();
        }

        disposableLifecycle = stompClient.lifecycle().subscribe(lifecycleEvent -> {
            Log.d(TAG, "stompClient lifecycleEvent: " + lifecycleEvent.getType() + ", message: " + lifecycleEvent.getMessage());
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    updateStatus("WebSocket connection opened");
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

    /**
     * A helper method to update status on UI thread
     * @param message
     */
    private void updateStatus(String message) {
        runOnUiThread(() -> { statusTextView.setText(message); });
    }

    private void closeConnection() {
        if (stompClient != null) {
            stompClient.disconnect();
            stompClient = null;
            updateStatus("WebSocket disconnected");
        }

        for (Disposable disposable : disposableMap.values()) {
            disposable.dispose();
        }

        disposableMap.clear();
    }

    private void subscribeAndSendGreeting() {
        if (stompClient == null) {
            return;
        }

        subscribeGreeting();
        sendGreeting();
    }

    private void sendGreeting() {
        Log.d(TAG, "sendGreetings");
        String message = "A greeting from an Android user.";
        stompClient.send("/topic/greeting", message)
                .doOnError(throwable -> Log.e(TAG, "greetingSendDisposable error:" + throwable.getMessage()))
                .subscribe()
                .dispose();
    }

    /**
     * Subscribe /topic/greeting, and receive message whenever there's one
     */
    private void subscribeGreeting() {
        if (!disposableMap.containsKey("greetingTopicDisposable")) {
            Log.d(TAG, "subscribeGreeting");
            Disposable greetingTopicDisposable = stompClient.topic("/topic/greeting")
                    .doOnError(throwable -> updateStatus("inx onError:" + throwable.getMessage()))
                    .subscribe(topicMessage -> {
                        Log.d(TAG, "greetingTopicDisposable topicMessage: " + topicMessage);
                        updateStatus("receive greeting: " + topicMessage);
                    });
            disposableMap.put("greetingTopicDisposable", greetingTopicDisposable);
        }
    }

    private void subscribeAndSendMessage() {
        if (stompClient == null) {
            return;
        }

        subscribeMessage();
        sendMessage();
    }

    private void sendMessage() {
        Log.d(TAG, "sendMessage");

        String message = "A message from an Android user at " + new Date();
        stompClient.send("/topic/chat", "{\"userId\": \"id_android_user\", \"message\": \"" + message + "\"}")
                .doOnError(throwable -> Log.e(TAG, "chatSendDisposable error: " + throwable.getMessage()))
                .subscribe()
                .dispose();
    }

    /**
     * Subscribe /topic/chat, and receive message whenever there's one
     */
    private void subscribeMessage() {
        if (!disposableMap.containsKey("chatTopicDisposable")) {
            Log.d(TAG, "subscribeMessage");
            Disposable chatTopicDisposable = stompClient.topic("/topic/chat")
                    .doOnError(throwable -> updateStatus("chatTopicDisposable error:" + throwable.getMessage()))
                    .subscribe(topicMessage -> {
                        Log.d(TAG, "chatTopicDisposable topicMessage: " + topicMessage);
                        updateStatus("receive message: " + topicMessage);
                    });
            disposableMap.put("chatTopicDisposable", chatTopicDisposable);
        }
    }
}
