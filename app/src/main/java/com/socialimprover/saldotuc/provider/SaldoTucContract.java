package com.socialimprover.saldotuc.provider;

public class SaldoTucContract {

    interface CardsColumns {
        String CARD_ID = "_ID";
        String CARD_NAME = "NAME";
        String CARD_CARD = "CARD";
        String CARD_PHONE = "PHONE";
        String CARD_HOUR = "HOUR";
        String CARD_AMPM = "AMPM";
        String CARD_LAST_BALANCE = "LAST_BALANCE";
    }

    interface AgenciesColumns {
        String AGENCY_ID = "_ID";
        String AGENCY_ADDRESS = "ADDRESS";
        String AGENCY_NAME = "NAME";
        String AGENCY_NEIGHBORHOOD = "NEIGHBORHOOD";
    }

}
