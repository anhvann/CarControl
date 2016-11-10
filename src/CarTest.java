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
            	//Remove car from gate and restore into gate
            	cars.println("Car 1 and 2 enter the alley once there are no cars in the opposite direction on their path");
            	cars.removeCar(2);
            	sleep(1000);
            	cars.restoreCar(2);
                break;

            case 1:
            	//Remove car and restore immediately
            	cars.println("Car 1 and 2 enter the alley once there are no cars in the opposite direction on their path");
            	cars.startCar(2);
            	cars.removeCar(2);

            	cars.restoreCar(2);
                break;
                
            case 2:
            	//Remove car from alley and restore into gate
            	//Cars waiting to enter the alley will not take the removed car into account
            	cars.println("Car 1 and 2 enter the alley once there are no cars in the opposite direction on their path");
            	cars.removeCar(2);
                break;
                
            case 3:
            	//Remove car and cars
            	cars.println("Car 1 and 2 enter the alley once there are no cars in the opposite direction on their path");
            	cars.removeCar(2);
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



