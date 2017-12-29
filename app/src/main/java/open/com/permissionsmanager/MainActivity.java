package open.com.permissionsmanager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent serviceStartIntent = new Intent(getApplicationContext(), PermissionValidationService.class);
        startService(serviceStartIntent);
        ApplicationsDatabase applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(this);
        ListView listOfApplications = (ListView) findViewById(R.id.listOfApplications);
        ArrayAdapter<AndroidApplication> arrayAdapter = new ArrayAdapter<AndroidApplication>(this, R.layout.application_info_row){
            private LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);

            @NonNull
            @Override
            public View getView(int position, View reusableView, ViewGroup parent) {
                AndroidApplication androidApplication = getItem(position);
                if(reusableView == null)
                    reusableView = layoutInflater.inflate(R.layout.application_info_row, parent, false);
                TextView applicationName = (TextView) reusableView.findViewById(R.id.application_name);
                applicationName.setText(androidApplication.getName());
                TextView warningCount = (TextView) reusableView.findViewById(R.id.warning_count_text);
                warningCount.setText(String.valueOf(androidApplication.getWarnings()));
                return reusableView;
            }
        };
        for(AndroidApplication application : applicationsDatabase.applications)
            arrayAdapter.add(application);
        listOfApplications.setAdapter(arrayAdapter);
    }
}
