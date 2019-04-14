package com.csi5175.projecttracker.fragment;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.csi5175.projecttracker.Entity.Project;
import com.csi5175.projecttracker.Entity.ProjectManager;
import com.csi5175.projecttracker.R;
import com.csi5175.projecttracker.activity.ItemDetailActivity;
import com.csi5175.projecttracker.activity.ItemListActivity;
import com.csi5175.projecttracker.awsUtility.AwsConstant;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    private final Calendar myCalendar = Calendar.getInstance();
    private Button mDatePick;
    private TextView tvLeft;
    private Project mProject;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CollapsingToolbarLayout appBarLayout = getActivity().findViewById(R.id.toolbar_layout);
        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            mProject = ProjectManager.getInstance().getByUid(getArguments().getString(ARG_ITEM_ID));
            if (appBarLayout != null) {
                assert mProject != null;
                appBarLayout.setTitle(mProject.getCourseNumber() + " " + mProject.getProjectNumber());
            }
        } else if (getArguments() != null && getArguments().size() == 8) {
            setupProject();
            appBarLayout.setTitle(mProject.getCourseNumber() + " " + mProject.getProjectNumber());
        } else {
            appBarLayout.setTitle(getString(R.string.newProject));
        }
    }

    private void setupProject() {
        mProject = new Project();
        mProject.setCourseTitle(getArguments().getString(getString(R.string.course_title)));
        mProject.setCourseNumber(getArguments().getString(getString(R.string.course_number)));
        mProject.setInstructorName(getArguments().getString(getString(R.string.instructor_name)));
        mProject.setProjectNumber(getArguments().getString(getString(R.string.project_number)));
        mProject.setProjectDescription(getArguments().getString(getString(R.string.project_description)));
        mProject.setComplete(getArguments().getBoolean(getString(R.string.complete)));
        mProject.setDue(getArguments().getString(getString(R.string.dueDate)));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        mDatePick = rootView.findViewById(R.id.date_pick);
        tvLeft = rootView.findViewById(R.id.date_left);

        mDatePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        if (mProject != null) {
            EditText ct = rootView.findViewById(R.id.etCourseTitle);
            EditText cn = rootView.findViewById(R.id.etCourseNumber);
            EditText in = rootView.findViewById(R.id.etInstructorName);
            EditText pn = rootView.findViewById(R.id.etProjectNumber);
            EditText pd = rootView.findViewById(R.id.etDescription);
            Switch c = rootView.findViewById(R.id.complete);
            ct.setText(mProject.getCourseTitle());
            cn.setText(mProject.getCourseNumber());
            in.setText(mProject.getInstructorName());
            pn.setText(mProject.getProjectNumber());
            pd.setText(mProject.getProjectDescription());
            c.setChecked(mProject.getCompleted());
            String dueDate = mProject.getDue();
            try {
                myCalendar.setTime(new SimpleDateFormat(getString(R.string.dateFormat), Locale.CANADA).parse(dueDate));
                updateLabel();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return rootView;
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(year, month, dayOfMonth);
            updateLabel();
        }
    };

    private void updateLabel() {
        String format = getString(R.string.dateFormat);
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CANADA);
        mDatePick.setText(sdf.format(myCalendar.getTime()));
        Calendar cur = Calendar.getInstance();
        long diff = myCalendar.getTimeInMillis() - cur.getTimeInMillis();
        long diffDays = diff / (24 * 1000 * 60 * 60);
        long diffHours = diff / (60 * 60 * 1000) - diffDays * 24;
        String result = MessageFormat.format(getString(R.string.deadlineNotification),diffDays, diffHours);
        if (diff < 0)
            result = getString(R.string.passDue);
        else if (diffDays>0&&diffDays<2)
            tvLeft.setTextColor(Color.RED);
        tvLeft.setText(result);
    }
}
