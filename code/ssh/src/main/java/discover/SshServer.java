package discover;

import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;

import java.io.File;
import java.io.IOException;

public class SshServer {

    public static void main(final String[] args) {
        org.apache.sshd.server.SshServer sshd = org.apache.sshd.server.SshServer.setUpDefaultServer();
        sshd.setPort(2222);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("~/hostkey.ser")));
        sshd.setShellFactory(new ProcessShellFactory(new String[] { "/bin/sh", "-i", "-l" }));
        sshd.setCommandFactory(new ScpCommandFactory());
        // sshd.setCommandFactory(new ScpCommandFactory());
        try {
            sshd.start();
            Thread.sleep(600000);
        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
