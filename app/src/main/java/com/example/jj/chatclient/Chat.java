package com.example.jj.chatclient;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Chat extends AppCompatActivity implements View.OnClickListener {
    MyApp myapp;
    TextView title;
    Button button_logout;
    Button button_sendmsg;
    TextView message;
    EditText edit_msg;
    Handler handler;
    Thread thread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myapp = (MyApp) getApplication();

        title = (TextView) findViewById(R.id.chat_title);
        title.setText(String.format("Welcome, %s", myapp.get_username()));

        button_logout = (Button) findViewById(R.id.button_logout);
        button_logout.setOnClickListener(this);
        button_sendmsg = (Button) findViewById(R.id.button_sendmsg);
        button_sendmsg.setOnClickListener(this);

        message = (TextView) findViewById(R.id.chat_msg);
        edit_msg = (EditText) findViewById(R.id.edit_message);

        handler = myapp.create_chat_handler(Chat.this, myapp);
        thread = myapp.create_chat_thread();
        if (!thread.isAlive()) {
            thread.start();
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_logout:
                logout();
                break;
            case R.id.button_sendmsg:
                myapp.send_message(edit_msg.getText().toString());
                edit_msg.setText("");
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            // 按下BACK，同时没有重复
            if (event.getRepeatCount() == 0) {
                return true;
            } else logout();
        }
        return super.onKeyDown(keycode, event);
    }


    private void logout() {
        myapp.chat_logout();
        this.finish();
    }
}
