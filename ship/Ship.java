package ship;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import port.Berth;
import port.Port;
import port.PortException;
import warehouse.Container;
import warehouse.Warehouse;

public class Ship implements Runnable {

	private final static Logger logger = Logger.getRootLogger();
	private volatile boolean stopThread = false;

	private String name;
	private Port port;
	private Warehouse shipWarehouse;

	public Ship(String name, Port port, int shipWarehouseSize) {
		this.name = name;
		this.port = port;
		this.shipWarehouse = new Warehouse(shipWarehouseSize);
	}

	public void setContainersToWarehouse(List<Container> containerList) {
		this.shipWarehouse.addContainer(containerList);
	}

	public String getName() {
		return this.name;
	}

	public void stopThread() {
		this.stopThread = true;
	}

	public void run() {
		try {
			while (!this.stopThread) {
				atSea();
				inPort();
			}
		}
		catch (InterruptedException e) {
			logger.error("С кораблем случилась неприятность и он уничтожен.", e);
		}
		catch (PortException e) {
			logger.error("Порт не ответил на запрос корабля", e);
		}
	}

	private void atSea() throws InterruptedException {
		logger.debug("Ship " + this.name + " at see right now");
		Thread.sleep(5000);
	}

	private void inPort() throws PortException, InterruptedException {

		boolean isLockedBerth = false;
		Berth berth = null;
		try {
			isLockedBerth = port.lockBerth(this);
			
			if (isLockedBerth) {
				berth = port.getBerth(this);
				logger.debug("Корабль " + name + " пришвартовался к причалу " + berth.getId());
				ShipAction action = getNextAction();
				executeAction(action, berth);
				Thread.sleep(7000); //emulate action execution longer
			}
			else {
				logger.debug("Кораблю " + name + " отказано в швартовке к причалу ");
			}
		}
		finally {
			if (isLockedBerth){
				port.unlockBerth(this);
				logger.debug("Корабль " + name + " отошел от причала " + berth.getId());
			}
		}
		
	}

	private void executeAction(ShipAction action, Berth berth) throws InterruptedException {
		switch (action) {
		case LOAD_TO_PORT:
 				loadToPort(berth);
			break;
		case LOAD_FROM_PORT:
				loadFromPort(berth);
			break;
		}
	}

	private boolean loadToPort(Berth berth) throws InterruptedException {

		int containersNumberToMove = conteinersCount(shipWarehouse.getRealSize());
		boolean result = false;

		logger.debug("Корабль " + name + " хочет выгрузить " + containersNumberToMove
				+ " контейнеров на склад порта." + shipWarehouse.getRealSize());

		result = berth.add(shipWarehouse, containersNumberToMove);
		
		if (!result) {
			logger.debug("Недостаточно места на складе порта для выгрузки кораблем "
					+ name + " " + containersNumberToMove + " контейнеров.");
		} else {
			logger.debug("Корабль " + name + " выгрузил " + containersNumberToMove
					+ " контейнеров в порт.");
			
		}
		return result;
	}

	private boolean loadFromPort(Berth berth) throws InterruptedException {
		
		int containersNumberToMove = conteinersCount(port.getPortWarehouse().getRealSize());
		
		boolean result = false;

		logger.debug("Корабль " + name + " хочет загрузить " + containersNumberToMove
				+ " контейнеров со склада порта." + shipWarehouse.getRealSize());
		
		result = berth.get(shipWarehouse, containersNumberToMove);
		
		if (result) {
			logger.debug("Корабль " + name + " загрузил " + containersNumberToMove
					+ " контейнеров из порта.");
		} else {
			logger.debug("Недостаточно места на на корабле " + name
					+ " для погрузки " + containersNumberToMove + " контейнеров из порта.");
		}
		
		return result;
	}

	private int conteinersCount(int size) {
		Random random = new Random();

		if (size == 0) {
			return 1;
		}

		int containers = random.nextInt(size) + 1;
		if (containers > size) {
			return size;
		}

		return containers;
	}

	private ShipAction getNextAction() {
		Random random = new Random();
		int value = random.nextInt(4000);
		if (value < 1000 && this.shipWarehouse.getRealSize() != 0) {
			return ShipAction.LOAD_TO_PORT;
		} else {
			return ShipAction.LOAD_FROM_PORT;
		}
	}

	enum ShipAction {
		LOAD_TO_PORT, LOAD_FROM_PORT
	}

	public Warehouse getShipWarehouse() {
		return this.shipWarehouse;
	}
}
