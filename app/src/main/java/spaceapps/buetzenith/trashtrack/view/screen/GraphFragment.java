package spaceapps.buetzenith.trashtrack.view.screen;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.TransitionInflater;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.R;
import spaceapps.buetzenith.trashtrack.databinding.FragmentGraphBinding;
import spaceapps.buetzenith.trashtrack.utils.RiskAnalysis;
import spaceapps.buetzenith.trashtrack.view.MainActivity;
import spaceapps.buetzenith.trashtrack.view.screen.googlemap.DeviceLocationFinder;
import spaceapps.buetzenith.trashtrack.viewModel.CelestrackViewModel;


public class GraphFragment extends Fragment {
    private static final String TAG = "GraphFragment";

    MainActivity mainActivity;
    @Inject
    DeviceLocationFinder deviceLocationFinder;

    @Inject
    CelestrackViewModel celestrackViewModel;

    FragmentGraphBinding mVB;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) this.getActivity();
        if (mainActivity.activityComponent == null)
            mainActivity.initDaggerActivityComponent();
        mainActivity.activityComponent.inject(this);

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        super.setEnterTransition(inflater.inflateTransition(R.transition.slide_in_from_right));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mVB = FragmentGraphBinding.inflate(inflater, container, false);
        return mVB.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mVB.lineChart.setVisibility(View.INVISIBLE);
        mVB.analysisLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mVB.lineChart.setVisibility(View.INVISIBLE);
        mVB.analysisLoading.setVisibility(View.VISIBLE);

        mVB.numberOfHourEdt.setText("6");

        mVB.altTv.setText("500");

        deviceLocationFinder.requestDeviceLocation(latLng -> {
            int hour = 6;
            int alt = 500;
            try{
                hour = Integer.parseInt(Objects.requireNonNull(mVB.numberOfHourEdt.getText()).toString());
                alt = Integer.parseInt(Objects.requireNonNull(mVB.altTv.getText()).toString());
            }catch (Exception e){
                Log.e(TAG, "onViewCreated: value must be int");
            }

            RiskAnalysis riskAnalysis = new RiskAnalysis(latLng, alt, 500, hour);
            riskAnalysis.setOnAnalysisFinish(map -> mainActivity.runOnUiThread(()->{
                initGraph(map);
            }));
            celestrackViewModel.getAllDebrisPiecesData()
                    .observe(getViewLifecycleOwner(), riskAnalysis::countInside);

            mVB.analyzeBtn.setOnClickListener(v -> {
                mVB.lineChart.setVisibility(View.INVISIBLE);
                mVB.analysisLoading.setVisibility(View.VISIBLE);
                celestrackViewModel.getAllDebrisPiecesData()
                        .observe(getViewLifecycleOwner(), riskAnalysis::countInside);
            });
        });

        mVB.backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });

        deviceLocationFinder.requestDeviceLocation(latLng -> {
            mVB.latTv.setText(""+latLng.latitude);
            mVB.longTv.setText(""+latLng.longitude);
        });


    }

    /*private void initGraph(Map<Integer, Integer> map){
        ArrayList<BarEntry> dataValues = new ArrayList<>();
        *//*for (int i = 0; i < 6; i++) {
            dataValues.add(new Entry(i, map.get(i)));
        }*//*
        for (Map.Entry<Integer, Integer> entry :
                map.entrySet()) {
            Log.d(TAG, "initGraph: hour: "+entry.getKey()+" , cnt: "+entry.getKey());
            dataValues.add(new BarEntry(entry.getKey(), entry.getValue()));
        }

        BarDataSet lineDataSet = new BarDataSet(dataValues, "Number of Debris");
        lineDataSet.setValueTextColor(Color.WHITE);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        BarData data = new BarData(dataSets);
        data.setValueTextColor(Color.WHITE);
        mVB.lineChart.setData(data);
        mVB.lineChart.invalidate();

        mVB.lineChart.getDescription().setText("Hour");
        mVB.lineChart.getDescription().setTextColor(Color.WHITE);

        mVB.lineChart.getXAxis().setTextColor(Color.WHITE);
        mVB.lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        mVB.lineChart.getAxisLeft().setTextColor(Color.WHITE);
        mVB.lineChart.getAxisRight().setEnabled(false);
    }*/

    private void initGraph(Map<Integer, Integer> map){
        mVB.lineChart.setVisibility(View.VISIBLE);
        mVB.analysisLoading.setVisibility(View.INVISIBLE);

        ArrayList<Entry> dataValues = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry :
                map.entrySet()) {
            Log.d(TAG, "initGraph: hour: "+entry.getKey()+" , cnt: "+entry.getKey());
            dataValues.add(new Entry(entry.getKey(), entry.getValue()));
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