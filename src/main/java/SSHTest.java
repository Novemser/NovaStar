import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project: HadoopDFS
 * Package: PACKAGE_NAME
 * Author:  Novemser
 * 2017/1/6
 */
public class SSHTest {
    public static void main(String[] args) throws Exception {
        final SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier((s, i, publicKey) -> true);

        ssh.connect("192.168.52.139", 22);
        try {
            ssh.authPassword("nova", "a19951106");
            final Session session = ssh.startSession();
            try {
                final Command cmd = session.exec("df /home/nova/NovaStar/");
                Scanner scanner = new Scanner(IOUtils.readFully(cmd.getInputStream()).toString());
                scanner.nextLine();
                String line = scanner.nextLine();
                Matcher matcher = Pattern.compile("\\d+").matcher(line);
                int cnt = 0;
                while (matcher.find()) {
                    cnt++;
                    if (cnt == 3)
                        System.out.println(matcher.group());
                }


                cmd.join(5, TimeUnit.SECONDS);
                System.out.println("\n** exit status: " + cmd.getExitStatus());
            } finally {
                session.close();
            }
        } finally {
            ssh.disconnect();
        }
    }
}
