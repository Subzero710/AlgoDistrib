package ensisa;

import ensisa.process.InputMessage;
import ensisa.process.OutputMessage;
import ensisa.process.Process;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Point 10.2 : chaque suiveur conserve les identifiants lus dans les messages.
 * La remontee des identifiants vers le pere sera ajoutee au point 10.3.
 */
public class Echo extends Process {
    private static final int DIFFUSION = 1;
    private static final String TEXTE = "Bonjour depuis le processus 0";

    // Etat local du processus : il sera reutilise pour tous les futurs messages recus.
    private final Set<Integer> identifiantsMemorises = new HashSet<>();

    public Echo() throws IOException {
    }

    public static void main(String[] args) throws Exception {
        new Echo().start();
    }

    @Override
    public void run() throws Exception {
        if (id == 0) {
            Set<Integer> identifiants = new HashSet<>();
            identifiants.add(id);
            afficher(TEXTE);
            diffuser(TEXTE, identifiants, -1);
        } else {
            InputMessage message = receive();
            if (message.type != DIFFUSION) {
                throw new IllegalStateException("Type de message inattendu : " + message.type);
            }
            String texte = message.readString();
            int taille = message.readInt();
            for (int i = 0; i < taille; i++) {
                identifiantsMemorises.add(message.readInt());
            }
            afficher(texte);
            System.out.println("Processus " + id + " - identifiants memorises : " + identifiantsMemorises);
            diffuser(texte, identifiantsMemorises, message.senderProcessId);
        }
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
            message.writeInt(identifiants.size());
            for (int identifiant : identifiants) {
                message.writeInt(identifiant);
            }
            send(voisin, message);
        }
    }
}
