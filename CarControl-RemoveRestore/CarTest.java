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
            	//Remove car from closed gate and restore into closed gate
            	cars.println("Car is removed from closed gate and restored into closed gate");
            	cars.removeCar(2);
            	sleep(1000);
            	cars.restoreCar(2);
            	cars.stopAll();
                break;

            case 1:
            	//Remove car from closed gate and restore into open gate
            	cars.println("Car is removed from closed gate and restored into open gate");
            	cars.removeCar(2);
            	cars.startCar(2);
            	sleep(1000);
            	cars.restoreCar(2);
            	sleep(1000);
            	cars.stopAll();
                break;
                
            case 2:
            	//Remove car and restore immediately into closed gate, no difference can be seen
            	cars.println("Car is removed and restored immediately into closed gate, no difference can be seen");
            	cars.removeCar(2);
            	cars.restoreCar(2);
                break;
            
            case 3:
            	//Remove car and restore immediately into open gate, it only looks like the car is started
            	cars.println("Car is removed and restored immediately into open gate, it only looks like the car is started");
            	cars.startCar(2);
            	cars.removeCar(2);
            	cars.restoreCar(2);
            	sleep(1000);
            	cars.stopAll();
                break;
            
            case 4:
            	//Remove car from outside gate and not in the alley and restore into gate
            	cars.println("Car is removed from outside its gate and not in the alley and restored again");
            	cars.startCar(2);
            	sleep(1200);
            	cars.removeCar(2);
            	cars.restoreCar(2);
            	cars.stopAll();
                break;
                
            case 5:
            	//Remove car from alley and restore into gate
            	//Cars waiting to enter the alley will not take the removed car into account
            	cars.println("Car is removed from alley");
            	cars.setSlow(true);
            	cars.setSpeed(2, 50);
            	cars.startAll();
            	sleep(2000);
            	cars.removeCar(2);
            	cars.stopAll();
            	cars.restoreCar(2);
                break;
            case 6:
            	//Remove car from waiting at alley and restore into gate
            	//Other cars from the same direction waiting to enter alley, is one step closer and one car less needs to get out for the cars in the other direction to get it
            	cars.println("Car is removed from right outside the alley");
            	cars.setSlow(true);
            	cars.setSpeed(8, 50);
            	cars.startAll();
            	sleep(2500);
            	cars.removeCar(8);
            	cars.stopAll();
            	cars.restoreCar(8);
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



