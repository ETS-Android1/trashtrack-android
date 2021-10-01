package spaceapps.buetzenith.trashtrack.view.screen;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.R;
import spaceapps.buetzenith.trashtrack.databinding.FragmentHomeBinding;
import spaceapps.buetzenith.trashtrack.utils.DebrisCatalog;
import spaceapps.buetzenith.trashtrack.view.MainActivity;
import spaceapps.buetzenith.trashtrack.view.adapter.DebrisListAdapter;


public class HomeFragment extends Fragment {

    private  MainActivity mainActivity;

    // views
    private FragmentHomeBinding mVB;

    @Inject
    DebrisListAdapter debrisListAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) this.getActivity();
        if (mainActivity.activityComponent == null)
            mainActivity.initActivityComponent();
        mainActivity.activityComponent.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment)
        mVB = FragmentHomeBinding.inflate(inflater, container, false);
        return mVB.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        initAdapter();
    }

    private void initAdapter() {
        mVB.debrisCatalog.setAdapter(debrisListAdapter);
        debrisListAdapter.setDebrisList(DebrisCatalog.debrisList);

        debrisListAdapter.setOnClickListener((debris, fragmentId) -> {
            if(fragmentId == R.id.googleMapFragment){

                Bundle bundle = new Bundle();
                bundle.putString("debris", debris.toString());

                NavHostFragment.findNavController(this)
                        .navigate(fragmentId, bundle);
            }
        });
    }


}