package open.com.permissionsmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static open.com.permissionsmanager.ApplicationDetails.APPLICATION_INDEX;


public class MainActivity extends AppCompatActivity {
    ApplicationsDatabase applicationsDatabase;
    ListView listOfApplications_listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent serviceStartIntent = new Intent(getApplicationContext(), PermissionValidationService.class);
//        startService(serviceStartIntent);
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

    private void getApplicationsDatabase() {
        applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(applicationsDatabase == null)
            getApplicationsDatabase();
        applicationsDatabase.updateApplicationsDatabase();
        ApplicationsArrayAdapter adapter = (ApplicationsArrayAdapter) listOfApplications_listView.getAdapter();
        if(adapter == null){
            adapter = new ApplicationsArrayAdapter(this, R.layout.application_info_row);
            listOfApplications_listView.setAdapter(adapter);
        }
        adapter.addAllApplications(applicationsDatabase.applications);
        adapter.notifyDataSetChanged();
    }
}
