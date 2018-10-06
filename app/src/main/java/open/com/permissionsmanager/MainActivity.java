package open.com.permissionsmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static android.view.View.GONE;
import static open.com.permissionsmanager.ApplicationDetails.APPLICATION_INDEX;


public class MainActivity extends AppCompatActivity {
    private ApplicationsDatabase applicationsDatabase;
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
                intentToShowApplicationDetails.putExtra(APPLICATION_INDEX, position);
                startActivity(intentToShowApplicationDetails);
            }
        });
    }

    private void setAlarmIfNotSet() {
        SharedPreferences sharedPreferences = Utils.getSharedPreferences(this);
        String alarm_set_key = getString(R.string.alarm_set);
        if(!sharedPreferences.contains(alarm_set_key)){
            Utils.setAlarm(this);
            sharedPreferences.edit().putBoolean(alarm_set_key, true).apply();
        }
    }

    private void getApplicationsDatabase() {
        applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSpinner();
        updateApplicationsList();
    }

    private void showSpinner() {
        findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
        findViewById(R.id.listOfApplications).setVisibility(GONE);
    }

    private void updateApplicationsList() {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                if(applicationsDatabase == null)
                    getApplicationsDatabase();
                applicationsDatabase.updateApplicationsDatabase();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ApplicationsArrayAdapter adapter = (ApplicationsArrayAdapter) listOfApplications_listView.getAdapter();
                if(adapter == null){
                    adapter = new ApplicationsArrayAdapter(MainActivity.this, R.layout.application_info_row);
                    listOfApplications_listView.setAdapter(adapter);
                }
                adapter.addAllApplications(applicationsDatabase.getACopyOfApplications());
                adapter.notifyDataSetChanged();
                getSupportActionBar().setSubtitle(getString(R.string.apps_with_warnings_count) + adapter.getCount());
                hideSpinner();
            }
        }.execute();
    }

    private void hideSpinner() {
        findViewById(R.id.progressbar).setVisibility(GONE);
        findViewById(R.id.listOfApplications).setVisibility(View.VISIBLE);
    }
}
