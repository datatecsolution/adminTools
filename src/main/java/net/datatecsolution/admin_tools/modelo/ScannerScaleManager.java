package net.datatecsolution.admin_tools.modelo;

import jpos.JposException;
import jpos.Scale;
import jpos.ScaleConst;
import jpos.Scanner;

public class ScannerScaleManager {

    private Scanner scanner;
    private Scale scale;
    public boolean bAsyncMode = false;
    public boolean bUseFiveDigits = false;
    public String sUnits = "";

    String jposProfileNameScale = "DL-Magellan-9400i-USB-OEM-Scale";
    String jposProfileNameScanner = "DL-Magellan-9400i-USB-OEM-Scanner-Scale";

    public ScannerScaleManager(Scanner sc, Scale scal){
        scanner=sc;
        scale=scal;

        if (!connectScale()) {
            System.exit(1);
        }
        if (!connectScanner()) {
            System.exit(1);
        }
    }



    /**
     * Connects scanning device, enabling barcode reading.
     *
     * Should always follow flow of open -> claim -> enable -> enable data
     *
     *
     * @return boolean indicating connection status
     */
    public boolean connectScanner() {
        String profile=jposProfileNameScanner;
        System.out.println("INFO: Connecting to scanner...");

        //open the jpos.xml profile for the desired scanner
        try {
            scanner.open(profile);
        } catch (JposException je) {
            System.err.println("ERROR: Failed to open " + profile + " profile, " + je);
            return false;
        }

        //claim the scanner with a timeout of one second.
        try {
            scanner.claim(1000);
        } catch (JposException je) {
            //handle failed claim here
            closeScanner();
            System.err.println("ERROR: Failed to claim, " + je);
            return false;
        }

        //enable barcode reading for scanning device.
        try {
            scanner.setDeviceEnabled(true);
        } catch (JposException je) {
            //handle failed enable here, should release the scanner before
            //calling close. For this example, we just call close.
            closeScanner();
            System.err.println("ERROR: Failed to enable, " + je);
            return false;
        }

        //enable data events for the scanner instance. This is necessary to
        //retrieve the barcode data.
        try {
            scanner.setDataEventEnabled(true);
        } catch (JposException je) {
            //handle failed enable here, should disable and release the
            //scanner before calling close. For this example, we just call
            //close.
            closeScanner();
            System.err.println("ERROR: Failed to enable data, " + je);
            return false;
        }

        //add this class as a data listener for the scanner to receive barcode
        //data events.
       // scanner.addDataListener(this);

        System.out.println("INFO: Scanner connected.");
        return true;
    }

    /**
     * Connects scale device, enabling live weight reading.
     *
     * Should always follow flow of:
     * open -> claim -> enable status notify -> enable
     *
     *
     * @return boolean indicating connection status
     */
    public boolean connectScale() {

        String profile=jposProfileNameScale;

        System.out.println("INFO: Connecting to scale...");

        //open the jpos.xml profile for the desired scale
        try {
            scale.open(profile);
        } catch (JposException je) {
            System.err.println("ERROR: Failed to open " + profile + " profile, " + je);
            return false;
        }

        //claim the scale with a timeout of one second.
        try {
            scale.claim(1000);
        } catch (JposException je) {
            //handle failed claim here
            closeScale();
            System.err.println("ERROR: Failed to claim, " + je);
            return false;
        }

        //get configuration data
       // getScaleInfo();

        //enables status notify for the scale instance. This must be done before
        //enabling the scale for live weight to start.
        try {
            scale.setStatusNotify(ScaleConst.SCAL_SN_ENABLED);
        } catch (JposException je) {
            //handle failed status notify enable here, should release the scale
            //before calling close. For this example, we just call close.
            closeScale();
            System.err.println("ERROR: Failed to enable status notify, " + je);
            return false;
        }

        //enable weight reading for scale device.
        try {
            scale.setDeviceEnabled(true);
        } catch (JposException je) {
            //handle failed enable here, should release the scale before
            //calling close. For this example, we just call close.
            closeScale();
            System.err.println("ERROR: Failed to enable, " + je);
            return false;
        }

        //add this class as a status update listener for the scale to recieve
        //live weight data
       // scale.addStatusUpdateListener(this);

        System.out.println("INFO: Scale connected.");
        return true;
    }


    /**
     * Disconnects scale device, disabling live weight reading.
     *
     * Should always follow flow of:
     * disable status notify -> disable -> release -> close
     *
     */
    public void disconnectScale() {
        System.out.println("INFO: Disconnecting scale...");

        //remove this class as a status update event listener.
       // scale.removeStatusUpdateListener(this);

        //for this example, going to ignore exception handling. For actual
        //applications, a similar format of handling each statement found in
        //connectScale() should be followed.
        try {
            scale.setStatusNotify(ScaleConst.SCAL_SN_DISABLED);
            scale.setDeviceEnabled(false);
            scale.release();
            scale.close();
        } catch (JposException je) {
            //ignoring exceptions for this example
        }

        System.out.println("INFO: Scale disconnected.");
    }

    /**
     * Disconnects scanning device, disabling barcode reading.
     *
     * Should always follow flow of disable -> release -> close
     * (though you could choose not to disable device before releasing on
     * certain interfaces)
     *
     */
    public void disconnectScanner() {
        System.out.println("INFO: Disconnecting scanner...");

        //remove this class as a data event listener.
       // scanner.removeDataListener(this);

        //for this example, going to ignore exception handling. For actual
        //applications, a similar format of handling each statement found in
        //connectScanner() should be followed.
        try {
            scanner.setDeviceEnabled(false);
            scanner.release();
            scanner.close();
        } catch (JposException je) {
            //ignoring exceptions for this example
        }

        System.out.println("INFO: Scanner disconnected.");
    }

    /**
     * Convenience method for this example.
     */
    private void closeScanner() {
        try {
            scanner.close();
        } catch (JposException je) {
            //ignoring exception for this example
        }
    }

    /**
     * Convenience method for this example.
     */
    private void closeScale() {
        try {
            scale.close();
        } catch (JposException je) {
            //ignoring exception for this example
        }
    }
}
