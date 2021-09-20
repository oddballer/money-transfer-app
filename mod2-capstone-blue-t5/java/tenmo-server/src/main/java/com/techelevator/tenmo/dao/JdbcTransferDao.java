package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Override
    public BigDecimal viewCurrentBalance(Principal principal) {

        String sql = "SELECT balance FROM accounts a JOIN users u ON u.user_id = a.user_id WHERE username = ?;";

        return jdbcTemplate.queryForObject(sql, BigDecimal.class, principal.getName());
    }

    @Override
    public List<Transfer> viewTransferHistory(Principal principal) {

        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT tx.*, f.username AS userFrom, t.username AS userTo, transfer_type_desc AS transferType, transfer_status_desc AS transferStatus " +
                "FROM transfers tx " +
                "JOIN accounts acc1 ON tx.account_from = acc1.account_id " +
                "JOIN accounts acc2 ON tx.account_to = acc2.account_id " +
                "JOIN users f ON acc1.user_id = f.user_id " +
                "JOIN users t ON acc2.user_id = t.user_id " +
                "JOIN transfer_types tt ON tt.transfer_type_id = tx.transfer_type_id " +
                "JOIN transfer_statuses ts ON ts.transfer_status_id = tx.transfer_status_id " +
                "WHERE acc1.user_id = ? OR acc2.user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, getUserID(principal), getUserID(principal));

        while (results.next()) {
            transfers.add(mapRowToTransfer(results));
        }

        return transfers;
    }

    @Override
    public Transfer getTransfer(int transferID) {
        Transfer transfer = null;
        return transfer;
    }

    @Override
    public List<Transfer> viewPendingRequests(Principal principal) {
        List<Transfer> transfersPending = new ArrayList<>();
        String sql = "SELECT tx.*, f.username AS userFrom, t.username AS userTo, transfer_type_desc AS transferType, transfer_status_desc AS transferStatus " +
                "FROM transfers tx " +
                "JOIN accounts acc1 ON tx.account_from = acc1.account_id " +
                "JOIN accounts acc2 ON tx.account_to = acc2.account_id " +
                "JOIN users f ON acc1.user_id = f.user_id " +
                "JOIN users t ON acc2.user_id = t.user_id " +
                "JOIN transfer_types tt ON tt.transfer_type_id = tx.transfer_type_id " +
                "JOIN transfer_statuses ts ON ts.transfer_status_id = tx.transfer_status_id " +
                "WHERE ts.transfer_status_id = 1 AND tt.transfer_type_id = 1 AND f.user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, getUserID(principal));

        while (results.next()) {
            transfersPending.add(mapRowToTransfer(results));
        }
        return transfersPending;
    }

    //WIP. Method dependent on approve/reject status to update database.
    @Override
    public boolean pendingAcceptReject(int appRejId, int transferID, BigDecimal amount, int accountFrom, int accountTo){

        if (appRejId==2){
            String sqlStatusUpdate = "UPDATE transfers SET transfer_status_id = 2 WHERE transfer_id = ?;";
            jdbcTemplate.update(sqlStatusUpdate, transferID);

            String sqlDec = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?;";
            jdbcTemplate.update(sqlDec, amount, accountFrom);

            String sqlInc = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?;";
            jdbcTemplate.update(sqlInc, amount, accountTo);

            return true;

        } else{
            String sqlStatusUpdate = "UPDATE transfers SET transfer_status_id = 3 WHERE transfer_id = ?;";
            jdbcTemplate.update(sqlStatusUpdate, transferID);
             return false;
        }
    }

    @Override
    public boolean sendBucks(Principal principal, Integer recipientUserID, BigDecimal amount) {

        if (viewCurrentBalance(principal).compareTo(amount) < 0 || getRecipientAccountID(recipientUserID) == null) {

            return false;
        }
        String sqlDec = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?;";
        jdbcTemplate.update(sqlDec, amount, getUserAccountID((principal)));

        String sqlInc = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?;";
        jdbcTemplate.update(sqlInc, amount, getRecipientAccountID(recipientUserID));

        String sqlTransfer = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (2, 2, ?, ?, ?);";
        jdbcTemplate.update(sqlTransfer, getUserAccountID(principal), getRecipientAccountID(recipientUserID), amount);

        return true;
    }

    @Override
    public boolean requestBucks(int sendID, int receiveID) {

        return true;
    }

    private Transfer mapRowToTransfer(SqlRowSet results) {
        Transfer transfer = new Transfer();
        transfer.setTransferID(results.getInt("transfer_id"));
        transfer.setTransferTypeID(results.getInt("transfer_type_id"));
        transfer.setTransferStatusID(results.getInt("transfer_status_id"));
        transfer.setAccountFrom(results.getInt("account_from"));
        transfer.setAccountTo(results.getInt("account_to"));
        transfer.setAmount(results.getBigDecimal("amount"));
        transfer.setUserFrom(results.getString("userFrom"));
        transfer.setUserTo(results.getString("userTo"));
        transfer.setTransferType(results.getString("transferType"));
        transfer.setTransferStatus(results.getString("transferStatus"));

        return transfer;

    }

    public int getUserAccountID(Principal principal) throws NullPointerException {

        String sql = "SELECT account_id FROM accounts a JOIN users u ON u.user_id = a.user_id WHERE username ILIKE ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, principal.getName());
    }

    public int getUserID(Principal principal) throws NullPointerException {

        String sql = "SELECT user_id FROM users WHERE username ILIKE ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, principal.getName());
    }

    public String getUsernameFromAccountId(int accountId) throws NullPointerException {

        String sql = "SELECT username FROM users u JOIN accounts a ON a.user_id = u.user_id WHERE account_id = ?;";
        return jdbcTemplate.queryForObject(sql, String.class, accountId);
    }

    public Integer getRecipientAccountID(int userID) throws NullPointerException {

        String sql = "SELECT account_id FROM accounts WHERE user_id = ?;";
        return jdbcTemplate.queryForObject(sql, Integer.class, userID);

    }


}
