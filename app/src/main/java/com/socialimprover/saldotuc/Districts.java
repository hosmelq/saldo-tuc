package com.socialimprover.saldotuc;

import java.io.Serializable;
import java.util.List;

public class Districts implements Serializable {

    public List<District> data;

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

}
