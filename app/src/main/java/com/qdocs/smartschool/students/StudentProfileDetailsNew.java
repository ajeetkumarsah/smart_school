package com.qdocs.smartschool.students;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.qdocs.smartschool.Login;
import com.qdocs.smartschool.adapters.StudentProfileAdapter;
import com.qdocs.smartschool.fragments.StudentOtherDetailNew;
import com.qdocs.smartschool.fragments.StudentParentsDetailNew;
import com.qdocs.smartschool.fragments.StudentPersonalDetailNew;
import com.qdocs.smartschool.utils.Constants;
import com.qdocs.smartschool.utils.Utility;
import com.qdocs.smartschool.R;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.widget.Toast.makeText;

public class StudentProfileDetailsNew extends BaseActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    TextView nameTV, admissionNoTV, classTV, rollNoTV;
    ImageView profileIV;
    RelativeLayout headerLayout;
    public Map<String, String> headers = new HashMap<String, String>();
    public Map<String, String> params = new Hashtable<String, String>();
    StudentProfileAdapter adapter;
    JSONObject customArray;
    LinearLayout rollno_layout;
    ProfileViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.student_profile_activity, null, false);
        mDrawerLayout.addView(contentView, 0);
        titleTV.setText(getApplicationContext().getString(R.string.profile));
        viewPager = (ViewPager) findViewById(R.id.profileViewPager);
        profileIV = (ImageView) findViewById(R.id.studentProfile_profileImageview);
        nameTV = (TextView) findViewById(R.id.studentProfile_nameTV);
        rollno_layout = (LinearLayout) findViewById(R.id.rollno_layout);
        admissionNoTV = (TextView) findViewById(R.id.studentProfile_admissionNoTV);
        rollNoTV = (TextView) findViewById(R.id.studentProfile_rollNoTV);
        classTV = (TextView) findViewById(R.id.studentProfile_classTV);
        headerLayout = findViewById(R.id.profile_headerLayout);
        viewPagerAdapter = new ProfileViewPagerAdapter(getSupportFragmentManager());
        tabLayout = (TabLayout) findViewById(R.id.profileTabLayout);
        tabLayout.setupWithViewPager(viewPager);
        if(Utility.isConnectingToInternet(StudentProfileDetailsNew.this)){
            params.put("student_id", Utility.getSharedPreferences(getApplicationContext(), "studentId"));
            JSONObject obj=new JSONObject(params);
            Log.e("params ", obj.toString());
            getDataFromApi(obj.toString());
            setupViewPager(viewPager);
        }else{
            makeText(getApplicationContext(),R.string.noInternetMsg, Toast.LENGTH_SHORT).show();
        }

        decorate();
        Locale current = getResources().getConfiguration().locale;
        Log.e("current locale", current.getDisplayName()+"..");
    }

    private void decorate () {
        headerLayout.setBackgroundColor(Color.parseColor(Utility.getSharedPreferences(getApplicationContext(), Constants.secondaryColour)));
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor(Utility.getSharedPreferences(getApplicationContext(), Constants.primaryColour)));
    }

    private void getDataFromApi (String bodyParams) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.setCancelable(false);
        pd.show();

        final String requestBody = bodyParams;
        String url = Utility.getSharedPreferences(getApplicationContext(), "apiUrl")+Constants.getStudentProfileUrl;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String result) {
                if (result != null) {
                    pd.dismiss();
                    try {
                        Log.e("StudentProfileResult", result);
                        JSONObject object = new JSONObject(result);

                            JSONObject dataArray = object.getJSONObject("student_result");

                            nameTV.setText(Utility.getSharedPreferences(StudentProfileDetailsNew.this, Constants.userName));
                            admissionNoTV.setText(dataArray.getString("admission_no"));
                            rollNoTV.setText(dataArray.getString("roll_no"));
                            classTV.setText(dataArray.getString("class") + " - " + dataArray.getString("section"));

                            String imgUrl = Utility.getSharedPreferences(getApplicationContext(), "imagesUrl") + dataArray.getString("image");
                            Picasso.with(getApplicationContext()).load(imgUrl).placeholder(R.drawable.placeholder_user).into(profileIV);

                        JSONObject fieldsArray = object.getJSONObject("student_fields");

                        if(!fieldsArray.has("roll_no")){
                            rollno_layout.setVisibility(View.GONE);
                        }if(!fieldsArray.has("student_photo")){
                            profileIV.setVisibility(View.GONE);
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
                Toast.makeText(StudentProfileDetailsNew.this, R.string.apiErrorMsg, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                headers.put("Client-Service", Constants.clientService);
                headers.put("Auth-Key", Constants.authKey);
                headers.put("Content-Type", Constants.contentType);
                headers.put("User-ID", Utility.getSharedPreferences(getApplicationContext(), "userId"));
                headers.put("Authorization", Utility.getSharedPreferences(getApplicationContext(), "accessToken"));
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
        RequestQueue requestQueue = Volley.newRequestQueue(StudentProfileDetailsNew.this);//Creating a Request Queue
        requestQueue.add(stringRequest);//Adding request to the queue
    }

    private String checkData(String key) {
        if(key.equals("null")) {
            return "";
        } else {
            return key;
        }
    }
    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter.addFragment(new StudentPersonalDetailNew(),getApplicationContext().getString(R.string.personalDetail));
        viewPagerAdapter.addFragment(new StudentParentsDetailNew(), getApplicationContext().getString(R.string.parentsDetails));
        viewPagerAdapter.addFragment(new StudentOtherDetailNew(), getApplicationContext().getString(R.string.otherDetails));
        viewPager.setAdapter(viewPagerAdapter);
    }

    class ProfileViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private Context context;


        public ProfileViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment,String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}