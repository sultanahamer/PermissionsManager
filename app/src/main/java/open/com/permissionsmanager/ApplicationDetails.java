package open.com.permissionsmanager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ApplicationDetails extends AppCompatActivity {
    AndroidApplication application;
    LayoutInflater layoutInflater;
    public static final String APPLICATION_INDEX = "APPLICATION_INDEX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutInflater = getLayoutInflater();
        setContentView(R.layout.activity_application_details);
        Intent intent = getIntent();
        int applicationIndex = intent.getIntExtra(APPLICATION_INDEX, -1);
        if(applicationIndex == -1)
            finish();
        application = ApplicationsDatabase.getApplicationsDatabase(this).applications.get(applicationIndex);
        addApplicationDetails();
        ListView permissionsList_listView = (ListView) findViewById(R.id.permissions);
        permissionsList_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApplicationsDatabase.getApplicationsDatabase(ApplicationDetails.this).ignorePermission(application.getPermissions().get(position));
            }
        });
    }

    private void addApplicationDetails() {
        setTitle(application.getName());
        ListView permissionsList_listView = (ListView) findViewById(R.id.permissions);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, R.layout.application_info_row){
            @NonNull
            @Override
            public View getView(int position, View reusableView, ViewGroup parent) {
                String permission = getItem(position);
                if(reusableView == null)
                    reusableView = layoutInflater.inflate(R.layout.application_info_row, parent, false);
                TextView permission_textView = (TextView) reusableView.findViewById(R.id.title);
                ImageView warningImage = (ImageView) reusableView.findViewById(R.id.warning_image);
                if(ApplicationsDatabase.getApplicationsDatabase(ApplicationDetails.this).allowedPermissions.containsKey(permission))
                    warningImage.setVisibility(View.INVISIBLE);
                permission_textView.setText(permission);
                return reusableView;
            }
        };
        for(String permission : application.getPermissions())
            arrayAdapter.add(permission);
        permissionsList_listView.setAdapter(arrayAdapter);
    }
}
