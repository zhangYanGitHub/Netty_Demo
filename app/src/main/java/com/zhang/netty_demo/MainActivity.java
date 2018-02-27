package com.zhang.netty_demo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.zhang.netty_lib.NettyService;
import com.zhang.netty_lib.activity.NettyActivity;
import com.zhang.netty_lib.bean.ChatInfo;
import com.zhang.netty_lib.bean.NettyBaseFeed;
import com.zhang.netty_lib.netty.NettyClient;

import java.util.ArrayList;

public class MainActivity extends NettyActivity {

    private EditText etContent;
    private Button btn;
    private ListView listView;
    private ArrayList<String> contentList;
    private Gson gson;
    private ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Intent intent = new Intent(getApplication(), NettyService.class);
        startService(intent);
        etContent = findViewById(R.id.et_content);
        listView = findViewById(R.id.list_view);
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        contentList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, R.layout.item_text_view, R.id.tv_content, contentList);
        listView.setAdapter(adapter);
        gson = new Gson();
    }

    private void sendMessage() {
        final String message = etContent.getText().toString();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        final NettyBaseFeed<ChatInfo> baseFeed = new NettyBaseFeed<>();
        baseFeed.setModule(1);
        baseFeed.setCmd(2);
        final ChatInfo chatInfo = new ChatInfo();
        baseFeed.setData(chatInfo);
        chatInfo.setChatType(2);
        chatInfo.setFrom(1);
        chatInfo.setTo(50);
        chatInfo.setMsgType(1);
        chatInfo.setMessage(message);
        NettyClient.getInstance().sendMessage(baseFeed, null);
        notifyData(gson.toJson(baseFeed));
        etContent.setText(null);
    }

    protected void notifyData(String json) {
        if (TextUtils.isEmpty(json)) {
            return;
        }
        contentList.add(json);
        adapter.notifyDataSetChanged();
        listView.setSelection(adapter.getCount() - 1);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
