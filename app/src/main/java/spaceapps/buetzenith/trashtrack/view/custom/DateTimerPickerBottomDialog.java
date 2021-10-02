package spaceapps.buetzenith.trashtrack.view.custom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import spaceapps.buetzenith.trashtrack.databinding.BottomDialogTimelineBinding;

public class DateTimerPickerBottomDialog extends BottomSheetDialogFragment{
    private static final String TAG = "DateTimerPickerBottomDi";
    private BottomDialogTimelineBinding mVB;

    private int currentSelectedTabPosition;

    private TimeZone timeZone;

    private OnDateTimeSelectedListener onDateTimeSelectedListener;

    @Inject
    public DateTimerPickerBottomDialog() {

    }

    public interface OnDateTimeSelectedListener{
        void onDateTimeSelected(Date date);
    }

    public void setOnDateTimeSelectedListener(OnDateTimeSelectedListener onDateTimeSelectedListener) {
        this.onDateTimeSelectedListener = onDateTimeSelectedListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        mVB = BottomDialogTimelineBinding.inflate(inflater, container, false);
        return mVB.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        currentSelectedTabPosition = mVB.tabLayout.getSelectedTabPosition();
        changePicker(currentSelectedTabPosition);

        mVB.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentSelectedTabPosition = tab.getPosition();
                changePicker(currentSelectedTabPosition);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        mVB.submitBtn.setOnClickListener(v -> {
            Calendar calendar = new GregorianCalendar();
            int year = mVB.datePicker.getYear();
            int month = mVB.datePicker.getMonth();
            int date = mVB.datePicker.getDayOfMonth();
            int hour  =  mVB.timerPicker.getCurrentHour();
            int minute = mVB.timerPicker.getCurrentMinute();

            calendar.set(year, month, date, hour, minute);

            if(onDateTimeSelectedListener!=null)
                onDateTimeSelectedListener.onDateTimeSelected(calendar.getTime());

            // close the bottom sheet
            //https://stackoverflow.com/questions/42293488/show-and-hide-bottom-sheet-programmatically
            super.dismiss();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume: ");
        // init time and date picker with current time date
        setInitialDateTime();
    }

    private void setInitialDateTime() {
       // Log.d(TAG, "setInitialDateTime: ");
        // https://stackoverflow.com/questions/15218542/get-correct-local-time-in-java-calendar#:~:text=I%20use%20this%20method%20to,%3Amm%3Ass%22).
        Calendar calendar = new GregorianCalendar();

        if(timeZone == null){
            timeZone = calendar.getTimeZone();
        }else
            calendar.setTimeZone(timeZone);

       // Log.d(TAG, "setInitialDateTime: time zone: "+calendar.getTimeZone());

        // https://stackoverflow.com/questions/6451837/how-do-i-set-the-current-date-in-a-datepicker
        mVB.datePicker.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                null);

        mVB.timerPicker.setCurrentHour(calendar.get(Calendar.HOUR));
        mVB.timerPicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
    }

    private void changePicker(int currentSelectedTabPosition){
        Log.d(TAG, "changePicker: current position: "+currentSelectedTabPosition);
        if(currentSelectedTabPosition==0){
            mVB.timerPicker.setVisibility(View.VISIBLE);
            mVB.datePicker.setVisibility(View.GONE);
        }else{
            mVB.timerPicker.setVisibility(View.GONE);
            mVB.datePicker.setVisibility(View.VISIBLE);
        }
    }
}
