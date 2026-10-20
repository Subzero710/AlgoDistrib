package ensisa;

import ensisa.process.InputMessage;
import ensisa.process.OutputMessage;
import ensisa.process.Process;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Point 10.1 : les messages transportent le texte et un ensemble d'identifiants.
 * La collecte des identifiants et les messages d'echo seront ajoutes ensuite.
 */
public class Echo extends Process {
    private static final int DIFFUSION = 1;
    private static final String TEXTE = "Bonjour depuis le processus 0";

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
            Set<Integer> identifiants = new HashSet<>();
            for (int i = 0; i < taille; i++) {
                identifiants.add(message.readInt());
            }
            afficher(texte);
            diffuser(texte, identifiants, message.senderProcessId);
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
