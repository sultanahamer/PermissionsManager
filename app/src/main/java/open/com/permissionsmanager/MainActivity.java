package open.com.permissionsmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements ApplicationDatabaseChangeListener {
    public static final String APPLICATION_PACKAGE_NAME = "APPLICATION_PACKAGE_NAME";
    private ApplicationsDatabase applicationsDatabase;
    private List<AndroidApplication> warnableApplications, ignoredApplications;
    private ListView listOfApplications_listView, ignoreListOfApplications_listView;
    private AppCompatTextView warnableAppsToggle;
    private AppCompatTextView ignoredAppsToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAlarmIfNotSet();
        setContentView(R.layout.activity_main);
        applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(this);
        applicationsDatabase.addApplicationDatabaseChangeListener(this);
        setupListViewsAndToggles();
        if(!applicationsDatabase.isScanInProgress())
            applicationsDatabaseUpdated(applicationsDatabase.getACopyOfApplications());
        else
            showSpinner();
    }

    private void setupListViewsAndToggles() {
        listOfApplications_listView = (ListView) findViewById(R.id.list_apps);
        ignoreListOfApplications_listView = (ListView) findViewById(R.id.list_ignored_apps);
        warnableAppsToggle = (AppCompatTextView) findViewById(R.id.warnable_apps_toggle);
        ignoredAppsToggle = (AppCompatTextView) findViewById(R.id.ignored_apps_toggle);

        warnableAppsToggle.setOnClickListener(getToggleClickListener(listOfApplications_listView, warnableAppsToggle));
        ignoredAppsToggle.setOnClickListener(getToggleClickListener(ignoreListOfApplications_listView, ignoredAppsToggle));

        AdapterView.OnItemClickListener onAppClick = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intentToShowApplicationDetails = new Intent(MainActivity.this, ApplicationDetails.class);
                intentToShowApplicationDetails.putExtra(APPLICATION_PACKAGE_NAME, ((ApplicationsArrayAdapter) parent.getAdapter()).getItem(position).getPackageName());
                startActivity(intentToShowApplicationDetails);
            }
        };

        listOfApplications_listView.setOnItemClickListener(onAppClick);

        listOfApplications_listView.setOnItemLongClickListener(getAppLongClickListener(true));

        listOfApplications_listView.setAdapter(new ApplicationsArrayAdapter(MainActivity.this, R.layout.application_info_row));

        ignoreListOfApplications_listView.setOnItemClickListener(onAppClick);

        ignoreListOfApplications_listView.setOnItemLongClickListener(getAppLongClickListener(false));

        ignoreListOfApplications_listView.setAdapter(new ApplicationsArrayAdapter(MainActivity.this, R.layout.application_info_row));
    }

    @NonNull
    private View.OnClickListener getToggleClickListener(final ListView listView, final AppCompatTextView appCompatTextView) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleList(listView, appCompatTextView);
            }
        };
    }

    @NonNull
    private AdapterView.OnItemLongClickListener getAppLongClickListener(final boolean isWarnableAppsList) {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(isWarnableAppsList ? R.string.add_to_temporary_ignore_list : R.string.stop_ignoring)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(isWarnableAppsList)
                                    applicationsDatabase.addAppToIgnoreList(((ApplicationsArrayAdapter)parent.getAdapter()).getItem(position));
                                else
                                    applicationsDatabase.removeAppFromIgnoreList(((ApplicationsArrayAdapter)parent.getAdapter()).getItem(position));
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        };
    }

    private void toggleList(ListView listView, AppCompatTextView toggle) {
        if(listView.getVisibility() == View.VISIBLE){
            listView.setVisibility(View.GONE);
            toggle.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_keyboard_arrow_down_24dp), null);
        }
        else{
            listView.setVisibility(View.VISIBLE);
            toggle.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_keyboard_arrow_up_24dp), null);
        }
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
        listOfApplications_listView.setVisibility(View.GONE);
        ignoreListOfApplications_listView.setVisibility(View.GONE);
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
        findViewById(R.id.progressbar).setVisibility(View.GONE);
        listOfApplications_listView.setVisibility(View.VISIBLE);
        ignoreListOfApplications_listView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        applicationsDatabase.removeApplicationDatabaseChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void applicationPermissionsUpdated(final AndroidApplication androidApplication) {
        final ApplicationsArrayAdapter warnableApplicationsListAdapter = (ApplicationsArrayAdapter) listOfApplications_listView.getAdapter();
        final ApplicationsArrayAdapter ignoredApplicationsListAdapter = (ApplicationsArrayAdapter) ignoreListOfApplications_listView.getAdapter();
        if(warnableApplicationsListAdapter == null && ignoredApplicationsListAdapter == null)
            return;
        if(androidApplication.isIgnoredTemporarily())
            updateAdapterWithNewApplication(androidApplication, ignoredApplicationsListAdapter);
        else
            updateAdapterWithNewApplication(androidApplication, warnableApplicationsListAdapter);
    }

    private void updateAdapterWithNewApplication(final AndroidApplication androidApplication, final ApplicationsArrayAdapter listAdapter) {

        final int indexOfApplication = listAdapter.getPosition(androidApplication);
        if(indexOfApplication == -1)
            return;
        warnableApplications.remove(indexOfApplication);
        warnableApplications.add(indexOfApplication, androidApplication);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.replaceItemAt(indexOfApplication, androidApplication);
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateView() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showSpinner();
                ApplicationsArrayAdapter warnableAppsAdapter = (ApplicationsArrayAdapter) listOfApplications_listView.getAdapter();
                ApplicationsArrayAdapter ignoredAppsAdapter = (ApplicationsArrayAdapter) ignoreListOfApplications_listView.getAdapter();
                updateListWithApplications(warnableAppsAdapter, warnableApplications);
                updateListWithApplications(ignoredAppsAdapter, ignoredApplications);
                getSupportActionBar().setSubtitle(getString(R.string.apps_with_warnings_count) + warnableAppsAdapter.getCount());
                hideSpinner();
            }
        });
    }

    private void updateListWithApplications(ApplicationsArrayAdapter adapter, List<AndroidApplication> androidApplications) {
        adapter.addAllApplications(androidApplications);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void applicationsDatabaseUpdated(List<AndroidApplication> androidApplications) {
        ApplicationsArrayAdapter warnableAppsListAdapter = (ApplicationsArrayAdapter) listOfApplications_listView.getAdapter();
        ApplicationsArrayAdapter ignoredAppsListAdapter = (ApplicationsArrayAdapter) listOfApplications_listView.getAdapter();
        if(warnableAppsListAdapter == null && ignoredAppsListAdapter == null)
            return;
        Utils.sort(androidApplications);
        warnableApplications = new ArrayList<>();
        ignoredApplications = new ArrayList<>();
        for(AndroidApplication application: androidApplications){
            if(application.isIgnoredTemporarily())
                ignoredApplications.add(application);
            else
                warnableApplications.add(application);
        }
        updateView();
    }

    @Override
    public void applicationAddedToIgnoreList(final AndroidApplication application) {
        warnableApplications.remove(application);
        ignoredApplications.add(application);
        updateView();
    }

    @Override
    public void applicationRemovedFromIgnoredList(final AndroidApplication application) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                warnableApplications.add(application);
                ApplicationsArrayAdapter warnableApplicationsAdapter = (ApplicationsArrayAdapter) listOfApplications_listView.getAdapter();
                warnableApplicationsAdapter.add(application);
                warnableApplicationsAdapter.notifyDataSetChanged();

                ignoredApplications.remove(application);
                ApplicationsArrayAdapter ignoredApplicationsAdapter = (ApplicationsArrayAdapter) ignoreListOfApplications_listView.getAdapter();
                ignoredApplicationsAdapter.remove(application);
                ignoredApplicationsAdapter.notifyDataSetChanged();
            }
        });
    }
}
