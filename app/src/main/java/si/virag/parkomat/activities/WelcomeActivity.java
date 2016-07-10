package si.virag.parkomat.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;
import si.virag.parkomat.ParkomatApplication;
import si.virag.parkomat.R;
import si.virag.parkomat.models.Car;
import si.virag.parkomat.modules.CarsManager;

public class WelcomeActivity extends AppCompatActivity {

    @BindView(R.id.welcome_addcar_container)
    public View containerAddCar;

    @BindView(R.id.welcome_disclaimer_container)
    public View containerDisclaimer;

    @BindView(R.id.welcome_carname)
    public EditText edtCarName;

    @BindView(R.id.welcome_carplate)
    public EditText edtCarPlate;

    @Inject
    public CarsManager carsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        ParkomatApplication.get(this).inject(this);

        edtCarPlate.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onAddCarClick();
                    return true;
                }

                return false;
            }
        });
    }

    @OnClick(R.id.welcome_iunderstand)
    public void onAcceptDisclaimer() {
        containerAddCar.setVisibility(View.VISIBLE);
        containerAddCar.setAlpha(0.0f);
        containerAddCar.animate().alpha(1.0f);
        containerDisclaimer.animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                containerDisclaimer.setVisibility(View.INVISIBLE);
            }
        });
    }

    @OnClick(R.id.welcome_addcar)
    public void onAddCarClick() {
        carsManager.addCar(edtCarName.getText().toString(), edtCarPlate.getText().toString()).subscribe(new Action1<Car>() {
            @Override
            public void call(Car car) {
                completeWelcome();
            }
        });
    }

    private void completeWelcome() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(MainActivity.PREF_WELCOME_DONE, true).apply();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
