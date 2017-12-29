package open.com.permissionsmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent serviceStartIntent = new Intent(getApplicationContext(), PermissionValidationService.class);
        startService(serviceStartIntent);
        ApplicationsDatabase applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(this);
        ListView listOfApplications = (ListView) findViewById(R.id.listOfApplications);
        ArrayAdapter<AndroidApplication> arrayAdapter = new ArrayAdapter<AndroidApplication>(this, R.layout.support_simple_spinner_dropdown_item);
        for(AndroidApplication application : applicationsDatabase.applications)
            arrayAdapter.add(application);
        listOfApplications.setAdapter(arrayAdapter);
    }
}
