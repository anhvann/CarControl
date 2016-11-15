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

	public Semaphore[][] sem;
	public Alley alley;
	public Barrier barrier;
	public Semaphore move;
	int tile; //0 = in no tile, 1 = in one tile, 2 = in two tiles
	public boolean inAlley;
	public boolean isRemoved;
	public boolean atBarrier;
	
	public Car(int no, CarDisplayI cd, Gate g, Semaphore[][] sem, Alley alley, Barrier barrier) {
		this.sem = sem;
		this.alley = alley;
		this.barrier = barrier;
		this.no = no;
		this.cd = cd;
		this.move = new Semaphore(1);
		this.tile = 1;
		this.inAlley = false;
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
				
				boolean cEnter = curpos.row == 10 && curpos.col == 0;
				boolean ccEnter = (curpos.row == 1 || curpos.row == 2) && curpos.col == 3;
				boolean cLeave = curpos.row == 0 && curpos.col == 2;
				boolean ccLeave = curpos.row == 9 && curpos.col == 1;
				boolean cBarrier = no >= 5 && curpos.row == 5 && curpos.col >= 3 && curpos.col <= 11;
				boolean ccBarrier = no <5 && curpos.row == 6 && curpos.col >= 3 && curpos.col <= 11;
					
				//Synchronize at barrier
				if (cBarrier || ccBarrier){
					barrier.sync(this);
				}

				//Enter alley
				if (cEnter || ccEnter) {
					alley.enter(this);
				} else if (cLeave || ccLeave) {
					alley.leave(this);
				}

				newpos = nextPos(curpos);
				sem[newpos.row][newpos.col].P();
				
				// Move to new position
				
				//Car has the semaphore of both its current position and the position it wants to enter
				//Not displayed
				move.P();
				cd.clear(curpos);
				tile = 0;
				move.V();
				
				//Car has the semaphore of both its current position and the position it wants to enter
				//Displayed in both its current position and the position it wants to enter
				move.P();
				cd.mark(curpos, newpos, col, no);
				tile = 2;
				move.V();
				sleep(speed());
				
				//Car has the semaphore of both its current position and the position it wants to enter
				//Not displayed
				move.P();
				cd.clear(curpos, newpos);
				tile = 0;
				move.V();
				
				//Car has the semaphore of the potision it wants to enter
				//Displayed in its new position (which by the end of this code is its current position)
				move.P();
				cd.mark(newpos, col, no);
				tile = 1;
				sem[curpos.row][curpos.col].V();
				
				curpos = newpos;
				move.V();
			}

		} catch (Exception e) {
			//TODO Interrupted
			//cd.println("Exception in Car no. " + no);
			//System.err.println("Exception in Car no. " + no + ":" + e);
			//e.printStackTrace();
			clean();
		}
	}

	public void clean() {
		Pos next = nextPos(curpos);
		
		if(tile == 1){ //in one tile with one semaphore
			cd.clear(curpos);
			sem[curpos.row][curpos.col].V();
		} else if (tile == 2){ //in two tiles with two semaphores
			cd.clear(curpos, newpos);
			sem[next.row][next.col].V();
			sem[curpos.row][curpos.col].V();
		} else { //in no tiles with two semaphores
			sem[next.row][next.col].V();
			sem[curpos.row][curpos.col].V();
		}

		if (inAlley){ //remove from alley counters
			if (no<5){
				alley.ccCounter--;
			} else {
				alley.cCounter--;
			}
		}
		alley.update(); //notify alley
	}
}

class Alley {
	
	volatile int cCounter; //clockwise counter
	volatile int ccCounter; //counter-clockwise counter
	
	public Alley() {
		cCounter = 0;
		ccCounter = 0;
	}

	public synchronized void enter(Car car){
		int i = car.no;
		if (i<5){
			while(cCounter>0){
				try {
					wait(); //for active cars
				} catch (InterruptedException e) {
					car.clean();
					try {
						while(car.isRemoved){ //for removed cars
							wait();
						}
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
			ccCounter++;
			car.inAlley = true;
		} else {
			while(ccCounter>0){
				try {
					wait(); //for active cars
				} catch (InterruptedException e) {
					car.clean();
					try {
						while(car.isRemoved){ 
							wait(); //for removed cars
						}
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
			cCounter++;
			car.inAlley = true;
		}
	}
	
	public synchronized void leave(Car car){
		int i = car.no;
		if (i<5){
			ccCounter--;
			car.inAlley = false;
			if (ccCounter==0){ //all have left
				notifyAll();
			}
		} else {
			cCounter--;
			car.inAlley = false;
			if (cCounter==0){ //all have left
				notifyAll();
			}
		}
		
	}
	
	public synchronized void update() {
		notifyAll();
	}
}

class Barrier {
	private Boolean barrierOn;
	public volatile int N = 9;
	public volatile int counter;
	private boolean ready;
	
	public Barrier (){
		barrierOn = false;
		counter = 0;
		ready = false;
	}

	public synchronized void sync(Car car) {
		if(barrierOn){
			while(ready){
				try {wait();} catch (InterruptedException e) {}
			}
			counter++;
			if (counter == N){ //all are present
				ready = true;
				notifyAll();
			}
			
			while(!ready){
				try {wait();} catch (InterruptedException e) {}
			}
			counter--;
			if(counter == 0){ //all have left
				ready = false;
				notifyAll();
			}
		}
	}
	
	public synchronized void on() {
		barrierOn = true;
	}
		
	public synchronized void off() {
		barrierOn = false;
		ready = true;
		notifyAll();
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
	Barrier barrier;
	int N = 9;
	
	public CarControl(CarDisplayI cd) {
		this.cd = cd;
		car = new Car[N];
		gate = new Gate[N];
		sem = new Semaphore[row][col];
		alley = new Alley();
		barrier = new Barrier();
		
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				sem[i][j] = new Semaphore(1);
			}
		}

		for (int no = 0; no < N; no++) {
			gate[no] = new Gate();
			car[no] = new Car(no, cd, gate[no], sem, alley, barrier);
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

	public void  removeCar(int no) {
		if(!car[no].isRemoved){
			try {
				car[no].move.P(); //wait til status has been set
				car[no].interrupt();
				car[no].isRemoved = true;
				car[no].move.V();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void restoreCar(int no) {
		if(car[no].isRemoved){
			car[no] = new Car(no, cd, gate[no], sem, alley, barrier);
			car[no].start();
		}
	}

	/* Speed settings for testing purposes */

	public void setSpeed(int no, int speed) {
		car[no].setSpeed(speed);
	}

	public void setVariation(int no, int var) {
		car[no].setVariation(var);
	}

}
