package port;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import warehouse.Container;
import warehouse.Warehouse;

public class Berth {

	private int id;
	private Warehouse portWarehouse;

	public Berth(int id, Warehouse warehouse) {
		this.id = id;
		this.portWarehouse = warehouse;
	}

	public int getId() {
		return id;
	}

	public synchronized boolean add(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;

		int newConteinerCount = portWarehouse.getRealSize() + numberOfConteiners;

		if (newConteinerCount <= portWarehouse.getFreeSize()) {
			result = doMoveFromShip(shipWarehouse, numberOfConteiners);
		}

		return result;
	}
	
	private synchronized boolean doMoveFromShip(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException{

		if (shipWarehouse.getRealSize() >= numberOfConteiners) {
			List<Container> containers = shipWarehouse.getContainer(numberOfConteiners);
			portWarehouse.addContainer(containers);
			return true;
		}

		return false;		
	}

	public synchronized boolean get(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;

		if (numberOfConteiners <= portWarehouse.getRealSize()) {
			result = doMoveFromPort(shipWarehouse, numberOfConteiners);
		}

		return result;
	}
	
	private synchronized boolean doMoveFromPort(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {

		int newConteinerCount = shipWarehouse.getRealSize() + numberOfConteiners;
		if (newConteinerCount <= shipWarehouse.getFreeSize()) {
			List<Container> containers = portWarehouse.getContainer(numberOfConteiners);
			shipWarehouse.addContainer(containers);
			return true;
		}
		
		return false;		
	}

}
