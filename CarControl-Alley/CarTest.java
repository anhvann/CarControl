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
            	//Cars do not bump into each other and there is no deadlock
            	cars.println("Cars do not bump into each other and there is no deadlock");
            	cars.startAll();
            	sleep(10000);
            	cars.stopAll();
                break;
            case 1:
            	//The alley synchronization works even if one car direction is entering the aley multiple times in a row
            	cars.println("The alley synchronization works even if one car direction is entering the aley multiple times in a row");
                cars.startAll();
                cars.setSlow(true);
                sleep(10000);
                cars.stopAll();
                cars.setSlow(false);
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



