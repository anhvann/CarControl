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
            	//Car 1 and 2 enter the alley once there are no cars in the opposite direction on their path
            	cars.println("Car 1 and 2 enter the alley once there are no cars in the opposite direction on their path");
            	cars.setSpeed(1, 50);
            	cars.startAll();
            	sleep(10000);
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



