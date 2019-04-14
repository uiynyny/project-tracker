package com.csi5175.projecttracker.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.csi5175.projecttracker.Entity.Project;
import com.csi5175.projecttracker.Entity.ProjectManager;
import com.csi5175.projecttracker.R;
import com.csi5175.projecttracker.recyclerViewHelper.SimpleItemRecyclerViewAdapter;
import com.csi5175.projecttracker.recyclerViewHelper.SwipeToDeleteCallback;
import com.csi5175.projecttracker.awsUtility.AwsClient;
import com.csi5175.projecttracker.awsUtility.AwsConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static java.lang.Math.max;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private static final String TAG = ItemListActivity.class.getSimpleName();
    private boolean mTwoPane;
    RecyclerView recyclerView;
    FrameLayout layout;
    TextView loading;
    private int checked = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        ProjectManager.getInstance();
        initLayout();
        new AwsClient(getApplicationContext());
        new RetriveTask().execute("list");
        enableSwipeToDeleteAndUndo();
    }

    private void initLayout() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, ItemDetailActivity.class);
                context.startActivity(intent);
            }
        });
        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
        }
        recyclerView = findViewById(R.id.item_list);
        layout = findViewById(R.id.frameLayout);
        if (ProjectManager.getInstance().getProjects() == null) {
            initLoadingText();
        } else {
            setupRecyclerView(ProjectManager.getInstance().getProjects());
        }
    }

    private void initLoadingText() {
        loading = new TextView(this);
        loading.setGravity(Gravity.CENTER);
        loading.setText(getString(R.string.Loading));
        layout.addView(loading);
    }

    private void setupRecyclerView(List<Project> p) {
        if (layout.getChildCount() == 2) {
            layout.removeView(loading);
            ShowAlertDialogWithListview();
        }
        recyclerView.removeAllViews();
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, p, mTwoPane));
        if (recyclerView.getItemDecorationCount() < 2) {
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), LinearLayout.VERTICAL));
        }
    }

    public void ShowAlertDialogWithListview() {
        List<String> projects = new ArrayList<>();

        for (Project p : ProjectManager.getInstance().getProjects()) {
            try {
                long cur = Calendar.getInstance().getTimeInMillis();
                long due = new SimpleDateFormat(getString(R.string.dateFormat), Locale.CANADA).parse(p.getDue()).getTime();
                long diff = (due - cur) / (1000 * 60 * 60);
                if (diff < 48 && diff > 0 && !p.getCompleted()) {
                    projects.add(p.getCourseTitle() + " " + p.getProjectNumber());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        final CharSequence[] p = projects.toArray(new String[projects.size()]);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        if (projects.isEmpty()) return;
        dialogBuilder.setTitle("Projects due in next two days");
        dialogBuilder.setItems(p, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String selectedText = p[item].toString();  //Selected item in listview
            }
        });
        //Create alert dialog object via builder
        AlertDialog alertDialogObject = dialogBuilder.create();
        //Show the dialog
        alertDialogObject.show();
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                super.onSwiped(viewHolder, i);
                final int position = viewHolder.getAdapterPosition();
                final SimpleItemRecyclerViewAdapter adapter = (SimpleItemRecyclerViewAdapter) recyclerView.getAdapter();
                final Project item = adapter.getProjects().get(position);
                adapter.removeItem(position);
                new RetriveTask().execute("remove", item.getUid());
                Snackbar.make(findViewById(R.id.root_view), "Item was removed", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                adapter.restoreItem(item, position);
                                recyclerView.scrollToPosition(position);
                                new RetriveTask().execute("restore", item.getUid(), item.toJson(getApplicationContext()));
                            }
                        }).setActionTextColor(Color.YELLOW).show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(getString(R.string.about));
                alertDialog.setMessage(AwsConstant.info.getContent());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
            case R.id.filter:
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle("Filter Options");
                dialogBuilder.setSingleChoiceItems(R.array.options, checked, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checked=which;
                        setupRecyclerView(ProjectManager.getInstance().getFilteredProjects(which));
                    }
                });
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialogBuilder.create().show();
        }
        return false;
    }

    class RetriveTask extends AsyncTask<String, Void, List<String>> {


        @Override
        protected List<String> doInBackground(String... strings) {
            // Create a client
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    AwsConstant.IdentityPoolId.getContent(), // Identity pool ID
                    Regions.US_EAST_1 // Region
            );
            AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider, Region.getRegion(Regions.US_EAST_1));
            switch (strings[0]) {
                case "list":
                    List<String> files = new ArrayList<>();
                    for (S3ObjectSummary sum : S3Objects.withPrefix(s3Client, AwsConstant.bucket.getContent(), AwsConstant.dir.getContent())) {
                        Log.i(TAG, sum.getKey());
                        S3Object s3Object = s3Client.getObject(AwsConstant.bucket.getContent(), sum.getKey());
                        StringBuilder result = new StringBuilder();
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
                            String line;
                            while (null != (line = reader.readLine())) {
                                Log.i(TAG, line);
                                result.append(line);
                            }
                            files.add(String.valueOf(result));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return files;
                case "remove":
                    s3Client.deleteObject(AwsConstant.bucket.getContent(), AwsConstant.dir.getContent() + "_" + strings[1]);
                    break;
                case "restore":
                    s3Client.putObject(AwsConstant.bucket.getContent(), AwsConstant.dir.getContent() + "_" + strings[1], strings[2]);
                    break;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            super.onPostExecute(s);
            List<Project> projects = new ArrayList<>();
            if (s == null) return;
            for (String item : s) {
                try {
                    JSONObject jsonObject = new JSONObject(item);
                    Project project = getProject(jsonObject);
                    projects.add(project);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            ProjectManager.getInstance().setProjects(projects);
            setupRecyclerView(ProjectManager.getInstance().getProjects());
        }

        private Project getProject(@NonNull JSONObject jsonObject) throws JSONException {
            Project project = new Project();
            project.setCourseTitle(jsonObject.getString(getString(R.string.course_title)));
            project.setCourseNumber(jsonObject.getString(getString(R.string.course_number)));
            project.setInstructorName(jsonObject.getString(getString(R.string.instructor_name)));
            project.setProjectNumber(jsonObject.getString(getString(R.string.project_number)));
            project.setProjectDescription(jsonObject.getString(getString(R.string.project_description)));
            project.setDue(jsonObject.getString(getString(R.string.dueDate)));
            project.setComplete(jsonObject.getBoolean(getString(R.string.complete)));
            project.setUid(jsonObject.getString(getString(R.string.project_uid)));
            return project;
        }
    }
}
