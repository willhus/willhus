package client;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import server.models.Course;
import server.models.RegistrationForm;

public class Client {
    public final static String REGISTER_COMMAND = "INSCRIRE";
    public final static String LOAD_COMMAND = "CHARGER";

    private final String host;
    private final int port;

    private String semester;
    private List<Course> courses;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private static final HashMap<String, String> SEMESTERS = new HashMap<>();

    static {
        SEMESTERS.put("1", "Automne");
        SEMESTERS.put("2", "Hiver");
        SEMESTERS.put("3", "Ete");
    }

    public void start() {
        try (Socket socket = new Socket(host, port);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {

            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            System.out.println("*** Bienvenue au portail d'inscription de cours de l'UDEM ***");

            while (running) {
                // Choose a semester
                String choice = this.chooseSemester(scanner);
                this.semester = SEMESTERS.get(choice);
                outputStream.writeObject(LOAD_COMMAND + " " + this.semester);
                outputStream.flush();

                // Return or choose a course
                this.courses = (List<Course>) inputStream.readObject();
                choice = this.chooseRegOrBack(scanner);

                if (choice.equals("1")) {
                    continue;
                }

                // Choose a course
                RegistrationForm form = this.chooseCourse(scanner);
                outputStream.writeObject(REGISTER_COMMAND);
                outputStream.flush();
                outputStream.writeObject(form);
                outputStream.flush();
                System.out.printf("Félicitations! Inscription réussie de %s au cours de %s.%n",
                form.getPrenom(), form.getCourse().getCode());
            }

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 1337);
        client.start();
    }

    private String chooseSemester(Scanner scanner) {
        while (true) {
            System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste des cours:");
            SEMESTERS.forEach((k, v) -> System.out.printf("%s. %s%n", k, v));
            System.out.printf("> Choix: ");
            String choice = scanner.nextLine();
            if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
                System.out.println("Erreur: choix invalide");
            } else {
                return choice;
            }
        }
    }

    private String chooseRegOrBack(Scanner scanner) {
        while (true) {
            System.out.printf("Les cours offerts pendant la session d'%s sont:%n", this.semester.toLowerCase());
            int ct = 1;
            for (Course course: this.courses) {
                System.out.printf("%d. %s%n", ct++, course.getName());
            }
            System.out.println("> Choix:");
            System.out.println("1. Consulter les cours offerts pour une autre session");
            System.out.println("2. Inscription à un cours");
            System.out.printf("> Choix: ");
            String choice = scanner.nextLine();
            if (!choice.equals("1") && !choice.equals("2")) {
                System.out.println("Erreur: choix invalide");
            } else {
                return choice;
            }
        }
    }

    private RegistrationForm chooseCourse(Scanner scanner) {
        while (true) {
            System.out.println("Veuillez saisir votre prénom:");
            String firstName = scanner.nextLine();
            System.out.println("Veuillez saisir votre nom:");
            String lastName = scanner.nextLine();
            System.out.println("Veuillez saisir votre email:");
            String email = scanner.nextLine();
            System.out.println("Veuillez saisir votre matricule:");
            String studentNumber = scanner.nextLine();
            System.out.println("Veuillez saisir le code du cours:");
            String courseCode = scanner.nextLine();
            
            // Verify that course code is valid
            for (Course course: this.courses) {
                if (course.getCode().equals(courseCode)) {
                    RegistrationForm form = new RegistrationForm(firstName, lastName, email, studentNumber, course);
                    return form;
                }
            }

            // Course not found
            System.out.println("Erreur: code de cours invalide");
        }
    }
}
