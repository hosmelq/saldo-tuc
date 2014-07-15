package com.socialimprover.saldotuc;

import java.io.Serializable;
import java.util.List;

public class Records implements Serializable {

    public List<Record> data;

    public class Record implements Serializable {
        public double balance;
        public double spending;
        public String card;
        public String created_at;
    }

}
