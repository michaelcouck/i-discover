package discover;

import com.jcraft.jsch.JSchException;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class SshServerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshServerTest.class);

    @Test
    public void main() throws JSchException, IOException {
        SshServer.main(null);

        String userid = "michael";
        String host = "localhost";
        int port = 2222;
        String password = "password";

        /*Properties props = new Properties();
        props.put("StrictHostKeyChecking", "no");*/

        SSHClient sshClient = new SSHClient();
        sshClient.setTimeout(Integer.MAX_VALUE);
        // int maxPacketSize = sshClient.getConnection().getMaxPacketSize();
        sshClient.setConnectTimeout(Integer.MAX_VALUE);
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        sshClient.connect(host, port);
        sshClient.authPassword(userid, password.toCharArray());
        // sshClient.useCompression();

        Session session = sshClient.startSession();

        /*Session.Shell shell = session.startShell();
        OutputStream outputStream = shell.getOutputStream();
        outputStream.write("ssh mkdir remote-directory".getBytes());
        outputStream.flush();*/
        // shell.join();
        // sshClient.close();

        // LOGGER.error(IOUtils.readLines(shell.getInputStream()).toString());

        Session.Command command;

        command = sshClient.startSession().exec("pwd");
        command = sshClient.startSession().exec("ls -lh");
        command = sshClient.startSession().exec("df -hT");
        command = sshClient.startSession().exec("mkdir remote-directory");

        // LOGGER.info(IOUtils.readLines(command.getInputStream()).toString());
        // LOGGER.info(IOUtils.readLines(command.getInputStream()).toString());
    }

}
