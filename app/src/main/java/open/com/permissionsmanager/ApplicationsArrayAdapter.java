package open.com.permissionsmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sultanm on 12/29/17.
 */

public class ApplicationsArrayAdapter extends ArrayAdapter<AndroidApplication> {
    private LayoutInflater layoutInflater;

    public ApplicationsArrayAdapter(Context context, int resource) {
        super(context, resource);
        layoutInflater = LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public View getView(int position, View reusableView, ViewGroup parent) {
        AndroidApplication androidApplication = getItem(position);
        if(reusableView == null)
            reusableView = layoutInflater.inflate(R.layout.application_info_row, parent, false);
        TextView applicationName = (TextView) reusableView.findViewById(R.id.title);
        applicationName.setText(androidApplication.getName());
        TextView warningCount = (TextView) reusableView.findViewById(R.id.warning_count_text);
        warningCount.setText(String.valueOf(androidApplication.getWarnablePermissionIndexes().size()));
        return reusableView;
    }

    public void addAllApplications(List<AndroidApplication> applications){
        clear();
        for(AndroidApplication application : applications)
            add(application);
    }
}
