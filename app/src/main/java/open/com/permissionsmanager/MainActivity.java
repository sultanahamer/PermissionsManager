package open.com.permissionsmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import static android.view.View.GONE;


public class MainActivity extends AppCompatActivity implements ApplicationDatabaseChangeListener {
    public static final String APPLICATION_PACKAGE_NAME = "APPLICATION_PACKAGE_NAME";
    private ApplicationsDatabase applicationsDatabase;
    private List<AndroidApplication> applications;
    private ListView listOfApplications_listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAlarmIfNotSet();
        setContentView(R.layout.activity_main);
        listOfApplications_listView = (ListView) findViewById(R.id.listOfApplications);
        listOfApplications_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentToShowApplicationDetails = new Intent(MainActivity.this, ApplicationDetails.class);
                intentToShowApplicationDetails.putExtra(APPLICATION_PACKAGE_NAME, ((ApplicationsArrayAdapter)parent.getAdapter()).getItem(position).getPackageName());
                startActivity(intentToShowApplicationDetails);
            }
        });
        applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(this);
        applicationsDatabase.addApplicationDatabaseChangeListener(this);
        updateApplicationsList();
        ApplicationsArrayAdapter adapter = new ApplicationsArrayAdapter(MainActivity.this, R.layout.application_info_row);
        listOfApplications_listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        menu.findItem(R.id.refresh).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                updateApplicationsList();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void setAlarmIfNotSet() {
        SharedPreferences sharedPreferences = Utils.getSharedPreferences(this);
        String alarm_set_key = getString(R.string.alarm_set);
        if(!sharedPreferences.contains(alarm_set_key)){
            Utils.setAlarm(this);
            sharedPreferences.edit().putBoolean(alarm_set_key, true).apply();
        }
    }

    private void showSpinner() {
        findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        findViewById(R.id.listOfApplications).setVisibility(GONE);
    }

    private void updateApplicationsList() {
        showSpinner();
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                applicationsDatabase.updateApplicationsDatabase();
                return null;
            }
        }.execute();
    }

    private void hideSpinner() {
        findViewById(R.id.progressbar).setVisibility(GONE);
        findViewById(R.id.listOfApplications).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        applicationsDatabase.removeApplicationDatabaseChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void applicationPermissionsUpdated(final AndroidApplication androidApplication) {
        final ApplicationsArrayAdapter adapter = (ApplicationsArrayAdapter) listOfApplications_listView.getAdapter();
        if(adapter == null)
            return;
        final int indexOfApplication = applications.indexOf(androidApplication);
        if(indexOfApplication == -1)
            return;
        applications.remove(indexOfApplication);
        applications.add(indexOfApplication, androidApplication);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.replaceItemAt(indexOfApplication, androidApplication);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateView() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ApplicationsArrayAdapter adapter = (ApplicationsArrayAdapter) listOfApplications_listView.getAdapter();
                showSpinner();
                adapter.addAllApplications(applications);
                adapter.notifyDataSetChanged();
                getSupportActionBar().setSubtitle(getString(R.string.apps_with_warnings_count) + adapter.getCount());
                hideSpinner();
            }
        });
    }

    @Override
    public void applicationsDatabaseUpdated(List<AndroidApplication> androidApplications) {
        applications = androidApplications;
        ApplicationsArrayAdapter adapter = (ApplicationsArrayAdapter) listOfApplications_listView.getAdapter();
        if(adapter == null)
            return;
        Utils.sort(applications);
        updateView();
    }
}
