package discover;

import ikube.toolkit.FILE;
import org.apache.commons.lang.StringUtils;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class SshServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshServer.class);

    public static void main(final String[] args) {
        org.apache.sshd.server.SshServer sshServer = org.apache.sshd.server.SshServer.setUpDefaultServer();
        sshServer.setPort(2222);
        sshServer.setPasswordAuthenticator((userid, password, serverSession) -> {
            File passwordsFile = FILE.findFileRecursively(new File("."), "passwords.properties");
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(passwordsFile));
                String storedPassword = properties.getProperty(userid);
                if (StringUtils.isNotEmpty(storedPassword) && StringUtils.equals(password, storedPassword)) {
                    return Boolean.TRUE;
                }
            } catch (final IOException e) {
                LOGGER.error("Exception authenticating : " + userid, e);
            }
            return Boolean.FALSE;
        });
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("~/hostkey.ser")));
        sshServer.setShellFactory(new SshSessionFactory());
        sshServer.setCommandFactory(new SshSessionFactory());

        // sshServer.setShellFactory(new ProcessShellFactory("/bin/sh", "-i", "-l"));
        // sshServer.setCommandFactory(new ScpCommandFactory());
        // sshServer.setCommandFactory(command -> new ProcessShellFactory("/bin/bash", "-i", "-l").create());
        try {
            sshServer.start();
        } catch (final IOException e) {
            LOGGER.error("Exception starting the ssh server : ", e);
        }
    }

    static class SshSessionFactory implements CommandFactory, Factory<Command> {

        @Override
        public Command createCommand(String command) {
            return new SshSessionInstance();
        }

        @Override
        public Command create() {
            return createCommand("none");
        }
    }

    static class SshSessionInstance implements Command, Runnable {

        //ANSI escape sequences for formatting purposes
        static final String ANSI_LOCAL_ECHO = "\u001B[12l";
        static final String ANSI_NEWLINE_CRLF = "\u001B[20h";

        static final String ANSI_RESET = "\u001B[0m";
        static final String ANSI_GREEN = "\u001B[0;32m";

        //IO streams for communication with the client
        private InputStream is;
        private OutputStream os;

        //Environment stuff
        @SuppressWarnings("unused")
        private Environment environment;
        private ExitCallback callback;

        private Thread sshThread;

        @Override
        public void start(final Environment env) throws IOException {
            //must start new thread to free up the input stream
            environment = env;
            sshThread = new Thread(this, "EchoShell");
            sshThread.start();
        }

        @Override
        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            try {
                //Make sure local echo is on (because password turned it off
                os.write((ANSI_LOCAL_ECHO + ANSI_NEWLINE_CRLF).getBytes());
                os.flush();

                // TODO: Execute the actual command on the local machine here, for windows cmd.exe and for linux bash probably?
                boolean exit = false;
                String text;
                while (!exit) {
                    text = br.readLine();
                    if (text == null) {
                        exit = true;
                    } else {
                        os.write((ANSI_GREEN + text + ANSI_RESET + "\r\n").getBytes());
                        os.flush();
                        if ("exit".equals(text)) {
                            exit = true;
                        }
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            } finally {
                callback.onExit(0);
            }
        }

        @Override
        public void destroy() throws Exception {
            sshThread.interrupt();
        }

        @Override
        public void setErrorStream(final OutputStream errOS) {
        }

        @Override
        public void setExitCallback(final ExitCallback ec) {
            callback = ec;
        }

        @Override
        public void setInputStream(final InputStream is) {
            this.is = is;
        }

        @Override
        public void setOutputStream(final OutputStream os) {
            this.os = os;
        }
    }

}