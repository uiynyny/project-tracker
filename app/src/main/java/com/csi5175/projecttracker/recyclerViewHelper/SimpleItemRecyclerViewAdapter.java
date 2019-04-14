package com.csi5175.projecttracker.recyclerViewHelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.csi5175.projecttracker.Entity.Project;
import com.csi5175.projecttracker.fragment.ItemDetailFragment;
import com.csi5175.projecttracker.R;
import com.csi5175.projecttracker.activity.ItemDetailActivity;
import com.csi5175.projecttracker.activity.ItemListActivity;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

    private final static String TAG = SimpleItemRecyclerViewAdapter.class.getSimpleName();
    private final ItemListActivity mParentActivity;
    private final List<Project> mProjects;
    private final boolean mTwoPane;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Project item = (Project) view.getTag();
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putString(ItemDetailFragment.ARG_ITEM_ID, item.getUid());
                ItemDetailFragment fragment = new ItemDetailFragment();
                fragment.setArguments(arguments);
                mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, ItemDetailActivity.class);
                intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.getUid());
                context.startActivity(intent);
            }
        }
    };

    public SimpleItemRecyclerViewAdapter(ItemListActivity parent, List<Project> projects, boolean twoPane) {
        mProjects = projects;
        mParentActivity = parent;
        mTwoPane = twoPane;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mIdView.setText(mProjects.get(position).getProjectNumber());
        holder.mContentView.setText(mProjects.get(position).getProjectDescription());

        LinearLayout layout = (LinearLayout) holder.mDueView.getParent();
        int deadline = calculateDue(mProjects.get(position).getDue());
        if (mProjects.get(position).getCompleted()) {
            layout.setBackgroundColor(Color.GRAY);
            holder.mDueView.setText("Completed");
        } else if (!mProjects.get(position).getCompleted() && deadline < 48 && deadline > 0) {
            holder.mDueView.setTextColor(Color.RED);
            layout.setBackgroundColor(Color.YELLOW);
            String message = MessageFormat.format("{0} hours left", deadline);
            holder.mDueView.setText(message);
        } else if (deadline < 0) {
            String message = "Over Due";
            holder.mDueView.setText(message);
        } else {
            String message = MessageFormat.format("{0} days left", deadline / 24);
            holder.mDueView.setText(message);
            holder.mDueView.setTextColor(Color.DKGRAY);
        }

        holder.itemView.setTag(mProjects.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    private int calculateDue(String dueDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        try {
            sdf.parse(dueDate);
            long cur = Calendar.getInstance().getTimeInMillis();
            long hourLeft = (sdf.parse(dueDate).getTime() - cur) / (60 * 60 * 1000);
            return (int) hourLeft;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return mProjects.size();
    }

    public List<Project> getProjects() {
        return mProjects;
    }

    public void removeItem(int position) {
        mProjects.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Project item, int position) {
        mProjects.add(position, item);
        notifyItemInserted(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mIdView;
        final TextView mContentView;
        final TextView mDueView;

        ViewHolder(View view) {
            super(view);
            mIdView = view.findViewById(R.id.id_text);
            mContentView = view.findViewById(R.id.id_content);
            mDueView = view.findViewById(R.id.id_duedate);
        }
    }
}