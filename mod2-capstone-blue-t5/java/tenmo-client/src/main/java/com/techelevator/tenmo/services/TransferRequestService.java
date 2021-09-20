package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferRequest;
import com.techelevator.tenmo.model.User;
import org.springframework.http.*;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Scanner;

public class TransferRequestService {

    private String url;
    private RestTemplate restTemplate = new RestTemplate();

    public TransferRequestService(String url) {
        this.url = url;
    }

    public void getBalance(String token) {
        BigDecimal balance = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<BigDecimal> entity = new HttpEntity<>(headers);
try {
    balance = restTemplate.exchange(url + "api/balance", HttpMethod.GET, entity, BigDecimal.class).getBody();

    System.out.println();
    System.out.println("--------");
    System.out.println("Your current account balance is: $" + balance);
    System.out.println("--------");
    System.out.println();
} catch (ResourceAccessException e){
    System.out.println("Unable to connect to server. Please try again later.");
    System.out.println();

}
    }

    public String getTransfers(String token, String currentUser) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Transfer> entity = new HttpEntity<>(headers);

        Transfer[] transfersArr =  restTemplate.exchange(url + "api/transfer/history", HttpMethod.GET, entity, Transfer[].class).getBody();

        System.out.println("-------------------------------------------");
        System.out.println("Transfers");
        System.out.printf("%-10s %-20s %-13s", "ID", "From/To", "Amount");
        System.out.println();
        System.out.println("-------------------------------------------");


        if (transfersArr.length != 0) {
            for (Transfer t : transfersArr) {
                if (t.getUserTo().equals(currentUser)) {
                    System.out.printf("%-10s %-20s %-13s", t.getTransferID(), "From: " + t.getUserFrom(), "$" + t.getAmount());
                } else {
                    System.out.printf("%-10s %-20s %-13s", t.getTransferID(), "To: " + t.getUserTo(), "$" + t.getAmount());
                }
                System.out.println();
            }
        } else {
            System.out.println("You have no transaction history.");
        }


        System.out.println("--------");

        System.out.print("Please enter transfer ID to view details (0 to cancel): ");
        Scanner userInput = new Scanner(System.in);

        return userInput.nextLine();
    }

    public void getTransfersById(String token, String choice) {
        Scanner userInput = new Scanner(System.in);
        String choiceCheck = choice;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Transfer> entity = new HttpEntity<>(headers);

        Transfer[] transfersArr = restTemplate.exchange(url + "api/transfer/history", HttpMethod.GET, entity, Transfer[].class).getBody();

        int badCount = 0;
        boolean badInput = true;
        while (badInput) {
            int validChoice = idCheck(choiceCheck);
            choiceCheck = String.valueOf(validChoice);
            for (Transfer t : transfersArr) {
                if (t.getTransferID() == validChoice) {
                    System.out.println(t.toString());
                    badInput = false;
                } else {
                    badCount++;
                }
            }

            if (badCount == transfersArr.length) {
                System.out.print("Invalid ID. Please try again >>> ");
                choiceCheck = userInput.nextLine();
            }
        }

    }

    public String sendFindUser(String token, String currentUser) {
        HttpHeaders sendGetHeaders = new HttpHeaders();
        sendGetHeaders.setContentType(MediaType.APPLICATION_JSON);
        sendGetHeaders.setBearerAuth(token);
        HttpEntity<User> sendGetEntity = new HttpEntity<>(sendGetHeaders);
        User[] users = restTemplate.exchange(url + "/api/users", HttpMethod.GET, sendGetEntity, User[].class).getBody();

        System.out.println("-------------------------------------------");
        System.out.println("Users");
        System.out.println("ID          Name");
        System.out.println("-------------------------------------------");
        for (User u : users) {
            if (!u.getUsername().equals(currentUser)) {
                System.out.println(u.getId() + "        " + u.getUsername());
            }
        }
        System.out.println("---------");

        System.out.print("Please enter the recipient User ID (0 to cancel): ");
        Scanner userInput = new Scanner(System.in);
        String choice = userInput.nextLine();

        return choice;
    }

    public String sendBuilder(String token, int currentID, String recipientUserID) {
        TransferRequest transferRequest = new TransferRequest(0, new BigDecimal("0"));

        Scanner userInput = new Scanner(System.in);

        int validID = idCheck(recipientUserID);

        while (currentID == validID) {
            System.out.print("Invalid recipient. You can't send money to yourself. Please try again >>> ");
            recipientUserID = userInput.nextLine();
            validID = idCheck(recipientUserID);
        }


        System.out.print("Enter transfer amount: $");
        userInput = new Scanner(System.in);
        String selectedAmount = userInput.nextLine();
        String validSelectedAmount = valueCheck(selectedAmount);
        BigDecimal amount = new BigDecimal(validSelectedAmount);

        transferRequest = new TransferRequest(Integer.parseInt(recipientUserID), amount);

        HttpHeaders sendPostHeaders = new HttpHeaders();
        sendPostHeaders.setContentType(MediaType.APPLICATION_JSON);
        sendPostHeaders.setBearerAuth(token);
        HttpEntity<TransferRequest> sendPostEntity = new HttpEntity<>(transferRequest, sendPostHeaders);

        ResponseEntity<String> result = null;

        try {
            result = restTemplate.exchange(url + "/api/transfer", HttpMethod.POST, sendPostEntity, String.class);
        } catch (RestClientResponseException e) {
            System.out.println(e.getRawStatusCode() + ": " + e.getResponseBodyAsString());
        }
        return result.getBody();
    }

    //This checks that the input doesn't throw a NumberFormatException. Specific methods handle if the numeric value is actually in the server
    // request.
    private int idCheck(String id) {
        Scanner userInput = new Scanner(System.in);
        String choice = id;
        boolean check = true;
        int choiceCheck = 0;

        while (check) {
            while (choice.length() != 4) {
                System.out.print("Invalid ID. Please try again >>> ");
                choice = userInput.nextLine();
            }
            try {
                choiceCheck = Integer.parseInt(choice);
                check = false;
            } catch (NumberFormatException e) {
                System.out.print("Invalid ID. Please try again >>> ");
                choice = userInput.nextLine();
            }
        }
        return choiceCheck;
    }

    //Another NumberFormatException check
    private String valueCheck(String inputValue) {
        Scanner userInput = new Scanner(System.in);
        String value = inputValue;
        boolean check = true;
        BigDecimal valueCheck = new BigDecimal("0");

        while (check) {
            try {
                valueCheck = new BigDecimal(value);
                check = false;
            } catch (NumberFormatException e) {
                System.out.print("Invalid dollar amount. Please try again >>> $");
                value = userInput.nextLine();
            }
        }
        return String.valueOf(valueCheck);
    }
}

