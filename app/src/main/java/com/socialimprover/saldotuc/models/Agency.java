package com.socialimprover.saldotuc.models;

import java.io.Serializable;

public class Agency implements Serializable {
    public Integer id;
    public String address;
    public String name;
    public String neighborhood;

    public Agency(Agency agency) {
        this.id = agency.id;
        this.address = agency.address;
        this.name = agency.name;
        this.neighborhood = agency.neighborhood;
    }

    public Agency(int id, String address, String name, String neighborhood) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.neighborhood = neighborhood;
    }

    public Integer getId() {
        return id;
    }

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