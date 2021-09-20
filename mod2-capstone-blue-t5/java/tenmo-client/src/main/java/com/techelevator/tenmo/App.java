package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.TransferRequestService;
import com.techelevator.view.ConsoleService;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;


public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
    private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
    private static final String[] LOGIN_MENU_OPTIONS = {LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT};
    private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
    private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
    private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
    private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
    private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
    private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
    private static final String[] MAIN_MENU_OPTIONS = {MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT};

    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private TransferRequestService transferRequestService = new TransferRequestService(API_BASE_URL);
    private RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
        app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService) {
        this.console = console;
        this.authenticationService = authenticationService;
    }

    public void run() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");

        registerAndLogin();
        mainMenu();
    }

    private void mainMenu() {
        while (true) {
            String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
            if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
                viewCurrentBalance();
            } else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
                viewTransferHistory();
            } else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
                viewPendingRequests();
            } else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
                sendBucks();
            } else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
                requestBucks();
            } else if (MAIN_MENU_OPTION_LOGIN.equals(choice)) {
                login();
            } else {
                // the only other option on the main menu is to exit
                exitProgram();
            }
        }
    }

    private void viewCurrentBalance() {
        transferRequestService.getBalance(currentUser.getToken());
        System.out.print("Press [Enter] to return to Main Menu");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    private void viewTransferHistory() {
        String choiceIfById = transferRequestService.getTransfers(currentUser.getToken(), currentUser.getUser().getUsername());

        if (choiceIfById.equals("0")) {
            mainMenu();
        } else {
            transferRequestService.getTransfersById(currentUser.getToken(), choiceIfById);
            System.out.print("\nPress [Enter] to return to Transfer History");
            try {
                System.in.read();
                System.out.println();
                viewTransferHistory();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void viewPendingRequests() {
        // TODO Auto-generated method stub
    }

    //WIP. Full send function. To be moved to TransferRequestService. Server-side currently doesn't accept decimals.
    private void sendBucks() {

        String choiceRecipient = transferRequestService.sendFindUser(currentUser.getToken(), currentUser.getUser().getUsername());

        String result="";
        if (choiceRecipient.equals("0")) {
            mainMenu();
        } else {
            result = transferRequestService.sendBuilder(currentUser.getToken(), currentUser.getUser().getId(), choiceRecipient);
        }
        System.out.println();
        System.out.println("---------");
        System.out.println(result);

        if (!result.equals("Transfer Completed")) {
            System.out.println("---------");
            System.out.println();
            System.out.print("Press [Enter] to return to Main Menu");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void requestBucks() {
        // TODO Auto-generated method stub

    }

    private void exitProgram() {
        System.exit(0);
    }

    private void registerAndLogin() {
        while (!isAuthenticated()) {
            String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
            if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
                login();
            } else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
                register();
            } else {
                // the only other option on the login menu is to exit
                exitProgram();
            }
        }
    }

    private boolean isAuthenticated() {
        return currentUser != null;
    }

    private void register() {
        System.out.println("Please register a new user account");
        boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
                authenticationService.register(credentials);
                isRegistered = true;
                System.out.println("Registration successful. You can now login.");
            } catch (AuthenticationServiceException e) {
                System.out.println("REGISTRATION ERROR: " + e.getMessage());
                System.out.println("Please attempt to register again.");
            }
        }
    }

    private void login() {
        System.out.println("Please log in");
        currentUser = null;
        while (currentUser == null) //will keep looping until user is logged in
        {
            UserCredentials credentials = collectUserCredentials();
            try {
                currentUser = authenticationService.login(credentials);
            } catch (AuthenticationServiceException e) {
                System.out.println("LOGIN ERROR: " + e.getMessage());
                System.out.println("Please attempt to login again.");
            }
        }
    }

    private UserCredentials collectUserCredentials() {
        String username = console.getUserInput("Username");
        String password = console.getUserInput("Password");
        return new UserCredentials(username, password);
    }
}
