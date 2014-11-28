package com.socialimprover.saldotuc.models;

import java.io.Serializable;

public class District implements Serializable {
    public String _id;
    public String name;
    public Integer page;

    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public Integer getPage() {
        return page;
    }
}