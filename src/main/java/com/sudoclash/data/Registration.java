package com.sudoclash.data;

// What a new or returning player gets back from the data layer: the account.
public class Registration {
    private final Account account;

    public Registration(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
}
