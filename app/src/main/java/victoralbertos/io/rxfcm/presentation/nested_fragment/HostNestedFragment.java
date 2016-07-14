package victoralbertos.io.rxfcm.presentation.nested_fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import victoralbertos.io.rxfcm.R;

/**
 * Created by victor on 12/05/16.
 */
public class HostNestedFragment extends Fragment {

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.host_nested_fragment, container, false);
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.fl_fragment_container, new FragmentNestedSupplies())
                .commit();
    }
}
