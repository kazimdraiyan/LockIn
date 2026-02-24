package app.lockin.lockin.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

// Does auth related backend tasks
public class AuthService {
    // TODO: Should I put the database inside resources folder?
    private static final String databasePath = "src/main/resources/app/lockin/lockin/database/";

    ObjectMapper mapper = new ObjectMapper();

    public void createUser(String username, String password) throws IOException {
        // TODO: Create file when database folder, or users.json file does not exist
        // TODO: Fix when file is empty instead of {}
        ObjectNode usersDatabase = (ObjectNode) mapper.readTree(new File(databasePath + "users.json"));

        // Check if username already exists
        for (Iterator<String> it = usersDatabase.fieldNames(); it.hasNext(); ) {
            String savedUsername = it.next();
            if (savedUsername.equals(username)) {
                throw new IOException("Username is already in use"); // TODO: Use custom exceptions
            }
        }

        ObjectNode user = mapper.createObjectNode();
        // TODO: Use hashes instead of storing the password in plaintext
        user.put("password", password);
        usersDatabase.set(username, user);

        System.out.println("User successfully created");

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(
                        new File(databasePath + "users.json"),
                        usersDatabase
                );
    }
}
