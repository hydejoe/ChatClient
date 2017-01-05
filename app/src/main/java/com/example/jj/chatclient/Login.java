package com.example.jj.chatclient;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private Button button_connect;
    public ProgressDialog dialog_waiting;
    private EditText edit_username;
    private EditText edit_address;
    private EditText edit_port;
    public String username;
    public String address;
    public String port;
    public Thread thread;
    public int connection_status;
    Handler handler;
    MyApp myapp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myapp = (MyApp) getApplication();

        button_connect = (Button) findViewById(R.id.button_connect);
        button_connect.setOnClickListener(this);

        edit_username = (EditText) findViewById(R.id.edit_username);
        edit_address = (EditText) findViewById(R.id.edit_address);
        edit_port = (EditText) findViewById(R.id.edit_port);

        dialog_waiting = myapp.create_dialog(this);
        dialog_waiting.setTitle("Please Wait");

        handler = myapp.create_login_handler(Login.this, myapp);
        if (myapp.get_socket_status() == myapp.SOCKET_BUSY) {
            dialog_waiting.show();
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_connect) {
            load_and_save_data();
            if (check_legal()) {
                connect_and_login();
            } else {
                error_invalid_input();
            }
        }
    }


    private void error_invalid_input() {
        Toast.makeText(getApplicationContext(), getString(R.string.string_input_invalid), Toast.LENGTH_SHORT).show();
    }

    private void load_and_save_data() {
        username = edit_username.getText().toString();
        address = edit_address.getText().toString();
        port = edit_port.getText().toString();
        myapp.save_data(username, address, port);
    }

    private boolean check_legal() {
        int is_legal = 0;
        if (Objects.equals(username, "")) {
            edit_username.setHintTextColor(Color.rgb(255, 0, 0));
            edit_username.setHint("Please enter username");
            is_legal += 1;
        }
        if (Objects.equals(address, "")) {
            edit_address.setHintTextColor(Color.rgb(255, 0, 0));
            edit_address.setHint("Please enter address");
            is_legal += 1;
        }
        if (Objects.equals(port, "")) {
            edit_port.setHintTextColor(Color.rgb(255, 0, 0));
            is_legal += 1;
        }
        return is_legal == 0;
    }

    private void connect_and_login() {
        //dialog_waiting.setMessage("Connecting to "+address+":"+port);
        dialog_waiting.show();
        dialog_waiting.setMessage("Trying to login as " + username);
        connect_to_server();
    }

    private void connect_to_server() {
        connection_status = 0;
        thread = myapp.create_login_thread();
        thread.start();
    }

    public void to_chat_activity() {
        Intent intentChat = new Intent(Login.this, Chat.class);
        startActivity(intentChat);
    }


}
