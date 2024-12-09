# bet-clj
A Clojure betting API with a built-in database. Its functionalities include depositing any given value into an account, checking the account's balance, placing a bet, and reviewing the account's transaction history.

The directory "api_dmbet" contains the main application. Within the handler, there are POST methods such as adicionar-ao-saldo (adding funds to the account) and realizar-aposta (registering a bet).

Both actions are logged in the db.clj file, which utilizes a Clojure atom to store transaction data. This data can be accessed through one of the GET methods in the handler.

The directory "teste" contains the interface that interacts with the API to place bets. Within its core, you'll find a simple menu and methods for adding funds and placing bets. These functionalities combine the previously mentioned methods from the API with betting data retrieved from another API. For this project, the Betano API, available on RapidAPI, was used.

To test the API independently, you can run the command lein midje in the "api_dmbet" directory. This will execute the test cases in handler_test without requiring external tools like Postman.

To run the entire project:
1: Navigate to the "api_dmbet" directory and execute the command lein ring server-headless in your terminal or Visual Studio Code.
2: Then, navigate to the "teste" directory and run the command lein run.

If all is configured correctly, the program should function as intended.
