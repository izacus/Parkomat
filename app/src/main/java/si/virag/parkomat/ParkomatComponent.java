package si.virag.parkomat;

import javax.inject.Singleton;

import dagger.Component;
import si.virag.parkomat.activities.CarManagerActivity;
import si.virag.parkomat.activities.MainActivity;
import si.virag.parkomat.activities.WelcomeActivity;

@Singleton
@Component(modules = {ParkomatApplicationModule.class})
public interface ParkomatComponent {
    void inject(ParkomatApplication parkomatApplication);
    void inject(CarManagerActivity activity);
    void inject(MainActivity mainActivity);
    void inject(WelcomeActivity welcomeActivity);
}
