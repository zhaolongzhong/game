package com.example.game;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    @Nullable
    private StompClient stompClient;
    @Nullable
    private Disposable disposableLifecycle;
    private Map<String, Disposable> disposableMap = new HashMap<>();

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
            subscribeAndSendGreeting();
            subscribeAndSendChat();
        }
    }

    private void initWebSocket() {
        Log.d(TAG, "initWebSocket");
        if (stompClient != null) {
            Log.d(TAG, "stompClient has already been initialized.");
            return;
        }

        // Make sure you connect localhost properly, https://stackoverflow.com/a/4779992
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://192.168.86.148:8080/ws/websocket");
        stompClient.withClientHeartbeat(3000); // Set heart beat 3s
        stompClient.connect();

        if (disposableLifecycle != null) {
            disposableLifecycle.dispose();
        }

        disposableLifecycle = stompClient.lifecycle().subscribe(lifecycleEvent -> {
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

        for (Disposable disposable : disposableMap.values()) {
            disposable.dispose();
        }

        disposableMap.clear();
    }

    private void subscribeAndSendGreeting() {
        if (stompClient == null) {
            return;
        }

        Log.d(TAG, "subscribeAndSendGreeting");

        if (!disposableMap.containsKey("greetingTopicDisposable")) {
            Disposable greetingTopicDisposable = stompClient.topic("/topic/greeting")
                    .doOnError(throwable -> System.out.println("inx onError:" + throwable.getMessage()))
                    .subscribe(topicMessage -> {
                        Log.d(TAG, "greetingTopicDisposable topicMessage: " + topicMessage);
                    });
            disposableMap.put("greetingTopicDisposable", greetingTopicDisposable);
        }


        String message = "Android";
        stompClient.send("/topic/greeting", message)
                .doOnError(throwable -> Log.e(TAG, "greetingSendDisposable error:" + throwable.getMessage()))
                .subscribe()
                .dispose();

    }

    private void subscribeAndSendChat() {
        if (stompClient == null) {
            return;
        }

        Log.d(TAG, "subscribeAndSendChat");

        if (!disposableMap.containsKey("chatTopicDisposable")) {
            Disposable chatTopicDisposable = stompClient.topic("/topic/chat")
                    .doOnError(throwable -> System.out.println("chatTopicDisposable error:" + throwable.getMessage()))
                    .subscribe(topicMessage -> Log.d(TAG, "chatTopicDisposable topicMessage: " + topicMessage));
            disposableMap.put("chatTopicDisposable", chatTopicDisposable);
        }

        stompClient.send("/topic/chat", "{\"userId\": \"id_123\", \"message\": \"hello, i'm id_123\"}")
                .doOnError(throwable -> Log.e(TAG, "chatSendDisposable error: " + throwable.getMessage()))
                .subscribe()
                .dispose();
    }
}
