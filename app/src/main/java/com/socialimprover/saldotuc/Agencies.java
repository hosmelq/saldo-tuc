package com.socialimprover.saldotuc;

import java.io.Serializable;
import java.util.List;

public class Agencies implements Serializable {

    public List<Agency> data;

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

}
