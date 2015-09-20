package si.virag.parkomat;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CarManagerActivity extends AppCompatActivity {

    @Bind(R.id.carmanager_list)
    protected RecyclerView carList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_manager);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.carmanager_add)
    protected void onAddClick(FloatingActionButton button) {
        new MaterialDialog.Builder(this)
                          .title("Add car")
                          .positiveText("Add")
                          .negativeText("Cancel")
                          .show();
    }
}
