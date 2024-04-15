/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package interfaces;

import java.util.Collection;
import java.util.Random;

import core.*;

/**
 * A simple Network Interface that provides a variable transmission range in a period
 */
public class DroneBroadcastInterface extends NetworkInterface {
    /** transmit range -setting id ({@value})*/
    public static final String INIT_TRANSMIT_RANGE_S = "initTransmitRange";
    protected double initTransmitRange;
    protected double currentTransmitRange;

    /**
     * Reads the interface settings from the Settings file
     */
    public DroneBroadcastInterface(Settings s)	{
        super(s);
        this.initTransmitRange = s.getDouble(INIT_TRANSMIT_RANGE_S);
        ensurePositiveValue(initTransmitRange, INIT_TRANSMIT_RANGE_S);

    }

    /**
     * Copy constructor
     * @param ni the copied network interface object
     */
    public DroneBroadcastInterface(DroneBroadcastInterface ni) {
        super(ni);
        this.transmitRange = ni.transmitRange;
        this.transmitSpeed = ni.transmitSpeed;
        this.currentTransmitRange = ni.initTransmitRange;
        this.initTransmitRange = ni.initTransmitRange;

    }

    public NetworkInterface replicate()	{
        return new DroneBroadcastInterface(this);
    }

    /**
     * Returns the transmit speed of this network layer
     * @return the transmit speed
     */
    @Override
    public double getTransmitRange() {
        return this.currentTransmitRange;
    }

    /**
     * Tries to connect this host to another host. The other host must be
     * active and within range of this host for the connection to succeed.
     * @param anotherInterface The host to connect to
     */
    public void connect(NetworkInterface anotherInterface) {
        if (isScanning()
                && anotherInterface.getHost().isRadioActive()
                && isWithinRange(anotherInterface)
                && !isConnected(anotherInterface)
                && (this != anotherInterface)) {
            // new contact within range
            // connection speed is the lower one of the two speeds
            int conSpeed = anotherInterface.getTransmitSpeed();
            if (conSpeed > this.transmitSpeed) {
                conSpeed = this.transmitSpeed;
            }

            Connection con = new CBRConnection(this.host, this,
                    anotherInterface.getHost(), anotherInterface, conSpeed);
            connect(con,anotherInterface);
        }
    }

    /**
     * Updates the state of current connections (i.e., tears down connections
     * that are out of range).
     */
    public void update() {
        if (optimizer == null) {
            return; /* nothing to do */
        }

        // First break the old ones
        optimizer.updateLocation(this);
        for (int i=0; i<this.connections.size(); ) {
            Connection con = this.connections.get(i);
            NetworkInterface anotherInterface = con.getOtherInterface(this);

            // all connections should be up at this stage
            assert con.isUp() : "Connection " + con + " was down!";

            if (!isWithinRange(anotherInterface)) {
                disconnect(con,anotherInterface);
                connections.remove(i);
            } else {
                i++;
            }
        }

        // Then find new possible connections
        Collection<NetworkInterface> interfaces =
                optimizer.getNearInterfaces(this);
        for (NetworkInterface i : interfaces)
            connect(i);


        double currentConRange = getTransmitRange();
        double connRange = updateTransmitRange();
        if (Math.abs(currentConRange - connRange) < 0.5) {
            double calc = Math.abs(currentConRange - connRange);
            System.out.println("O valor de variacao do drone eh: " + calc);
            this.currentTransmitRange = connRange;
        }

    }

    /**
     * Creates a connection to another host. This method does not do any checks
     * on whether the other node is in range or active
     * @param anotherInterface The interface to create the connection to
     */
    public void createConnection(NetworkInterface anotherInterface) {
        if (!isConnected(anotherInterface) && (this != anotherInterface)) {
            // connection speed is the lower one of the two speeds
            int conSpeed = anotherInterface.getTransmitSpeed();
            if (conSpeed > this.transmitSpeed) {
                conSpeed = this.transmitSpeed;
            }

            Connection con = new CBRConnection(this.host, (NetworkInterface) this,
                    anotherInterface.getHost(), anotherInterface, conSpeed);
            connect(con,anotherInterface);
        }
    }

    /**
     * Updates transmit range according to altitude
     */

    public double updateTransmitRange(){
        // Eu preciso determinar tempo aqui para saber quando mudar a altitude?
        Random random = new Random();
        double maxRange = this.transmitRange;
        int initAltitude = (int)this.initTransmitRange;
        double newRange = initAltitude + (maxRange - initAltitude) * random.nextDouble();
//        System.out.println("MAX range is: " + maxRange);
//        System.out.println("initAltitude range is: " + initAltitude);

        return newRange;
    }


    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString() {
        return "DroneBroadcastInterface " + super.toString();
    }


}

