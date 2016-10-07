//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2016

//Hans Henrik Lovengreen    Oct 3, 2016

import java.awt.Color;
import java.util.HashMap;

class Gate {

	Semaphore g = new Semaphore(0);
	Semaphore e = new Semaphore(1);
	boolean isopen = false;

	public void pass() throws InterruptedException {
		g.P();
		g.V();
	}

	public void open() {
		try {
			e.P();
		} catch (InterruptedException e) {
		}
		if (!isopen) {
			g.V();
			isopen = true;
		}
		e.V();
	}

	public void close() {
		try {
			e.P();
		} catch (InterruptedException e) {
		}
		if (isopen) {
			try {
				g.P();
			} catch (InterruptedException e) {
			}
			isopen = false;
		}
		e.V();
	}

}

class Car extends Thread {

	int basespeed = 100; // Rather: degree of slowness
	int variation = 50; // Percentage of base speed

	CarDisplayI cd; // GUI part

	int no; // Car number
	Pos startpos; // Startpositon (provided by GUI)
	Pos barpos; // Barrierpositon (provided by GUI)
	Color col; // Car color
	Gate mygate; // Gate at startposition

	int speed; // Current car speed
	Pos curpos; // Current position
	Pos newpos; // New position to go to

	// Semaphores
	HashMap<String, Semaphore> sem;
	Semaphore alley;
	Semaphore pair;
	volatile int[] counters; //clockwise, counter-clockwise, first
	
	public Car(int no, CarDisplayI cd, Gate g, HashMap<String, Semaphore> sem, Semaphore alley, Semaphore pair, int[] counters) {
		this.counters = counters;
		this.sem = sem;
		this.alley = alley;
		this.pair = pair;
		this.no = no;
		this.cd = cd;
		mygate = g;
		startpos = cd.getStartPos(no);
		barpos = cd.getBarrierPos(no); // For later use

		col = chooseColor();

		// do not change the special settings for car no. 0
		if (no == 0) {
			basespeed = 0;
			variation = 0;
			setPriority(Thread.MAX_PRIORITY);
		}
	}

	public synchronized void setSpeed(int speed) {
		if (no != 0 && speed >= 0) {
			basespeed = speed;
		} else
			cd.println("Illegal speed settings");
	}

	public synchronized void setVariation(int var) {
		if (no != 0 && 0 <= var && var <= 100) {
			variation = var;
		} else
			cd.println("Illegal variation settings");
	}

	synchronized int chooseSpeed() {
		double factor = (1.0D + (Math.random() - 0.5D) * 2 * variation / 100);
		return (int) Math.round(factor * basespeed);
	}

	private int speed() {
		// Slow down if requested
		final int slowfactor = 3;
		return speed * (cd.isSlow(curpos) ? slowfactor : 1);
	}

	Color chooseColor() {
		return Color.blue; // You can get any color, as longs as it's blue
	}

	Pos nextPos(Pos pos) {
		// Get my track from display
		return cd.nextPos(no, pos);
	}

	boolean atGate(Pos pos) {
		return pos.equals(startpos);
	}

	public void run() {
		try {

			speed = chooseSpeed();
			curpos = startpos;
			cd.mark(curpos, col, no);

			while (true) {
				sleep(speed());

				if (atGate(curpos)) {
					mygate.pass();
					speed = chooseSpeed();
				}
				if (no >= 5) {
					if (curpos.row == 10 && curpos.col == 0) {
						counters[0]++;
						if (counters[0] == 1 ) {
							alley.P();
						}
					} else if (curpos.row == 0 && curpos.col == 3) {
						counters[0]--;
						if (counters[0] == 0) {
							alley.V();
						}
					}
				} else {
					if ((curpos.row == 1 || curpos.row == 2) && curpos.col == 3) {
						counters[1]++;
						System.out.println(pair.toString()+ " "+alley.toString());
						if (counters[1] == 1) {
							pair.P();
							alley.P();
							pair.V();
						} else if (counters[1] == 2){
						    pair.P();
						    pair.V();
						}
					} else if (curpos.row == 9 && curpos.col == 1) {
						counters[1]--;
						if (counters[1] == 0) {
							alley.V();
						}
					}
				}

				sem.get(nextPos(curpos).toString()).P();
				newpos = nextPos(curpos);

				// Move to new position
				cd.clear(curpos);
				cd.mark(curpos, newpos, col, no);
				sleep(speed());
				cd.clear(curpos, newpos);
				cd.mark(newpos, col, no);

				sem.get(curpos.toString()).V();
				curpos = newpos;
			}

		} catch (

		Exception e) {
			cd.println("Exception in Car no. " + no);
			System.err.println("Exception in Car no. " + no + ":" + e);
			e.printStackTrace();
		}
	}

}

public class CarControl implements CarControlI {

	CarDisplayI cd; // Reference to GUI
	Car[] car; // Cars
	Gate[] gate; // Gates

	HashMap<String, Semaphore> sem;
	Semaphore alley = new Semaphore(1);
	Semaphore pair = new Semaphore(1);
	int[] counters = new int[3]; 
			
	public CarControl(CarDisplayI cd) {
		this.cd = cd;
		car = new Car[9];
		gate = new Gate[9];
		sem = new HashMap<>();
		Semaphore alley = new Semaphore(1);
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 12; j++) {
				Pos p = new Pos(i, j);
				sem.put(p.toString(), new Semaphore(1));
			}
		}

		for (int no = 0; no < 9; no++) {
			gate[no] = new Gate();
			car[no] = new Car(no, cd, gate[no], sem, alley, pair, counters);
			car[no].start();
		}

	}

	public void startCar(int no) {
		gate[no].open();
	}

	public void stopCar(int no) {
		gate[no].close();
	}

	public void barrierOn() {
		cd.println("Barrier On not implemented in this version");
	}

	public void barrierOff() {
		cd.println("Barrier Off not implemented in this version");
	}

	public void barrierSet(int k) {
		cd.println("Barrier threshold setting not implemented in this version");
		// This sleep is for illustrating how blocking affects the GUI
		// Remove when feature is properly implemented.
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
	}

	public void removeCar(int no) {
		cd.println("Remove Car not implemented in this version");
	}

	public void restoreCar(int no) {
		cd.println("Restore Car not implemented in this version");
	}

	/* Speed settings for testing purposes */

	public void setSpeed(int no, int speed) {
		car[no].setSpeed(speed);
	}

	public void setVariation(int no, int var) {
		car[no].setVariation(var);
	}

}
