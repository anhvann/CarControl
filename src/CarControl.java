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
	Pos startpos; // Start position (provided by GUI)
	Pos barpos; // Barrier position (provided by GUI)
	Color col; // Car color
	Gate mygate; // Gate at start position

	int speed; // Current car speed
	Pos curpos; // Current position
	Pos newpos; // New position to go to

	Semaphore[][] sem;
	Alley alley;
	Barrier barrier;
	
	public Car(int no, CarDisplayI cd, Gate g, Semaphore[][] sem, Alley alley, Alley alley2, Barrier barrier) {
		this.sem = sem;
		this.alley = alley;
		this.barrier = barrier;
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
				
				//Enter alley
				boolean cEnter = curpos.row == 10 && curpos.col == 0;
				boolean ccEnter = curpos.row == 2 && curpos.col == 1;
				
				//Enter alley2
				boolean cEnter2 = no > 4 && curpos.row == 2 && curpos.col == 0;
				boolean ccEnter2 = curpos.row == 1 && curpos.col == 3;
				
				//Leave alley2
				boolean cLeave2 = no > 4 && curpos.row == 0 && curpos.col == 2; 
				boolean ccLeave2 = (no != 1 && no != 2) && curpos.row == 2 && curpos.col == 0;
				
				//Leave alley
				boolean cLeave = no > 4 && curpos.row == 1 && curpos.col == 0;
				boolean ccLeave = curpos.row == 9 && curpos.col == 1;
				
				boolean cBarrier = no >= 5 && curpos.row == 5 && curpos.col >= 3 && curpos.col <= 11;
				boolean ccBarrier = no <5 && curpos.row == 6 && curpos.col >= 3 && curpos.col <= 11;
						
				if (cBarrier || ccBarrier){
					barrier.sync();
				}

				if (cEnter || ccEnter) {
					alley.enter(no);
				} else if (cLeave || ccLeave) {
					alley.leave(no);
				}
				
				newpos = nextPos(curpos);
				sem[newpos.row][newpos.col].P();

				// Move to new position
				cd.clear(curpos);
				cd.mark(curpos, newpos, col, no);
				sleep(speed());
				cd.clear(curpos, newpos);
				cd.mark(newpos, col, no);

				sem[curpos.row][curpos.col].V();
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

class Alley {
	
	private Semaphore alley;
	private Semaphore first;
	private Semaphore mutex;
	private volatile int cCounter; //clockwise counter
	private volatile int ccCounter; //counter-clockwise counter
	
	public Alley() {
		this.alley = new Semaphore(1);
		this.first = new Semaphore(1);
		this.mutex = new Semaphore(1);
		cCounter = 0;
		ccCounter = 0;
	}
	
	public void enter(int no) throws InterruptedException {
		if (no >= 5) {
			mutex.P();
			cCounter++;
			if (cCounter == 1) {
				mutex.V();
				alley.P();
			} else if (cCounter > 1) {
				mutex.V();
			}
		} else {
			mutex.P();
			ccCounter++;
			if (ccCounter == 1) {
				mutex.V();
				first.P();
				alley.P();
				first.V();
			} else if (ccCounter > 1){
				mutex.V();
				first.P();
				first.V();
			} 
		}
	}
	
	public void leave(int no) throws InterruptedException {
		if (no >= 5) {
			mutex.P();
			cCounter--;
			if (cCounter == 0) {
				alley.V();
			}
			mutex.V();
		} else {
			mutex.P();
			ccCounter--;
			if (ccCounter == 0) {
				alley.V();
			}
			mutex.V();
		}
	}
}

class Barrier {
	private Boolean barrierOn;
	private int N = 9;
	private volatile int counter;
	private Semaphore barrier1;
	private Semaphore barrier2;
	private Semaphore mutex;
	
	public Barrier (){
		barrierOn = false;
		counter = 0;
		barrier1 = new Semaphore(0);
		barrier2 = new Semaphore(0);
		mutex = new Semaphore(1);
	}
	
	public void sync() throws InterruptedException {
		if(barrierOn){
			mutex.P();
			counter++;
			if (counter == N){
				mutex.V();
				barrier2 = new Semaphore(0);
				for(int i = 0; i<N; i++){
					barrier1.V();
				}
			} else {
				mutex.V();
			}
			barrier1.P();
			mutex.P();
			counter--;
			if (counter == 0){
				mutex.V();
				barrier1 = new Semaphore(0);
				for(int i = 0; i<N; i++){
					barrier2.V();
				}
			} else {
				mutex.V();
			}
			barrier2.P();
		}
	}
	
	public void on() {
		if(!barrierOn){
			barrierOn = true;
			counter = 0;
			barrier1 = new Semaphore(0);
			barrier2 = new Semaphore(0);
		}
	}
		
	public void off() {
		if(barrierOn){
			barrierOn = false;
			//Signal all
			for(int i = 0; i<N; i++){
				barrier1.V();
				barrier2.V();
			}		
		}
	}
	
}

public class CarControl implements CarControlI {

	CarDisplayI cd; // Reference to GUI
	Car[] car; // Cars
	Gate[] gate; // Gates

	Semaphore[][] sem;
	int row = 11;
	int col = 12;
	Alley alley;
	Alley alley2;
	Barrier barrier;
	
	public CarControl(CarDisplayI cd) {
		this.cd = cd;
		car = new Car[9];
		gate = new Gate[9];
		sem = new Semaphore[row][col];
		alley = new Alley();
		alley2 = new Alley();
		barrier = new Barrier();

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				sem[i][j] = new Semaphore(1);
			}
		}

		for (int no = 0; no < 9; no++) {
			gate[no] = new Gate();
			car[no] = new Car(no, cd, gate[no], sem, alley, alley2, barrier);
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
		barrier.on();
	}

	public void barrierOff() {
		barrier.off();
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
