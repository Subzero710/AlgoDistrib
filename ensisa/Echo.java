package ensisa;

import ensisa.process.InputMessage;
import ensisa.process.OutputMessage;
import ensisa.process.Process;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Points 10.3 et 10.4 : les suiveurs renvoient les identifiants au pere,
 * et l'initiateur affiche chaque ensemble recu et leur union.
 */
public class Echo extends Process {
    private static final int DIFFUSION = 1;
    private static final int ECHO = 2;
    private static final String TEXTE = "Bonjour depuis le processus 0";

    private final Set<Integer> identifiantsMemorises = new HashSet<>();

    public Echo() throws IOException {
    }

    public static void main(String[] args) throws Exception {
        new Echo().start();
    }

    @Override
    public void run() throws Exception {
        int pere = -1;
        String texte = TEXTE;

        if (id == 0) {
            identifiantsMemorises.add(id);
        } else {
            // Le premier message de diffusion designe le pere.
            InputMessage premier = receive();
            if (premier.type != DIFFUSION) {
                throw new IllegalStateException("Premier message inattendu : " + premier.type);
            }
            pere = premier.senderProcessId;
            texte = lireDiffusion(premier);
        }

        afficher(texte);
        if (id != 0) {
            System.out.println("Processus " + id + " - identifiants memorises : " + identifiantsMemorises);
        }

        // Un echo est attendu de chaque voisin explore, sauf le pere.
        Set<Integer> enAttente = new HashSet<>(outNeighbors);
        enAttente.remove(pere);
        diffuser(texte, identifiantsMemorises, pere);

        while (!enAttente.isEmpty()) {
            InputMessage message = receive();
            int expediteur = message.senderProcessId;

            if (message.type == DIFFUSION) {
                // Une autre branche nous a deja decouvert : on ne rediffuse pas.
                lireDiffusion(message);
                envoyerEcho(expediteur, Set.of());
            } else if (message.type == ECHO) {
                // Regrouper les identifiants remontes par ce voisin.
                if (!enAttente.remove(expediteur)) {
                    throw new IllegalStateException("ECHO inattendu de " + expediteur);
                }
                Set<Integer> recus = lireIdentifiants(message);
                if (id == 0) {
                    System.out.println("Processus 0 <- processus " + expediteur
                            + " - identifiants recus : " + new java.util.TreeSet<>(recus));
                }
            } else {
                throw new IllegalStateException("Type de message inattendu : " + message.type);
            }
        }

        if (id == 0) {
            System.out.println("Processus 0 - identifiants collectes : "
                    + new java.util.TreeSet<>(identifiantsMemorises));
            System.out.println("Processus 0 : diffusion terminee (tous les echos recus)");
        } else {
            // Le suiveur ajoute son propre numero avant de remonter vers son pere.
            identifiantsMemorises.add(id);
            envoyerEcho(pere, identifiantsMemorises);
            System.out.println("Processus " + id + " -> pere " + pere
                    + " : " + identifiantsMemorises);
        }
    }

    private String lireDiffusion(InputMessage message) throws IOException {
        String texte = message.readString();
        lireIdentifiants(message);
        return texte;
    }

    private Set<Integer> lireIdentifiants(InputMessage message) throws IOException {
        Set<Integer> recus = new HashSet<>();
        int taille = message.readInt();
        for (int i = 0; i < taille; i++) {
            recus.add(message.readInt());
        }
        identifiantsMemorises.addAll(recus);
        return recus;
    }

    private void afficher(String texte) {
        System.out.println("Processus " + id + " : " + texte);
    }

    private void diffuser(String texte, Set<Integer> identifiants, int pere) throws IOException {
        for (int voisin : outNeighbors) {
            if (voisin == pere) {
                continue;
            }
            OutputMessage message = new OutputMessage(DIFFUSION);
            message.writeString(texte);
            ecrireIdentifiants(message, identifiants);
            send(voisin, message);
        }
    }

    private void envoyerEcho(int destinataire, Set<Integer> identifiants) throws IOException {
        OutputMessage message = new OutputMessage(ECHO);
        ecrireIdentifiants(message, identifiants);
        send(destinataire, message);
    }

    private void ecrireIdentifiants(OutputMessage message, Set<Integer> identifiants) throws IOException {
        message.writeInt(identifiants.size());
        for (int identifiant : identifiants) {
            message.writeInt(identifiant);
        }
    }
}
