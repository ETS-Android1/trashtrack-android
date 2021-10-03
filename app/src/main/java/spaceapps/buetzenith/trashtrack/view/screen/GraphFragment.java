package spaceapps.buetzenith.trashtrack.view.screen;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.TransitionInflater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.R;
import spaceapps.buetzenith.trashtrack.databinding.FragmentGraphBinding;
import spaceapps.buetzenith.trashtrack.service.model.Satellite;
import spaceapps.buetzenith.trashtrack.view.MainActivity;
import spaceapps.buetzenith.trashtrack.view.screen.googlemap.DeviceLocationFinder;


public class GraphFragment extends Fragment {

    MainActivity mainActivity;

    @Inject
    DeviceLocationFinder deviceLocationFinder;

    FragmentGraphBinding mVB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) this.getActivity();
        if (mainActivity.activityComponent == null)
            mainActivity.initActivityComponent();
        mainActivity.activityComponent.inject(this);

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        super.setEnterTransition(inflater.inflateTransition(R.transition.slide_in_from_right));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //data
        //if(!mainActivity.activeSatCode.equals("moon"))
       // satelliteData = mainActivity.allSatelliteData.get(mainActivity.activeSatCode);

        // Inflate the layout for this fragment
        mVB = FragmentGraphBinding.inflate(inflater, container, false);

        return mVB.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initGraph();

        mVB.backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });

        deviceLocationFinder.requestDeviceLocation(latLng -> {
            mVB.latTv.setText("Latitude: "+latLng.latitude);
            mVB.longTv.setText("Longitude: "+latLng.longitude);
        });
    }

    private void initGraph(){
        ArrayList<Entry> dataValues = new ArrayList<>();
        int max = 100;
        for(int i = 0; i < 24; i++) {
            dataValues.add(new Entry(i, (int)(Math.random()*max)));
        }

        LineDataSet lineDataSet = new LineDataSet(dataValues, "Number of Debris");
        lineDataSet.setValueTextColor(Color.WHITE);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        LineData data = new LineData(dataSets);
        data.setValueTextColor(Color.WHITE);
        mVB.lineChart.setData(data);
        mVB.lineChart.invalidate();

        mVB.lineChart.getDescription().setText("Hour");
        mVB.lineChart.getDescription().setTextColor(Color.WHITE);

        mVB.lineChart.getXAxis().setTextColor(Color.WHITE);
        mVB.lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        mVB.lineChart.getAxisLeft().setTextColor(Color.WHITE);
        mVB.lineChart.getAxisRight().setEnabled(false);
    }
}