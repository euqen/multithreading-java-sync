package port;

import org.apache.log4j.Logger;
import ship.Ship;
import warehouse.Warehouse;

import java.io.*;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public final class PortDispatcher implements Runnable {

    private Port port;
    private final static Logger logger = Logger.getRootLogger();
    private final String origin = "/home/euqen/dev/sync/src/journal.txt";
    private Boolean enabled;

    public PortDispatcher(Port port) {
        this.port = port;
        this.enabled = true;

        new Thread(this).start();
    }

    /**
     * Run thread for dispatcher
     *
     */

    public void run() {
        try {
            while(this.enabled) {
                Thread.sleep(5000); //Dispatch port every 5s

                this.dispatch();
            }
        }
        catch(InterruptedException e) {
            logger.debug("Dispatcher system has been broken");

        }
    }

    /**
     * Disable dispatch system for port
     *
     */

    public void disable() {
        this.enabled = false;
    }

    /**
     * Get information about berths and wire house
     * and write it into journal.
     *
     */

    private void dispatch() {
        String dispatchInfo = this.dispatchWareHouse();
               dispatchInfo += this.dispatchBerths();

        journalize(dispatchInfo);
    }

    /**
     * Get wire house info for dispatch
     *
     * @return dispatch info
     */

    private String dispatchWareHouse() {
        Warehouse warehouse = this.port.getPortWarehouse();
        int portWareHouseFreeSize = warehouse.getFreeSize();
        int portWareHouseRealSize = warehouse.getSize();
        int portWareHouseUsedSize = warehouse.getRealSize();

        String info = "#################### PORT #" + this.port.number + " ######################\n";
               info += "### Warehouse: \n   Free size: " + portWareHouseFreeSize + "\n"
                    + "   Used size: " + portWareHouseUsedSize + "\n"
                    + "   Real size: " + portWareHouseRealSize + "\n";

        return info;
    }

    /**
     * Get dispatch info for berths
     *
     * @return dispatch info
     */

    private String dispatchBerths() {
        BlockingQueue<Berth> freeBerths = port.getPortFreeBerths();
        int freeBerthsCount = freeBerths.size();

        Map<Ship, Berth> usedBerths = port.getPortUsedBerths();

        String berthInfo = "### Berths:\n   Free berths: " + freeBerthsCount + " \n";

        for (Map.Entry<Ship, Berth>entry : usedBerths.entrySet()) {

            berthInfo += "   Berth #" + entry.getValue().getId() + " is used right now by ship "
                      +  entry.getKey().getName() + ".\n"
                      +  "   This ship have " + entry.getKey().getShipWarehouse().getRealSize()
                      + " containers and free space for " + entry.getKey().getShipWarehouse().getFreeSize() +" containers\n";

        }

        berthInfo += "##################################################\n";

        return berthInfo;
    }

    /**
     * Write dispatch info into journal
     *
     * @param message
     */

    private void journalize(String message) {
        File file = new File(origin);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(origin, true)));

            try {
                out.println(message);
            }
            finally {
                out.close();
            }
        }
        catch(IOException e) {
            logger.debug("Port dispatcher has broken!");
        }
    }
}
