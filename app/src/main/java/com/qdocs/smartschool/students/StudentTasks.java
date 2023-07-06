package com.qdocs.smartschool.students;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.qdocs.smartschool.utils.Constants;
import com.qdocs.smartschool.utils.Utility;
import com.qdocs.smartschool.R;
import com.qdocs.smartschool.adapters.StudentTaskAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static android.widget.Toast.makeText;

public class StudentTasks extends BaseActivity implements DatePickerDialog.OnDateSetListener    {

    RecyclerView taskListView;
    FloatingActionButton addTaskBtn;
    StudentTaskAdapter adapter;
    String startweek;
    private boolean isDateSelected = false;
    ArrayList<String> taskIdList = new ArrayList<>();
    ArrayList<String> taskTitleList = new ArrayList<>();
    ArrayList<String> taskStatusList = new ArrayList<>();
    ArrayList<String> taskDateList = new ArrayList<>();
    public Map<String, String> params = new Hashtable<String, String>();
    public Map<String, String> createTaskParams = new Hashtable<String, String>();
    public Map<String, String>  headers = new HashMap<String, String>();
    TextView dateTV;
    SwipeRefreshLayout pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.student_tasks_activity, null, false);
        mDrawerLayout.addView(contentView, 0);
        titleTV.setText(getApplicationContext().getString(R.string.toDo));
        taskListView = (RecyclerView) findViewById(R.id.studentTasks_listview);
        addTaskBtn = findViewById(R.id.studentTasks_fab);
        startweek = Utility.getSharedPreferences(getApplicationContext(), "startWeek");
        //DECORATE
