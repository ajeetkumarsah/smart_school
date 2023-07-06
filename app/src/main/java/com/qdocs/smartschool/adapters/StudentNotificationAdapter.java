package com.qdocs.smartschool.adapters;



import android.content.Intent;
import android.graphics.Color;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.fragment.app.FragmentActivity;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.qdocs.smartschool.R;
import com.qdocs.smartschool.utils.Constants;
import com.qdocs.smartschool.utils.Utility;
import java.util.ArrayList;

public class StudentNotificationAdapter extends BaseAdapter {

    private FragmentActivity context;
    private ArrayList<String> noticeTitleId;
    private ArrayList<String> noticeTitleList;
    private ArrayList<String> noticeDateList;
    private ArrayList<String> noticeDescList;
    private ArrayList<String> downloadLinks;

    public StudentNotificationAdapter(FragmentActivity studentNoticeBoard, ArrayList<String> noticeTitleId,
                                      ArrayList<String> noticeTitleList, ArrayList<String> noticeDateList, ArrayList<String> noticeDescList,ArrayList<String> downloadLinks) {

        this.context = studentNoticeBoard;
        this.noticeTitleId = noticeTitleId;
        this.noticeTitleList = noticeTitleList;
        this.noticeDateList = noticeDateList;
        this.noticeDescList = noticeDescList;
        this.downloadLinks= downloadLinks;
    }

    @Override
    public int getCount() {
        return noticeTitleId.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        View rowView = view;
        ViewHolder viewHolder = null;

        if (rowView == null) {

            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.adapter_student_notification, viewGroup, false);
            viewHolder = new ViewHolder();

            viewHolder.viewBtn = rowView.findViewById(R.id.studentNotificationAdapter_viewBtn);
            viewHolder.notifTitle = (TextView) rowView.findViewById(R.id.studentNotificationAdapter_titleTV);
            viewHolder.notifDate = (TextView) rowView.findViewById(R.id.studentNotificationAdapter_dateTV);
            viewHolder.linkBtn=(ImageView) rowView.findViewById(R.id.link_click_btn);

            viewHolder.viewBtn.setTag(position);
            viewHolder.notifTitle.setTag(position);
            viewHolder.notifDate.setTag(position);
            viewHolder.linkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = downloadLinks.get(position);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    context.startActivity(intent);
                }
            });

        }else{
            viewHolder  = (ViewHolder) rowView.getTag();
        }

        viewHolder.notifTitle.setText(noticeTitleList.get(position));
        viewHolder.notifDate.setText(noticeDateList.get(position));

        //DECORATE
        viewHolder.notifTitle.setBackgroundColor(Color.parseColor(Utility.getSharedPreferences(context.getApplicationContext(), Constants.secondaryColour)));

        viewHolder.viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View bottomSheet = context.getLayoutInflater().inflate(R.layout.fragment_notification_bottom_sheet, null);

                TextView headerTV = bottomSheet.findViewById(R.id.studentNotification_bottomSheet__header);
                ImageView crossBtn = bottomSheet.findViewById(R.id.studentNotification_bottomSheet__crossBtn);
                WebView notificationDetailTV = bottomSheet.findViewById(R.id.studentNotification_bottomSheet_descTV);
                notificationDetailTV.getSettings().setJavaScriptEnabled(true);
                notificationDetailTV.getSettings().setBuiltInZoomControls(true);
                notificationDetailTV.getSettings().setLoadWithOverviewMode(true);
                notificationDetailTV.getSettings().setUseWideViewPort(true);
                notificationDetailTV.getSettings().setDefaultFontSize(40);

                headerTV.setBackgroundColor(Color.parseColor(Utility.getSharedPreferences(context.getApplicationContext(), Constants.secondaryColour)));

                headerTV.setText(noticeTitleList.get(position));
                notificationDetailTV.loadData(noticeDescList.get(position),"text/html; charset=utf-8", "UTF-8");

                final BottomSheetDialog dialog = new BottomSheetDialog(context);

                dialog.setContentView(bottomSheet);
                dialog.show();

                crossBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        rowView.setTag(viewHolder);
        return rowView;
    }

    static class ViewHolder {
        public TextView notifTitle, notifDate;
        public LinearLayout viewBtn;
        public ImageView linkBtn;
    }
}
