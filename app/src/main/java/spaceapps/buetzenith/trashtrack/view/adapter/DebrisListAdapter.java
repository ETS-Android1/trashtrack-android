package spaceapps.buetzenith.trashtrack.view.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.R;
import spaceapps.buetzenith.trashtrack.databinding.CardDebrisBinding;
import spaceapps.buetzenith.trashtrack.databinding.CardSatelliteBinding;
import spaceapps.buetzenith.trashtrack.utils.DebrisCatalog;

public class DebrisListAdapter extends RecyclerView.Adapter<DebrisListAdapter.Holder>{
    private static final String TAG = "DebrisListAdapter";

    private List<DebrisCatalog.Debris> debrisList;

    private OnClickListener onClickListener;

    @Inject
    public DebrisListAdapter(){
    }

    public void setDebrisList(List<DebrisCatalog.Debris> debrisList) {
        Log.d(TAG, "setDebrisList: list size: "+debrisList.size());
        this.debrisList = debrisList;
        super.notifyDataSetChanged();
    }

    public interface OnClickListener{
        void onClick(DebrisCatalog.Debris debris, int fragmentId);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public Holder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_debris, parent, false);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull Holder holder, int position) {
        DebrisCatalog.Debris debris = debrisList.get(position);

        Log.d(TAG, "onBindViewHolder: position: "+position);

        holder.mVB.debrisIv.setImageResource(debris.image);

        holder.mVB.debrisNameTv.setText(debris.name);
        String date = "Created on "+debris.date;
        holder.mVB.creationTv.setText(date);
        holder.mVB.originTv.setText(debris.origin);

        holder.mVB.globeBtn.setOnClickListener(v -> {
            if(onClickListener!=null)
                onClickListener.onClick(debris, R.id.globeFragment);
        });

        holder.mVB.mapBtn.setOnClickListener(v -> {
            if(onClickListener!=null)
                onClickListener.onClick(debris, R.id.googleMapFragment);
        });

        holder.mVB.graphBtn.setOnClickListener(v -> {
            if(onClickListener !=null)
                onClickListener.onClick(debris, R.id.graphFragment);
        });
    }

    @Override
    public int getItemCount() {
        return debrisList.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        CardDebrisBinding mVB;

        public Holder(@NonNull @NotNull View itemView) {
            super(itemView);
            mVB = CardDebrisBinding.bind(itemView);
        }
    }
}
