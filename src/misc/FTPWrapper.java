package misc;

import de.flashheart.ocfflag.Main;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import javax.swing.*;
import java.io.*;
import java.util.concurrent.ExecutionException;

/**
 * FTP-Utility, basierend auf Apache FTPClient:
 * {@link "http://commons.apache.org/net/apidocs/org/apache/commons/net/ftp/FTPClient.html"}
 */
public class FTPWrapper {
    /**
     * FTP-Dateienliste.
     *
     * @return String-Array der Dateinamen auf dem FTP-Server
     */
    public static String[] list(String host, int port, String usr, String pwd) throws IOException {
        FTPClient ftpClient = new FTPClient();
        String[] filenameList;

        try {
            ftpClient.connect(host, port);
            ftpClient.login(usr, pwd);
            filenameList = ftpClient.listNames();
            ftpClient.logout();
        } finally {
            ftpClient.disconnect();
        }

        return filenameList;
    }

    /**
     * FTP-Client-Download.
     *
     * @return true falls ok
     */
    public static boolean download(String localResultFile, String remoteSourceFile,
                                   String host, int port, String usr, String pwd, boolean showMessages) throws IOException {
        FTPClient ftpClient = new FTPClient();
        FileOutputStream fos = null;
        boolean resultOk = true;
        Logger logger = Logger.getLogger(FTPWrapper.class);

        try {
            ftpClient.connect(host, port);
            if (showMessages) {
                logger.debug(ftpClient.getReplyString());
            }
            resultOk &= ftpClient.login(usr, pwd);
            if (showMessages) {
                logger.debug(ftpClient.getReplyString());
            }
            fos = new FileOutputStream(localResultFile);
            resultOk &= ftpClient.retrieveFile(remoteSourceFile, fos);
            if (showMessages) {
                logger.debug(ftpClient.getReplyString());
            }
            resultOk &= ftpClient.logout();
            if (showMessages) {
                logger.debug(ftpClient.getReplyString());
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {/* nothing to do */}
            ftpClient.disconnect();
        }

        return resultOk;
    }

    /**
     * FTP-Client-Upload.
     *
     * @return true falls ok
     */
    public static boolean upload(String content, boolean move2archive) throws IOException {

        FTPClient ftpClient = new FTPClient();

        if (!Main.getConfigs().isFTPComplete()) return false;

        String host = Main.getConfigs().get(Configs.FTPHOST);
        int port = Integer.parseInt(Main.getConfigs().get(Configs.FTPPORT));
        String usr = Main.getConfigs().get(Configs.FTPUSER);
        String pwd = Main.getConfigs().get(Configs.FTPPWD);
        String remotepath = Main.getConfigs().get(Configs.FTPREMOTEPATH);
        String uuid = Main.getConfigs().get(Configs.MYUUID);

        String activeFile = remotepath + "/ocfflag/active/" + uuid + ".php";

        boolean resultOk = true;
        Logger logger = Logger.getLogger(FTPWrapper.class);


        File tempPHPFile = File.createTempFile("ocfflag", ".php");
        tempPHPFile.deleteOnExit();
        FileUtils.writeStringToFile(tempPHPFile, content, "UTF-8");

        FileInputStream fis = null;


        try {
            ftpClient.connect(host, port);

            logger.debug(ftpClient.getReplyString());

            resultOk &= ftpClient.login(usr, pwd);

            logger.debug(ftpClient.getReplyString());

            fis = new FileInputStream(tempPHPFile);

            if (move2archive) {
                DateTime now = new DateTime();
                String archivefile = remotepath + "/ocfflag/archive/" + now.toString("yyyyMMddHHmmss") + "-" + uuid + ".php";
                resultOk &= ftpClient.storeFile(archivefile, fis);
                ftpClient.deleteFile(activeFile); // egal ob es eine gab oder nicht
            } else {
                resultOk &= ftpClient.storeFile(activeFile, fis);
                logger.debug("Uploading file to :" + activeFile);
            }

            logger.debug(ftpClient.getReplyString());

            resultOk &= ftpClient.logout();

            logger.debug(ftpClient.getReplyString());

        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {/* nothing to do */}
            ftpClient.disconnect();
        }

        return resultOk;
    }

    /**
     * Richtet das FTP Verzeichnis ein.
     * Verschiebt evtl. kaputte oder abgeschlossene PHP Statistiken in das Archivverzeichnis.
     * Dabei benennt es die Datei um.
     *
     * @return true falls ok
     */
    public static boolean initFTPDir() throws IOException {
        FTPClient ftpClient = new FTPClient();

        if (!Main.getConfigs().isFTPComplete()) return false;

        String host = Main.getConfigs().get(Configs.FTPHOST);
        int port = Integer.parseInt(Main.getConfigs().get(Configs.FTPPORT));
        String usr = Main.getConfigs().get(Configs.FTPUSER);
        String pwd = Main.getConfigs().get(Configs.FTPPWD);


        boolean resultOk = true;
        Logger logger = Logger.getLogger(FTPWrapper.class);

        try {
            ftpClient.connect(host, port);

            logger.debug(ftpClient.getReplyString());

            resultOk &= ftpClient.login(usr, pwd);

            logger.debug(ftpClient.getReplyString());

            if (resultOk) {
                DateTime now = new DateTime();
                String uuid = Main.getConfigs().get(Configs.MYUUID);


                String remotepath = Main.getConfigs().get(Configs.FTPREMOTEPATH);

                String archivepath = remotepath + "/ocfflag/archive";
                String activepath = remotepath + "/ocfflag/active";

                String remoteFile = activepath + "/" + uuid + ".php";

                String localFile = Tools.getWorkingPath() + File.separator + uuid + ".php";

                String archivefile = archivepath + "/" + now.toString("yyyyMMddHHmmss") + "-" + uuid + ".php";

                // Verzeichnisse erstellen, wenn nötig
                resultOk &= ftpClient.makeDirectory(remotepath);
                resultOk &= ftpClient.makeDirectory(activepath);
                resultOk &= ftpClient.makeDirectory(archivepath);

                // Gibts noch eine aktive Statistik Datei ?
                FileOutputStream fos = new FileOutputStream(localFile);
                boolean remoteFileStillExisting = ftpClient.retrieveFile(remoteFile, fos);

                if (remoteFileStillExisting) {
                    fos.close();
                    ftpClient.deleteFile(remoteFile);

                    // ins Archiv verschieben
                    FileInputStream fis = new FileInputStream(localFile);
                    resultOk &= ftpClient.storeFile(archivefile, fis);

                    logger.debug(archivefile + ": " + ftpClient.getReplyString());

                    resultOk &= ftpClient.logout();
                    logger.debug(ftpClient.getReplyString());
                }
            }
        } finally {
            ftpClient.disconnect();
        }

        return resultOk;
    }

    public static boolean testFTP(JTextArea outputArea, JButton buttonToDisable) {

        if (!Main.getConfigs().isFTPComplete()) {
            outputArea.append("FTP Verbindungsdaten unvollständig\n");
            return false;
        }

        buttonToDisable.setEnabled(false);

        // das funktioniert noch nicht
        boolean ftps = false;//(Main.getConfigs().get(Configs.FTPS).equalsIgnoreCase("true"));
        FTPClient ftpClient = new FTPClient(); //ftps ? new FTPSClient() : new FTPClient();

        String host = Main.getConfigs().get(Configs.FTPHOST);
        int port = Integer.parseInt(Main.getConfigs().get(Configs.FTPPORT));
        String usr = Main.getConfigs().get(Configs.FTPUSER);
        String pwd = Main.getConfigs().get(Configs.FTPPWD);
        String uuid = Main.getConfigs().get(Configs.MYUUID);
        Logger logger = Logger.getLogger(FTPWrapper.class);

        boolean success = false;

        SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>() {
            @Override
            protected Boolean doInBackground() throws IOException {
                boolean resultOk = true;
                String line = "";

                try {
                    ftpClient.connect(host, port);

                    line = ftpClient.getReplyString();
                    logger.debug(line);
                    outputArea.append(line);

                    resultOk &= ftpClient.login(usr, pwd);
                    line = ftpClient.getReplyString();
                    logger.debug(line);
                    outputArea.append(line);

                    // creating a testfile for the ftp test
                    File file = File.createTempFile("ocfflag", ".txt");
                    file.createNewFile();
                    FileWriter writer = new FileWriter(file);

                    for (int length = 0; length <= 1e+7 / 10; length += 39) {
                        writer.write("abcdefghijkl");
                        writer.write("\n");
                        writer.write("abcdefghijkl");
                        writer.write("\n");
                        writer.write("abcdefghijkl");
                        writer.write("\n");
                    }
                    writer.flush();
                    writer.close();
                    file.deleteOnExit();

                    // upload
                    FileInputStream fis = new FileInputStream(file);
                    resultOk &= ftpClient.storeFile("ocfflagtest-" + uuid, fis);
                    line = ftpClient.getReplyString();
                    logger.debug(line);
                    outputArea.append(line);

                    ftpClient.deleteFile("ocfflagtest-" + uuid);


                } catch (Exception ftpEx) {
                    logger.error(ftpEx);
                    outputArea.append(ftpEx.toString() + "\n");
                    resultOk = false;
                } finally {
                    ftpClient.disconnect();
                }
                return resultOk;
            }

            @Override
            protected void done() {
                try {
                    buttonToDisable.setEnabled(true);
                    outputArea.append(get() ? "FTP funktioniert\n" : "FEHLER IN FTP\n");
                } catch (InterruptedException e) {
                    logger.debug(e);
                } catch (ExecutionException e) {
                    logger.fatal(logger, e);
                }
            }
        };

        worker.execute();
        return true;
    }

}