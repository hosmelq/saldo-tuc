package com.socialimprover.saldotuc.models;

import java.io.Serializable;
import java.util.List;

public class Records implements Serializable {

    public List<Record> data;

    public class Record implements Serializable {
        public Number balance;
        public Number spending;
        public String card;
        public String created_at;
    }

}
