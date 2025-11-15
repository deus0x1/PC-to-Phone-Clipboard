package com.infoshare.client_app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.*;
import android.widget.ScrollView;

public class MainActivity extends Activity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textViewData);

        new Thread(() -> {
            String ip_final = null;
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Socket socket = null;
            // Scan subnet for the server
            for (int i = 1; i < 255; i++) {
                String ip = "192.168.209." + i;
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, 1337), 200);

                    runOnUiThread(() -> textView.append("Connected to " + ip + "\n"));
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    out.println("/isthisinfoshare");

                    Future<String> future = executor.submit(in::readLine);
                    String response;

                    try {
                        response = future.get(1, TimeUnit.SECONDS);
                    } catch (TimeoutException | ExecutionException | InterruptedException e) {
                        response = null;
                        future.cancel(true);
                    }

                    String finalResponse = response;
                    runOnUiThread(() -> textView.append("Response from " + ip + ": " + finalResponse + "\n"));

                    if ("/thisisinfoshare".equals(response)) {
                        ip_final = ip;
                        break;
                    } else {
                        socket.close();
                    }
                } catch (IOException ignored) {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            executor.shutdownNow();

            if (!socket.isClosed()) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String data;

                    while ((data = in.readLine()) != null) {
                        String finalData = data;
                        runOnUiThread(() -> {
                            textView.append(finalData + "\n");
                            ScrollView scrollView = findViewById(R.id.scrollView);
                            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                        });
                    }

                    socket.close();
                } catch (IOException e) {
                    runOnUiThread(() -> textView.append("Error reading data.\n"));
                    e.printStackTrace();
                }
            } else {
                runOnUiThread(() -> textView.append("No server found on subnet.\n"));
            }
        }).start();
    }
}

