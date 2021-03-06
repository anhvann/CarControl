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
            	//Car 1-8 wait for car 0 at the barrier
            	cars.println("Car 1-8 wait for car 0 at the barrier");
            	cars.startAll();
            	cars.barrierOn();
            	sleep(2000);
            	cars.startCar(0);
            	cars.stopAll();
            	cars.barrierOff();
                break;
                
            case 1:
            	//All cars get through the barrier once they have all arrived at the barrier
            	//Car 0's gate is closed in the second round to show that car 0 actually gets through the barrier
            	cars.println("All cars get through the barrier");
            	cars.barrierOn();
            	cars.startAll();
            	sleep(2000);
            	cars.stopAll();
            	cars.barrierOff();
                break;
                
            case 2:
            	//4 cars are waiting at the barrier and get through because the barrier is removed
            	cars.println("Waiting cars get through the barrier when barrier is off");
            	cars.barrierOn();
            	cars.startCar(0);
            	cars.startCar(1);
            	cars.startCar(2);
            	cars.startCar(3);
            	sleep(1000);
            	cars.barrierOff();
            	cars.stopAll();
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



