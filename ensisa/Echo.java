package ensisa;

import ensisa.process.InputMessage;
import ensisa.process.OutputMessage;
import ensisa.process.Process;

import java.io.IOException;

/**
 * Etape 2 : diffusion simple du texte depuis le processus 0.
 * L'echo (accuses de reception et terminaison globale) sera ajoute ensuite.
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
            afficher(TEXTE);
            diffuser(TEXTE, -1);
        } else {
            InputMessage message = receive();
            if (message.type != DIFFUSION) {
                throw new IllegalStateException("Type de message inattendu : " + message.type);
            }
            String texte = message.readString();
            afficher(texte);
            diffuser(texte, message.senderProcessId);
        }
    }

    private void afficher(String texte) {
        System.out.println("Processus " + id + " : " + texte);
    }

    private void diffuser(String texte, int pere) throws IOException {
        for (int voisin : outNeighbors) {
            if (voisin == pere) {
                continue;
            }
            OutputMessage message = new OutputMessage(DIFFUSION);
            message.writeString(texte);
            send(voisin, message);
        }
    }
}
