package com.socialimprover.saldotuc.models;

import java.io.Serializable;

public class Agency implements Serializable {
    public String address;
    public String name;
    public String neighborhood;

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getNeighborhood() {
        return neighborhood;
    }
}