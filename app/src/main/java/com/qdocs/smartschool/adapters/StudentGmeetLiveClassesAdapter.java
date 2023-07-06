package com.qdocs.smartschool.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.qdocs.smartschool.R;
import com.qdocs.smartschool.utils.Constants;
import com.qdocs.smartschool.utils.Utility;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static android.widget.Toast.makeText;

public class StudentGmeetLiveClassesAdapter extends RecyclerView.Adapter<StudentGmeetLiveClassesAdapter.MyViewHolder> {

    private FragmentActivity context;
    private ArrayList<String> titleList;
    private ArrayList<String> dateList;
    private ArrayList<String> classList;
    private ArrayList<String> idList;
    private ArrayList<String> join_url;
    private ArrayList<String> statuslist;
    private ArrayList<String> staff_namelist;
    public Map<String, String> params = new Hashtable<String, String>();
    public Map<String, String> headers = new HashMap<String, String>();
    String targetdatevalue,defaultDatetimeFormat;
    public StudentGmeetLiveClassesAdapter(FragmentActivity studentonlineexam, ArrayList<String> titleList, ArrayList<String> dateList,
                                          ArrayList<String> classList, ArrayList<String> idList, ArrayList<String> join_url, ArrayList<String> statuslist, ArrayList<String> staff_namelist) {
        this.context = studentonlineexam;
        this.titleList = titleList;
        this.dateList = dateList;
        this.classList = classList;
        this.idList = idList;
        this.join_url = join_url;
        this.statuslist = statuslist;
        this.staff_namelist = staff_namelist;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView titlename, date, classes, class_host, status;
        LinearLayout joinclass;
        RelativeLayout headLayout;

        public MyViewHolder(View view) {
            super(view);
            joinclass = view.findViewById(R.id.adapter_student_gmeet_joinclass);
            titlename = view.findViewById(R.id.adapter_student_gmeet_titlename);
            headLayout = view.findViewById(R.id.adapter_student_headLayout);
            status = view.findViewById(R.id.gmeet_status);
            date = view.findViewById(R.id.gmeet_date);
            classes = view.findViewById(R.id.gmeet_classes);
            class_host = view.findViewById(R.id.gmeet_class_host);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_student_gmeetliveclass, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        defaultDatetimeFormat = Utility.getSharedPreferences(context.getApplicationContext(), "datetimeFormat");
        holder.headLayout.setBackgroundColor(Color.parseColor(Utility.getSharedPreferences(context.getApplicationContext(), Constants.secondaryColour)));
        holder.titlename.setText(titleList.get(position));

        holder.date.setText(dateList.get(position));
        holder.classes.setText(classList.get(position));
        holder.class_host.setText(staff_namelist.get(position));
        if (statuslist.get(position).equals("0")) {
            holder.status.setText("Awaited");
            holder.status.setBackgroundResource(R.drawable.yellow_border);
            holder.joinclass.setVisibility(View.VISIBLE);
            holder.joinclass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = join_url.get(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    context.startActivity(intent);
                    if (Utility.isConnectingToInternet(context.getApplicationContext())) {
                        params.put("student_id", Utility.getSharedPreferences(context.getApplicationContext(), Constants.studentId));
                        params.put("gmeet_id", idList.get(position));
                        JSONObject obj=new JSONObject(params);
                        Log.e("parameteres ", obj.toString());
                        getDataFromApi(obj.toString());
                    } else {
                        makeText(context.getApplicationContext(),R.string.noInternetMsg, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        } else if (statuslist.get(position).equals("2")) {
            holder.status.setText("Finished");
            holder.joinclass.setVisibility(View.GONE);
            holder.status.setBackgroundResource(R.drawable.green_border);
        } else {
            holder.status.setText("Cancelled");
            holder.joinclass.setVisibility(View.GONE);
            holder.status.setBackgroundResource(R.drawable.red_border);
        }
    }

    @Override
    public int getItemCount() {
        return idList.size();
    }

    private void getDataFromApi (String bodyParams) {
        final String requestBody = bodyParams;
        String url = Utility.getSharedPreferences(context.getApplicationContext(), "apiUrl")+Constants.gmeethistoryUrl;
        Log.e("gmeethistoryUrl==", url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                if (result != null) {
                    try {
                        Log.e(" Result", result);
                        JSONObject obj = new JSONObject(result);
                        Toast.makeText(context, ""+obj.getString("msg"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                Log.e("Volley Error", volleyError.toString());
                Toast.makeText(context.getApplicationContext(), R.string.apiErrorMsg, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                headers.put("Client-Service", Constants.clientService);
                headers.put("Auth-Key", Constants.authKey);
                headers.put("Content-Type", Constants.contentType);
                headers.put("User-ID", Utility.getSharedPreferences(context.getApplicationContext(), "userId"));
                headers.put("Authorization", Utility.getSharedPreferences(context.getApplicationContext(), "accessToken"));
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
        RequestQueue requestQueue = Volley.newRequestQueue(context.getApplicationContext()); //Creating a Request Queue
        requestQueue.add(stringRequest);  //Adding request to the queue
    }

}


