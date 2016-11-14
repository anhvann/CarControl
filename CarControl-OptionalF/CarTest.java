//Prototype implementation of Car Test class
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2016

//Hans Henrik Lovengreen    Oct 3, 2016

public class CarTest extends Thread {

    CarTestingI cars;
    int testno;

    public CarTest(CarTestingI ct, int no) {
        cars = ct;
        testno = no;
    }

    public void run() {
        try {
            switch (testno) { 
            case 0:
            	//Remove random car not at barrier, the rest of the cars should be able to get through the barrier without the removed car
            	cars.println("Car not at barrier is removed and the rest of the cars get through the barrier");
            	cars.barrierOn();
            	cars.startAll();
            	sleep(1000);
            	cars.removeCar(2);
            	cars.stopAll();
            	cars.barrierOff();
                break;

            case 1:
            	//Remove the last car that the cars at the barrier are waiting for, the rest of the cars should be able to get through the barrier without the removed car
            	//This is a special case because the status of the cars at the barrier is usually only checked when a new car arrives at the barrier,
            	//but when the last car that they are waiting for is removed, no car arrives at the barrier to update the status, 
            	//so the status should be updated every time a car is removed as well
            	cars.println("Last car is removed and the rest of the cars get through the barrier");
            	cars.startCar(1);
            	sleep(1000);
            	cars.barrierOn();
            	cars.startAll();
            	sleep(2000);
            	cars.removeCar(1);
            	sleep(1000);
            	cars.stopAll();
            	cars.barrierOff();
                break;
                
            case 2:
            	//Restore a removed car just before the previously last car arrives at the barrier, the cars should wait for the restored car before they get through the barrier
            	cars.println("Car is restored, cars at barrier should wait for it");
            	cars.startCar(1);
            	cars.removeCar(2);
            	sleep(1000);
            	cars.barrierOn();
            	cars.startAll();
            	cars.stopCar(2);
            	sleep(2000);
            	cars.restoreCar(2);
            	sleep(5000);
            	cars.startCar(2);
            	sleep(2000);
            	cars.stopAll();
            	cars.barrierOff();
                break;
            
            case 3:
            	//Car waiting at barrier is removed and should no longer be taken into account by the barrier
            	cars.println("Car waiting at the barrier is removed");
            	cars.startAll();
            	cars.barrierOn();
            	sleep(1000);
            	cars.removeCar(2);
            	sleep(1000);
            	cars.startCar(0);
            	sleep(1000);
            	cars.stopAll();
            	cars.restoreCar(2);
            	cars.barrierOff();
                break;
                
            default:
                cars.println("Test " + testno + " not available");
            }

            cars.println("Test ended");

        } catch (Exception e) {
            System.err.println("Exception in test: "+e);
        }
    }

}



