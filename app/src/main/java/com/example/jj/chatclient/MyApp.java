package com.example.jj.chatclient;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

public class MyApp extends Application {
    private Socket socket;
    private String username;
    private String address;
    private String port;
    private int socket_status;
    private Handler login_handler;
    private Handler chat_handler;
    private Thread loginthread;
    private Thread chatthread;
    final int MSG_CONNECT = 1;
    final int MSG_LOGIN = 2;
    final int MSG_EXISTEDNAME=3;
    final int MSG_SUCCESS = 0;
    final int MSG_FAILED = -1;
    final int SOCKET_BUSY = 1;
    final int SOCKET_READY = 0;
    private ProgressDialog logindialog;
    private String dialogmsg;


    public Socket get_socket() {
        return socket;
    }

    public Socket create_socket(String address, int port) throws IOException {
        socket = new Socket(address, port);
        return socket;
    }


    public String get_username() {
        return username;
    }

    public String get_address() {
        return address;
    }

    public String get_port() {
        return port;
    }

    public void save_data(String name, String addr, String p) {
        username = name;
        address = addr;
        port = p;
    }

    public int get_socket_status() {
        return socket_status;
    }

    public ProgressDialog create_dialog(Context context) {
        logindialog = new ProgressDialog(context);
        logindialog.setCanceledOnTouchOutside(false);
        logindialog.setCancelable(false);
        logindialog.setMessage(dialogmsg);
        return logindialog;
    }

    public void chat_logout(){
        chatthread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send_message(String s) {
        final String msg = s;
        Thread temp_thread = new Thread() {
            @Override
            public void run() {
                try {
                    socket.getOutputStream().write((String.format("say %s\n", msg)).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        temp_thread.run();
    }

    public Handler create_login_handler(Login login, MyApp myApp) {
        login_handler = new LoginHandler(login, myApp);
        return login_handler;
    }

    public Thread create_login_thread() {
        loginthread = new Thread() {
            @Override
            public void run() {
                try {
                    socket_status = SOCKET_BUSY;
                    String data;
                    send_msg_to_handler(MSG_CONNECT, login_handler);
                    socket = create_socket(address, Integer.parseInt(port));
                    send_msg_to_handler(MSG_LOGIN, login_handler);
                    socket.getOutputStream().write(("login " + username + "\n").getBytes());
                    Scanner scanner = new Scanner(socket.getInputStream());
                    data = scanner.nextLine().split(" ",2)[0];
                    if (Objects.equals(data, "OK")) {
                        send_msg_to_handler(MSG_SUCCESS, login_handler);

                    }
                    else if (Objects.equals(data,"ExistedName")){
                        send_msg_to_handler(MSG_EXISTEDNAME,login_handler);
                        socket.close();
                    }
                    else {
                        send_msg_to_handler(MSG_FAILED, login_handler);
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    send_msg_to_handler(MSG_FAILED, login_handler);
                }
                socket_status = SOCKET_READY;
            }
        };
        return loginthread;
    }

    public Handler create_chat_handler(Chat chat, MyApp myApp) {
        chat_handler = new ChatHandler(chat, myApp);
        return chat_handler;
    }

    public Thread create_chat_thread() {
        if (chatthread==null||!chatthread.isAlive()) {
            chatthread = new Thread() {
                boolean __end;

                @Override
                public void run() {
                    String data;
                    Message msg;
                    __end = false;
                    while (!__end)
                        try {
                            Scanner scanner = new Scanner(socket.getInputStream());
                            data = scanner.nextLine();
                            msg=chat_handler.obtainMessage();
                            msg.obj = String.format("%s\n", data);
                            chat_handler.sendMessage(msg);
                        } catch (IOException | NoSuchElementException e) {
                            e.printStackTrace();
                        }
                }

                @Override
                public void interrupt() {
                    super.interrupt();
                    __end = true;
                }
            };
        }
        return chatthread;
    }

    private static void send_msg_to_handler(int i, Handler h) {
        Message msg = h.obtainMessage();
        msg.what = i;
        h.sendMessage(msg);
    }

    public static class ChatHandler extends Handler {
        WeakReference<Chat> activity;
        MyApp myapp;

        public ChatHandler(Chat a, MyApp myApp) {
            activity = new WeakReference<Chat>(a);
            myapp = myApp;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (activity != null) {
                activity.get().message.append((String) msg.obj);
            }
        }
    }

    public static class LoginHandler extends Handler {
        WeakReference<Login> activity;
        MyApp myapp;

        public LoginHandler(Login a, MyApp myApp) {
            activity = new WeakReference<Login>(a);
            myapp = myApp;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (activity != null) {
                switch (msg.what) {
                    case 0:
                        Toast.makeText(activity.get(), "Success", Toast.LENGTH_SHORT).show();
                        activity.get().dialog_waiting.cancel();
                        activity.get().to_chat_activity();
                        break;
                    case 1:
                        myapp.dialogmsg = String.format("Connecting to %s:%s", myapp.get_address(), myapp.get_port());
                        activity.get().dialog_waiting.setMessage(myapp.dialogmsg);
                        break;
                    case 2:
                        myapp.dialogmsg = String.format("Trying to login as %s", myapp.get_username());
                        activity.get().dialog_waiting.setMessage(myapp.dialogmsg);
                        break;
                    case 3:
                        Toast.makeText(activity.get(), String.format("Failed, %s has already logined",myapp.get_username()), Toast.LENGTH_SHORT).show();
                        activity.get().dialog_waiting.cancel();
                        break;
                    case -1:
                        Toast.makeText(activity.get(), "Failed", Toast.LENGTH_SHORT).show();
                        activity.get().dialog_waiting.cancel();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