//     addTaskBtn.setBackgroundColor(Color.parseColor(Utility.getSharedPreferences(getApplicationContext(), Constants.primaryColour)));
        addTaskBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(Utility.getSharedPreferences(getApplicationContext(), Constants.primaryColour))));
        //DECORATE
        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog(StudentTasks.this);
            }
        });
        adapter = new StudentTaskAdapter(StudentTasks.this,taskListView, taskIdList, taskTitleList, taskStatusList, taskDateList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        taskListView.setLayoutManager(mLayoutManager);
        taskListView.setItemAnimator(new DefaultItemAnimator());
        taskListView.setAdapter(adapter);
        loaddata();
        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefresh.setRefreshing(true);
                loaddata();
            }
        });
    }

    public  void  loaddata(){
        if (Utility.isConnectingToInternet(getApplicationContext())) {
            params.put("user_id", Utility.getSharedPreferences(getApplicationContext(), Constants.userId));
            JSONObject obj=new JSONObject(params);
            Log.e("params ", obj.toString());
            getDataFromApi(obj.toString());
        } else {
            makeText(getApplicationContext(),R.string.noInternetMsg, Toast.LENGTH_SHORT).show();
        }

    }

    private void showAddDialog(Context context) {

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.add_task_dialog);

        RelativeLayout headerLay = (RelativeLayout) dialog.findViewById(R.id.addTask_dialog_header);
        RelativeLayout dateBtn = (RelativeLayout) dialog.findViewById(R.id.addTask_dialog_dateBtn);
        ImageView closeBtn = (ImageView) dialog.findViewById(R.id.addTask_dialog_crossIcon);

        dateTV = dialog.findViewById(R.id.addTask_dialog_dateTV);
        final EditText titleET = dialog.findViewById(R.id.addTask_dialog_titleET);
        Button submitBtn = dialog.findViewById(R.id.addTask_dialog_submitBtn);

        final Calendar c = Calendar.getInstance();
        int startYear = c.get(Calendar.YEAR);
        int starthMonth = c.get(Calendar.MONTH);
        int startDay = c.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog datePickerDialog = new DatePickerDialog(context, StudentTasks.this, startYear, starthMonth, startDay);
        if(startweek.equals("Monday")){
            datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
        }else if(startweek.equals("Tuesday")){
            datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.TUESDAY);
        }else if(startweek.equals("Wednesday")){
            datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.WEDNESDAY);
        }else if(startweek.equals("Thursday")){
            datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.THURSDAY);
        }else if(startweek.equals("Friday")){
            datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.FRIDAY);
        }else if(startweek.equals("Saturday")){
            datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.SATURDAY);
        }else if(startweek.equals("Sunday")){
            datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.SUNDAY);
        }

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    datePickerDialog.show();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isDateSelected) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.selectDateError), Toast.LENGTH_LONG).show();
                } else if (titleET.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.selectTitleError), Toast.LENGTH_LONG).show();
                } else {
                   // createTaskParams.put("task_date", date);
                    if (Utility.isConnectingToInternet(getApplicationContext())) {
                        createTaskParams.put("user_id", Utility.getSharedPreferences(getApplicationContext(), "userId"));
                        createTaskParams.put("event_title", titleET.getText().toString());
                        JSONObject obj=new JSONObject(createTaskParams);
                        Log.e("params ", obj.toString());
                        createTaskApi(obj.toString());
                    } else {
                        makeText(getApplicationContext(),R.string.noInternetMsg, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        //DECORATE
        headerLay.setBackgroundColor(Color.parseColor(Utility.getSharedPreferences(getApplicationContext(), Constants.primaryColour)));
        submitBtn.setBackgroundColor(Color.parseColor(Utility.getSharedPreferences(getApplicationContext(), Constants.primaryColour)));
        //DECORATE
        dialog.show();
    }

    private void getDataFromApi (String bodyParams) {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();

        final String requestBody = bodyParams;

        String url = Utility.getSharedPreferences(getApplicationContext(), "apiUrl")+Constants.getTaskUrl;
        Log.e("URL", url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                pullToRefresh.setRefreshing(false);
                if (result != null) {
                    pd.dismiss();
                    try {
                        Log.e("Result", result);
                        JSONObject object = new JSONObject(result);
                        JSONArray dataArray = object.getJSONArray("tasks");
                        taskIdList.clear();
                        taskTitleList.clear();
                        taskStatusList.clear();
                        taskDateList.clear();
                        if (dataArray.length() != 0) {

                            for(int i = 0; i < dataArray.length(); i++) {
                                taskIdList.add(dataArray.getJSONObject(i).getString("id"));
                                taskTitleList.add(dataArray.getJSONObject(i).getString("event_title"));
                                taskStatusList.add(dataArray.getJSONObject(i).getString("is_active"));
                                String startDate = Utility.parseDate("yyyy-MM-dd HH:mm:ss", defaultDateFormat, dataArray.getJSONObject(i).getString("start_date"));
                                //String endDate = Utility.parseDate("yyyy-MM-dd HH:mm:ss", defaultDateFormat, dataArray.getJSONObject(i).getString("end_date"));
                                taskDateList.add(startDate);
                            }
                            adapter.notifyDataSetChanged();

                        } else {
                            pullToRefresh.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.noData), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    pd.dismiss();
                    pullToRefresh.setVisibility(View.GONE);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                Log.e("Volley Error", volleyError.toString());
                Toast.makeText(StudentTasks.this, R.string.apiErrorMsg, Toast.LENGTH_LONG).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(StudentTasks.this); //Creating a Request Queue
        requestQueue.add(stringRequest); //Adding request to the queue
    }

    private void createTaskApi (String bodyParams) {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();

        final String requestBody = bodyParams;

        String url = Utility.getSharedPreferences(getApplicationContext(), "apiUrl")+Constants.createTaskUrl;
        Log.e("URL",url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                if (result != null) {
                    pd.dismiss();
                    try {
                        Log.e("Result", result);
                        JSONObject object = new JSONObject(result);

                        String status = object.getString("status");

                        Toast.makeText(getApplicationContext(), object.getString("msg"), Toast.LENGTH_LONG).show();

                        if(status.equals("1")) {
                            finish();
                            startActivity(getIntent());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    pd.dismiss();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                Log.e("Volley Error", volleyError.toString());
                Toast.makeText(StudentTasks.this, R.string.apiErrorMsg, Toast.LENGTH_LONG).show();
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
        RequestQueue requestQueue = Volley.newRequestQueue(StudentTasks.this);//Creating a Request Queue
        requestQueue.add(stringRequest);//Adding request to the queue
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        String date = year+"-"+(++monthOfYear)+"-"+dayOfMonth;
        createTaskParams.put("date", date);
        dateTV.setText(date);
        isDateSelected = true;

    }
}
