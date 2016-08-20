package com.so2.running;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NotificationAdapter extends ArrayAdapter<NotificationListItem> {
    private LayoutInflater mInflater;
    private ArrayList<NotificationListItem> sessionList;
    SharedPreferences preferences = getContext().getSharedPreferences("here", Context.MODE_PRIVATE);       //目前使用者的名字
    SharedPreferences preferences1 = this.getContext().getSharedPreferences("creater_detail", Context.MODE_PRIVATE);       //你想加好友的人
    String user_name = preferences.getString("name","error");
    String friend_name = preferences1.getString("creater_name","error1");

    public NotificationAdapter(Context context, int resource, List<NotificationListItem> objects) {
        super(context, resource, objects);
        sessionList = (ArrayList) objects;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return sessionList.size();
    }

    @Override
    public NotificationListItem getItem(int arg0) {
        return sessionList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        TextView content;
        Button decide_button ;

        convertView = mInflater.inflate(R.layout.notification_list_item, null);

        content = (TextView) convertView.findViewById(R.id.name);
        decide_button = (Button) convertView.findViewById(R.id.decide_button);

        content.setText(sessionList.get(position).getContent());

        decide_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://ncnurunforall-yychiu.rhcloud.com/friendlists/"+user_name+"/"+friend_name;
                String url1 = "http://ncnurunforall-yychiu.rhcloud.com/notices/"+user_name+"/"+friend_name;
                try {
                    doPatchFriendListRequest(url);
                    doPatchNoticeRequest(url1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return convertView;
    }

    void doPatchFriendListRequest(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("status", "1")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .patch(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Error
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    handlePostResponse(res);
                } else {
                    Log.e("APp", "Error");
                }
            }

        });
    }

    void doPatchNoticeRequest(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("status", "1")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .patch(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Error
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    handlePostResponse(res);
                } else {
                    Log.e("APp", "Error");
                }
            }
        });
    }

    void handlePostResponse(String response) {
        Log.i("OkHttpPost", response);
    }

}