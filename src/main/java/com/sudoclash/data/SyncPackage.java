package com.sudoclash.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// The data two computers swap when they sync.
public class SyncPackage implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Account> accounts;
    private final List<GameRecord> records;

    public SyncPackage(List<Account> accounts, List<GameRecord> records) {
        this.accounts = new ArrayList<>(accounts);
        this.records = new ArrayList<>(records);
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }

    public List<GameRecord> getRecords() {
        return new ArrayList<>(records);
    }

    public boolean isEmpty() {
        return accounts.isEmpty() && records.isEmpty();
    }
}
