package me.pheynix.countdowntimer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStart;
    Button btnCancel;
    Button btnResume;
    Button btnPause;
    CountDownView countDownView;
    TextView tvDisplay;
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btn_start);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnResume = (Button) findViewById(R.id.btn_resume);
        btnPause = (Button) findViewById(R.id.btn_pause);
        countDownView = (CountDownView)findViewById(R.id.cdv_timer);
        tvDisplay = (TextView) findViewById(R.id.tv_display);

        btnStart.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnResume.setOnClickListener(this);
        btnPause.setOnClickListener(this);

        countDownView.setOnCountDowListener(new CountDownView.OnCountDownListener() {
            @Override public void onTick(long currentTimeInMillis) {
                calendar.setTimeInMillis(currentTimeInMillis);
                tvDisplay.setText(getDisplayText(calendar));
            }

            @Override public void onFinish() {
                tvDisplay.setText("finish!");
            }

            @Override public void onSet(int minute) {
                tvDisplay.setText(String.format("%d min", minute));
            }
        });

        btnStart.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
        btnResume.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                countDownView.start();

                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                btnResume.setVisibility(View.GONE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_resume:
                countDownView.resume();

                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.VISIBLE);
                btnResume.setVisibility(View.GONE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_pause:
                countDownView.pause();

                btnStart.setVisibility(View.GONE);
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_cancel:
                countDownView.cancel();

                btnStart.setVisibility(View.VISIBLE);
                btnPause.setVisibility(View.GONE);
                btnResume.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                break;
        }
    }


    private String getDisplayText(Calendar calendar) {
        return formatter.format(calendar.getTime());
    }

}
