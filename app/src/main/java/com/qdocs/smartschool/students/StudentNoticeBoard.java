package com.qdocs.smartschool.students;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.qdocs.smartschool.BaseActivity;
import com.qdocs.smartschool.adapters.StudentNotificationAdapter;
import com.qdocs.smartschool.utils.Constants;
import com.qdocs.smartschool.utils.Utility;
import com.qdocs.smartschool.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static android.widget.Toast.makeText;

public class StudentNoticeBoard extends BaseActivity {

    ListView notificationList;
    StudentNotificationAdapter adapter;
    ArrayList<String> noticeTitleId = new ArrayList<String>();
    ArrayList<String> downloadLinks=new ArrayList<String>();
    ArrayList<String> noticeTitleList = new ArrayList<String>();
    ArrayList <String> noticeDateList = new ArrayList<String>();
    ImageView linkButton;
    ArrayList <String> noticeDescList = new ArrayList<String>();
    public Map<String, String> params = new Hashtable<String, String>();
    public Map<String, String>  headers = new HashMap<String, String>();
    LinearLayout nodata_layout;




    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.student_notice_board_activity, null, false);
        mDrawerLayout.addView(contentView, 0);
        titleTV.setText(getApplicationContext().getString(R.string.noticeBoard));
        notificationList = (ListView) findViewById(R.id.studentNotice_listview);
        nodata_layout = (LinearLayout) findViewById(R.id.nodata_layout);
//           linkButton=(ImageView) findViewById(R.id.link_click_btn);
        adapter = new StudentNotificationAdapter(StudentNoticeBoard.this, noticeTitleId, noticeTitleList, noticeDateList, noticeDescList, downloadLinks);
        notificationList.setAdapter(adapter);

        if(Utility.isConnectingToInternet(getApplicationContext())){
            params.put("type", Utility.getSharedPreferences(getApplicationContext(), Constants.loginType));
            JSONObject obj=new JSONObject(params);
            Log.e("params ", obj.toString());
            getDataFromApi(obj.toString());
        }else{
            makeText(getApplicationContext(), R.string.noInternetMsg, Toast.LENGTH_SHORT).show();
        }

//        linkButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                String url = "http://www.gobloggerslive.com";
//
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);
//            }
//        });
//        linkButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                String url="https://www.google.com";
////                Intent google=new Intent(Intent.ACTION_VIEW);
////                google.setData(Uri.parse(url));
////                startActivity(google);
//            }
//        });
    }



    private void getDataFromApi (String bodyParams) {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();
        final String requestBody = bodyParams;
        String url = Utility.getSharedPreferences(getApplicationContext(), "apiUrl")+Constants.getNotificationsUrl;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                if (result != null) {
                    pd.dismiss();
                    try {
                        Log.e("Result", result);
                        JSONObject object = new JSONObject(result);
                        noticeDateList.clear();
                        noticeTitleList.clear();
                        noticeTitleId.clear();
                        noticeDescList.clear();
                        String success = object.getString("success");
                        if (success.equals("1")) {
                            notificationList.setVisibility(View.VISIBLE);
                            nodata_layout.setVisibility(View.GONE);
                            JSONArray dataArray = object.getJSONArray("data");
                            Log.e("length", dataArray.length()+"..");


                                for (int i = 0; i < dataArray.length(); i++) {
                                    noticeTitleId.add(dataArray.getJSONObject(i).getString("id"));
                                    noticeTitleList.add(dataArray.getJSONObject(i).getString("title"));
                                    noticeDateList.add(Utility.parseDate("yyyy-MM-dd", defaultDateFormat, dataArray.getJSONObject(i).getString("date")));
                                    // noticeDateList.add(dataArray.getJSONObject(i).getString("date"));
                                  String link =dataArray.getJSONObject(i).getString("document");
                                    downloadLinks.add(link);

//                                   Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(link));
                                    noticeDescList.add(dataArray.getJSONObject(i).getString("message"));
                                }

                                adapter.notifyDataSetChanged();

                        } else {
                            notificationList.setVisibility(View.GONE);
                            nodata_layout.setVisibility(View.VISIBLE);
                           // Toast.makeText(getApplicationContext(), object.getString("errorMsg"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    pd.dismiss();

                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        pd.dismiss();
                        Log.e("Volley Error", volleyError.toString());
                        Toast.makeText(StudentNoticeBoard.this, R.string.apiErrorMsg, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                headers.put("Client-Service", Constants.clientService);
                headers.put("Auth-Key", Constants.authKey);
                headers.put("Content-Type", Constants.contentType);
                headers.put("User-ID", Utility.getSharedPreferences(getApplicationContext(), "userId"));
                headers.put("Authorization", Utility.getSharedPreferences(getApplicationContext(), "accessToken"));
                Log.e("Headers", headers.toString());
                return headers;
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }


        };
        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(StudentNoticeBoard.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }




}
