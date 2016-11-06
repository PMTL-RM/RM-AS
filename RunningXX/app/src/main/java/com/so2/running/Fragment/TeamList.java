package com.so2.running.Fragment;


import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.so2.running.Adapter.TeamListAdapter;
import com.so2.running.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TeamList extends android.app.Fragment {
    TeamListItem item2 = new TeamListItem();
    Button returnbutton ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences preferences = this.getActivity().getSharedPreferences("here", Context.MODE_PRIVATE);
        String name = preferences.getString("name","error");

        ArrayList<TeamListItem> sessionList;
        View view = inflater.inflate(R.layout.fragment_team_list, container, false);
        final ListView listview = (ListView) view.findViewById(R.id.team_list_view);

//        returnbutton = (Button)view.findViewById(R.id.returnbutton);


//        returnbutton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.content_frame, new MainFragment());
//                ft.commit();
//            }
//        });

        //Set ActionBar title
        getActivity().setTitle("創辦的團");
        // Inflate the layout for this fragment


        sessionList = getSessionList(name);

        //If there are no sessions emtyListFragment is called
        if (sessionList.size() == 0) {
            view = inflater.inflate(R.layout.null_scene, container, false);
            ;
        }

        //Visualize session list
        else {
            listview.setAdapter(new TeamListAdapter(getActivity(), R.layout.fragment_team_list_item, sessionList));

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                //Go to session detail
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    // selected item
                    final TeamListItem item = (TeamListItem) listview.getItemAtPosition(position);
                    System.out.println("the position : : "+position);
                    TeamListDetail itemDetail = new TeamListDetail();
                    itemDetail.setItem(item);
                    FragmentManager fm = getFragmentManager();
                    fm.beginTransaction()
                            .replace(R.id.content_frame, itemDetail)
                            .commit();
                }
            });

            return view;
        }
        return view;
    }

    public ArrayList<TeamListItem> getSessionList(String name) {
        final ArrayList<TeamListItem> sessionList = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();

        System.out.println("you should be :::" + name);

        Request req = new Request.Builder()
                .url("http://ncnurunforall-yychiu.rhcloud.com/groups/" + name)
                .build();
        Call call = client.newCall(req);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String aFinalString = response.body().string();
                System.out.println(aFinalString);
                TeamListItem item;
                if (response.isSuccessful()) try {
                    final JSONArray array = new JSONArray(aFinalString);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        item = new TeamListItem();

                        item.setUsername(obj.getString("username"));
                        item.setGroupname(obj.getString("groupname"));
                        item.setContent(obj.getString("content"));
                        item.setDate(obj.getString("date"));
                        item.setTime(obj.getString("time"));
                        item.setLocation(obj.getString("location"));
                        item.setStart(obj.getString("start"));
                        item.setEnd(obj.getString("end"));
                        item.setPrivacy(obj.getString("privacy"));
                        item.setCreatername(obj.getString("creatername"));
                        item.setImagename(obj.getString("imagename"));
                        item.setUrl(obj.getString("imagename"));

                        item2 = item;

                        sessionList.add(item2);
                        Log.d("JSON:", item2.getUsername() + "/" + item2.getGroupname() + "/" + item2.getDate()+item2.getTime() + "/" + item2.getContent() + "/" + item2.getLocation()+ "/" + item2.getPrivacy()+"/"+item2.getStart()+"/"+item2.getEnd());

                    }

                } catch (JSONException e) {


                    e.printStackTrace();
                }
                else {
                    Log.e("APp", "Error");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                //告知使用者連線失敗
            }
        });

        if (item2.getUsername() == null) {
            synchronized (this) {
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            onStart();
        }
        return sessionList;
    }
}