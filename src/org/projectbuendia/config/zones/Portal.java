package org.projectbuendia.config.zones;

public final class Portal {

    private final String name;
    private final String macAddressIn;
    private final String macAddressOut;

    public final String getName() {
        return name;
    }
    public final String getMacAddressIn() {
        return this.macAddressIn;
    }
    public final String getMacAddressOut() {
        return this.macAddressOut;
    }

    /**
     * Initializes the portal object inside a hashmap in a {@link Tent} object
     * in a {@link Zone} object which is read from {@link org.projectbuendia.config.DatabaseConfigs}
     *
     * <p>
     * This method is final and should never be overwritten.
     *
     * @param  name  the name of the portal
     * @return      this
     * @see         Portal
     */

    public Portal (String name, String macAddressIn, String macAddressOut) {
        this.name = name;
        this.macAddressIn = macAddressIn;
        this.macAddressOut = macAddressOut;
    }
}
