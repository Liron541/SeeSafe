package com.cs407.seesafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<String> groupList; // Headers (e.g., Volunteers, Blind Friends)
    private final HashMap<String, List<String>> childList; // Children under each header

    public ExpandableListAdapter(Context context, List<String> groupList, HashMap<String, List<String>> childList) {
        this.context = context;
        this.groupList = groupList;
        this.childList = childList;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(groupList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(groupList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.group_item, parent, false);
        }

        TextView groupTextView = convertView.findViewById(R.id.groupTextView);
        ImageView groupArrow = convertView.findViewById(R.id.groupArrow);

        // Set group text
        String groupTitle = (String) getGroup(groupPosition);
        groupTextView.setText(groupTitle);

        // Set arrow direction based on expansion state
        if (isExpanded) {
            groupArrow.setRotation(180); // Arrow pointing up
        } else {
            groupArrow.setRotation(0); // Arrow pointing down
        }

        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String childTitle = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(childTitle);
        textView.setTextSize(16);
        textView.setPadding(80, 10, 10, 10);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
