package com.socialimprover.saldotuc.models;

import java.io.Serializable;

public class Card implements Serializable {

    private Integer id;
    private String name;
    private String number;
    private String phone;
    private String hour;
    private String ampm;
    private String balance;

    public Card() {}

    public Card(Integer id, String name, String number, String balance) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.balance = balance;
    }

    public Card(Integer id, String name, String number, String phone, String hour, String ampm, String balance) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.phone = phone;
        this.hour = hour;
        this.ampm = ampm;
        this.balance = balance;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getAmpm() {
        return ampm;
    }

    public void setAmpm(String ampm) {
        this.ampm = ampm;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }


}
