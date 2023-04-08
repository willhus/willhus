package server;

import javafx.util.Pair;
import server.exceptions.InvalidLineFormatException;
import server.models.Course;
import server.models.RegistrationForm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Server {

    public final static String REGISTER_COMMAND = "INSCRIRE";
    public final static String LOAD_COMMAND = "CHARGER";
    public final static String QUIT_COMMAND = "QUITTER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    private static final String COURSES_PATH = "willhus/TP2/src/main/java/server/data/cours.txt";
    private static final String INSCRIPTION_PATH = "willhus/TP2/src/main/java/server/data/inscription.txt";
    private static final String COURSE_ERROR_MSG = "Error: invalid line format, expected \"COURSE_ID\tCOURSE_NAME\tSEMESTER\"";
    private static final String SESSION_ERROR_MSG = "Error: invalid line format, expected \"SEMESTER\tCOURSE_ID\tSTUDENT_ID\tFIRST_NAME\tLAST_NAME\tEMAIL\"";

    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void listen() throws IOException, ClassNotFoundException {
        String line;
        while ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        } else if (cmd.equals(QUIT_COMMAND)) {
            System.out.println("Au revoir!");
        } else {
            System.out.println("NOT A VALID COMMAND??");
        }
    }

    /**
     Lire un fichier texte contenant des informations sur les cours et les transofmer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     La méthode gère les exceptions si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet dans le flux.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     */
    public void handleLoadCourses(String arg) {
        try {
            // Open file
            File file = new File(COURSES_PATH);
            Scanner scanner = new Scanner(file);

            // Create list
            System.out.println("Absolute path is: " + file.getAbsolutePath());
            ArrayList<Course> courses = new ArrayList<>();

            // Iterate through lines
            String line;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                String[] values = line.split("\t");
                if (values.length != 3) {
                    scanner.close();
                    throw new InvalidLineFormatException(COURSE_ERROR_MSG);
                }

                // Get courses with selected semester
                if (values[2].equals(arg)) {
                    courses.add(new Course(values[1], values[0], values[2]));
                }
            }

            // Close scanner
            scanner.close();

            // Return list of courses
            this.objectOutputStream.writeObject(courses);
            this.objectOutputStream.flush();
        } catch (IOException | InvalidLineFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un fichier texte
     et renvoyer un message de confirmation au client.
     La méthode gére les exceptions si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier ou dans le flux de sortie.
     */
    public void handleRegistration() {
        try {
            // Read RegistrationForm object from the socket
            RegistrationForm registrationForm = (RegistrationForm) objectInputStream.readObject();
            Course course = registrationForm.getCourse();

            // Write the registration information in the specified format
            String registrationData = String.format("%s\t%s\t%s\t%s\t%s\t%s%n",
                course.getSession(),
                course.getCode(),
                registrationForm.getMatricule(),
                registrationForm.getPrenom(),
                registrationForm.getNom(),
                registrationForm.getEmail()
            );

            // Write the registration data to the file
            try (FileWriter fw = new FileWriter(INSCRIPTION_PATH, true);
                BufferedWriter bw = new BufferedWriter(fw)) {

                bw.write(registrationData);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

