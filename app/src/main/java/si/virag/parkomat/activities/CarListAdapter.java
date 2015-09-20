package si.virag.parkomat.activities;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import si.virag.parkomat.R;
import si.virag.parkomat.models.Car;

public class CarListAdapter extends RecyclerView.Adapter<CarListAdapter.ViewHolder> {

    private final OnCarClickedListener listener;
    private List<Car> cars;

    public CarListAdapter(@NonNull List<Car> cars, @NonNull OnCarClickedListener listener) {
        this.cars = cars;
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_car, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Car c = cars.get(position);
        holder.name.setText(c.name);
        holder.registrationPlate.setText(c.registrationPlate);
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    @Override
    public long getItemId(int position) {
        return cars.get(position).id;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
        notifyDataSetChanged();
    }

    public interface OnCarClickedListener {
        void onCarClicked(Car car, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView name;
        public final TextView registrationPlate;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.item_car_name);
            registrationPlate = (TextView) itemView.findViewById(R.id.item_car_plate);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Car c = cars.get(getAdapterPosition());
            listener.onCarClicked(c, getAdapterPosition());
        }

        public Car getCar() {
            return cars.get(getAdapterPosition());
        }
    }
}
