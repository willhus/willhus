package client;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import server.models.Course;
import server.models.RegistrationForm;

/**
 * Classe Client: console-based client
 * 
 * Setup: Il suffit de simplement run ce fichier, et le client va commencer.
 * 1- L'utilisateur doit d'abord choisir son semestre.
 * 2- Par la suite, les cours associés à ce semestre seront affichés à l'écran.
 * 3- L'utilisateur a ensuite l'option de choisir de s'inscrire à un des cours du semestre en entrant les informations demandées, ou il peut retourner à 1-
 */
public class Client {
    public final static String REGISTER_COMMAND = "INSCRIRE";
    public final static String LOAD_COMMAND = "CHARGER";
    public final static String QUIT_COMMAND = "QUITTER";

    private final String host;
    private final int port;

    private String semester;
    private List<Course> courses;

    /**
     * Constructeur pour le Client.
     * @param host, l'hôte pour la connection
     * @param port, le port à choisir pour établir la connection
     */
    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Liste des semestres
    private static final HashMap<String, String> SEMESTERS = new HashMap<>();
    static {
        SEMESTERS.put("1", "Automne");
        SEMESTERS.put("2", "Hiver");
        SEMESTERS.put("3", "Ete");
    }

    /**
     * Fonction principale du client, qui exécute une boucle infinie pour voir les semestres et les cours associés, et pour s'inscrire aux cours.
     */
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

                if (choice.equalsIgnoreCase("q")) {
                    outputStream.writeObject(QUIT_COMMAND);
                    break;
                }

                this.semester = SEMESTERS.get(choice);
                outputStream.writeObject(LOAD_COMMAND + " " + this.semester);
                outputStream.flush();

                // Return or choose a course
                this.courses = (List<Course>) inputStream.readObject();
                choice = this.chooseRegOrBack(scanner);

                if (choice.equalsIgnoreCase("q")) {
                    outputStream.writeObject(QUIT_COMMAND);
                    break;
                }
                
                if (choice.equals("1")) {
                    continue;
                }

                // Choose a course
                RegistrationForm form = this.chooseCourse(scanner);

                if (form == null) {
                    outputStream.writeObject(QUIT_COMMAND);
                    break;
                }

                outputStream.writeObject(REGISTER_COMMAND);
                outputStream.flush();
                outputStream.writeObject(form);
                outputStream.flush();
                System.out.printf("Félicitations! Inscription réussie de %s au cours de %s.%n",
                form.getPrenom(), form.getCourse().getCode());

                // Decide if you want to continue or exit
                System.out.println("Voulez-vous continuer? (O/N)");
                String decision = scanner.nextLine();
                if (decision.equalsIgnoreCase("N")) {
                    running = false;
                } else {
                    System.out.println();
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Le main qui crée un client et qui le démarre
     * @param args, pas utilisé
     */
    public static void main(String[] args) {
        Client client = new Client("localhost", 1337);
        client.start();
    }

    /**
     * Fonction pour choisir un semestre
     * @param scanner pour lire le user input
     * @return choice, le choix du semestre
     * @return Q, pour quitter le client
     */
    private String chooseSemester(Scanner scanner) {
        while (true) {
            System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste des cours:");
            SEMESTERS.forEach((k, v) -> System.out.printf("%s. %s%n", k, v));
            System.out.printf("> Choix: ");
            String choice = scanner.nextLine();
            if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3") && !choice.equalsIgnoreCase("q")) {
                System.out.printf("Erreur: choix invalide%n%n");
            } else {
                return choice;
            }
        }
    }

    /**
     * Fonction pour choisir de s'inscrire ou retourner
     * @param scanner pour lire le user input
     * @return choice, le choix de l'utilisateur
     * @return Q, pour quitter le client
     */
    private String chooseRegOrBack(Scanner scanner) {
        while (true) {
            System.out.printf("Les cours offerts pendant la session d'%s sont:%n", this.semester.toLowerCase());
            int ct = 1;
            for (Course course: this.courses) {
                System.out.printf("%d. %s\t%s%n", ct++, course.getCode(), course.getName());
            }
            System.out.println("> Choix:");
            System.out.println("1. Consulter les cours offerts pour une autre session");
            System.out.println("2. Inscription à un cours");
            System.out.printf("> Choix: ");
            String choice = scanner.nextLine();
            if (!choice.equals("1") && !choice.equals("2") && !choice.equalsIgnoreCase("q")) {
                System.out.printf("Erreur: choix invalide%n%n");
            } else {
                return choice;
            }
        }
    }

    /**
     * Fonction pour choisir un cours
     * @param scanner pour lire le user input
     * @return choice, le choix du cours
     * @return Q, pour quitter le client
     */
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
            System.out.println("Voulez-vous quitter? (O/N)");
            if (scanner.nextLine().equalsIgnoreCase("o")) {
                return null;
            } else {
                System.out.println();
            }
        }
    }
}
