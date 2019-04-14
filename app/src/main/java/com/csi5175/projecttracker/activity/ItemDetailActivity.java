package com.csi5175.projecttracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.util.Mimetypes;
import com.csi5175.projecttracker.fragment.ItemDetailFragment;
import com.csi5175.projecttracker.R;
import com.csi5175.projecttracker.awsUtility.AwsClient;
import com.csi5175.projecttracker.awsUtility.AwsConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.UUID;

import static java.lang.Math.max;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 */
public class ItemDetailActivity extends AppCompatActivity {

    private static final String TAG = ItemListActivity.class.getSimpleName();
    private static final int WRITE_REQUEST_CODE = 3;
    private static final int READ_REQUEST_CODE = 2;
    private String projectUid = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Synced to the cloud", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    saveToS3();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            ItemDetailFragment fragment = new ItemDetailFragment();
            if (getIntent().hasExtra(ItemDetailFragment.ARG_ITEM_ID)) {
                projectUid = getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID);
                Bundle arguments = new Bundle();
                arguments.putString(ItemDetailFragment.ARG_ITEM_ID, projectUid);
                fragment.setArguments(arguments);
            } else if (getIntent().getExtras() != null) {
                Bundle args = getBundle();
                fragment.setArguments(args);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_detail_container, fragment)
                    .commit();
        }
    }

    private Bundle getBundle() {
        Bundle args = new Bundle();
        args.putString(getString(R.string.course_number), getIntent().getStringExtra(getString(R.string.course_number)));
        args.putString(getString(R.string.course_title), getIntent().getStringExtra(getString(R.string.course_title)));
        args.putString(getString(R.string.project_uid), getIntent().getStringExtra(getString(R.string.project_uid)));
        args.putString(getString(R.string.project_number), getIntent().getStringExtra(getString(R.string.project_number)));
        args.putString(getString(R.string.project_description), getIntent().getStringExtra(getString(R.string.project_description)));
        args.putString(getString(R.string.dueDate), getIntent().getStringExtra(getString(R.string.dueDate)));
        args.putString(getString(R.string.instructor_name), getIntent().getStringExtra(getString(R.string.instructor_name)));
        args.putBoolean(getString(R.string.complete), getIntent().getBooleanExtra(getString(R.string.complete), false));
        return args;
    }

    private void saveToS3() {
        JSONObject jsonObject = getJsonObject();
        final String filename = AwsConstant.dir.getContent() + "_" + projectUid;
        new SaveTask(this).execute(filename, jsonObject.toString());
    }

    private JSONObject getJsonObject() {
        EditText pn, cn, ct, pd, in;
        Button dd;
        Switch c;
        pn = findViewById(R.id.etProjectNumber);
        cn = findViewById(R.id.etCourseNumber);
        ct = findViewById(R.id.etCourseTitle);
        pd = findViewById(R.id.etDescription);
        in = findViewById(R.id.etInstructorName);
        dd = findViewById(R.id.date_pick);
        c = findViewById(R.id.complete);

        if (TextUtils.isEmpty(projectUid))
            projectUid = UUID.randomUUID().toString();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(getString(R.string.course_title), ct.getText());
            jsonObject.put(getString(R.string.course_number), cn.getText());
            jsonObject.put(getString(R.string.project_number), pn.getText());
            jsonObject.put(getString(R.string.project_description), pd.getText());
            jsonObject.put(getString(R.string.instructor_name), in.getText());
            jsonObject.put(getString(R.string.dueDate), dd.getText());
            jsonObject.put(getString(R.string.complete), c.isChecked());
            jsonObject.put(getString(R.string.project_uid), projectUid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_itemdetail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpTo(this, new Intent(this, ItemListActivity.class));
                return true;
            case R.id.action_import:
                Intent imIntent = new Intent(Intent.ACTION_GET_CONTENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType(Mimetypes.MIMETYPE_HTML);
                startActivityForResult(imIntent, READ_REQUEST_CODE);
                break;
            case R.id.action_export:
                Intent ouIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType(Mimetypes.MIMETYPE_HTML);
                startActivityForResult(ouIntent, WRITE_REQUEST_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            JSONObject jsonObject = getJsonObject();
            if (data != null) {
                Uri uri = data.getData();
                Log.i(TAG, "Uri:" + uri.toString());
                try {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    os.write(jsonObject.toString().getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                Log.i(TAG, "Uri:" + uri.toString());
                try {
                    InputStreamReader isr = new InputStreamReader(getContentResolver().openInputStream(uri));
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    JSONObject jsonObject = new JSONObject(sb.toString());
                    importedProject(jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void importedProject(JSONObject jsonObject) throws JSONException {
        Intent projectIntent = new Intent(getApplicationContext(), ItemDetailActivity.class);
        projectIntent.putExtra(getString(R.string.course_title), jsonObject.getString(getString(R.string.course_title)));
        projectIntent.putExtra(getString(R.string.course_number), jsonObject.getString(getString(R.string.course_number)));
        projectIntent.putExtra(getString(R.string.project_number), jsonObject.getString(getString(R.string.project_number)));
        projectIntent.putExtra(getString(R.string.project_description), jsonObject.getString(getString(R.string.project_description)));
        projectIntent.putExtra(getString(R.string.project_uid), jsonObject.getString(getString(R.string.project_uid)));
        projectIntent.putExtra(getString(R.string.instructor_name), jsonObject.getString(getString(R.string.instructor_name)));
        projectIntent.putExtra(getString(R.string.dueDate), jsonObject.getString(getString(R.string.dueDate)));
        projectIntent.putExtra(getString(R.string.complete), jsonObject.getBoolean(getString(R.string.complete)));
        startActivity(projectIntent);
    }

    private static class SaveTask extends AsyncTask<String, Void, Void> {

        private final WeakReference<Activity> weakActivity;

        SaveTask(Activity activity) {
            this.weakActivity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(String... strings) {
            AmazonS3Client s3Client = new AwsClient(weakActivity.get().getApplicationContext()).getS3Client();
            s3Client.putObject(AwsConstant.bucket.getContent(), strings[0], strings[1]);
            return null;
        }
    }
}
