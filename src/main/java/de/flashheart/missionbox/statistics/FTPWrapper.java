package de.flashheart.missionbox.statistics;
import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.HasLogger;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.joda.time.DateTime;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * FTP-Utility, basierend auf Apache FTPClient:
 * {@link "http://commons.apache.org/net/apidocs/org/apache/commons/net/ftp/FTPClient.html"}
 */
class FTPWrapper implements HasLogger {
    private final String SUBDIR = "missionbox";
    private final int MAX_ERROR_COUNT;

    private StatusMessageAppender myAppender = null;
    private FTPClient ftp;
    private int errorCount = 0;
    private String archivepath, activepath, remoteFile, uuid;

    public FTPWrapper() {
        MAX_ERROR_COUNT = Main.getConfigs().getInt(Configs.FTPMAXERRORCOUNT);
        tryToInitFTP();
    }

    private void tryToInitFTP() {
        if (!Main.getConfigs().isFTPComplete()) {
            ftp = null;
            return;
        }
        ftp = new FTPClient();
        try {
            ftp.setRemoteHost(Main.getConfigs().get(Configs.FTPHOST));
            ftp.setRemotePort(Integer.parseInt(Main.getConfigs().get(Configs.FTPPORT)));
            ftp.setTimeout(3000);
            ftp.connect();
            ftp.login(Main.getConfigs().get(Configs.FTPUSER), Main.getConfigs().get(Configs.FTPPWD));
            getLogger().debug(ftp.getLastReply().getReplyText());
            ftp.setConnectMode(FTPConnectMode.PASV);
            if (!Main.getConfigs().get(Configs.FTPREMOTEPATH).isEmpty()) {
                ftp.chdir(Configs.FTPREMOTEPATH);
            }

            uuid = Main.getConfigs().get(Configs.MYUUID);


            archivepath = SUBDIR + "/" + "archive";
            activepath = SUBDIR + "/" + "active";
            remoteFile = activepath + "/" + uuid + ".php";

            setupFTPDirStructure();
            ftp.quit();
            errorCount = 0;
        } catch (Exception e) {
            getLogger().error(e);
            e.printStackTrace();
            errorCount++;
            getLogger().warn("Try #" + errorCount + " out of " + MAX_ERROR_COUNT);
        } finally {
            if (errorCount >= MAX_ERROR_COUNT) {
                ftp = null;
                getLogger().warn("Did try to connect for " + errorCount + " times. Giving up.");
            }

        }
    }

    public boolean isFTPWorking() {
        return ftp != null;
    }

    private boolean connect() {
        if (ftp == null) return false;
        try {
            ftp.connect();
            ftp.login(Main.getConfigs().get(Configs.FTPUSER), Main.getConfigs().get(Configs.FTPPWD));
            getLogger().debug(ftp.getLastReply().getReplyText());
            ftp.setConnectMode(FTPConnectMode.PASV);
            if (!Main.getConfigs().get(Configs.FTPREMOTEPATH).isEmpty()) {
                ftp.chdir(Configs.FTPREMOTEPATH);
                getLogger().debug(ftp.getLastReply().getReplyText());
            }
            errorCount = 0;
        } catch (Exception e) {
            getLogger().error(e);
            e.printStackTrace();
            errorCount++;
            getLogger().warn("Try #" + errorCount + " out of " + MAX_ERROR_COUNT);
        } finally {
            if (errorCount >= MAX_ERROR_COUNT) {
                ftp = null;
                getLogger().warn("Did try to connect for " + errorCount + " times. Giving up.");
            }
        }
        return errorCount == 0;
    }

    /**
     * FTP-Client-Upload.
     *
     * @return true falls ok
     */
    public boolean upload(String content, boolean move2archive) {
        if (!connect()) return false;

        try {
            File tempPHPFile = File.createTempFile("ocfflag", ".php");
            tempPHPFile.deleteOnExit();
            FileUtils.writeStringToFile(tempPHPFile, content, "UTF-8");
            upload(tempPHPFile.getPath(), remoteFile);
            if (move2archive) move2archive();
            ftp.quit();
            errorCount = 0;
            getLogger().debug(ftp.getLastReply().getReplyText());
        } catch (Exception e) {
            getLogger().error(e);
            e.printStackTrace();
            errorCount++;
            getLogger().warn("Try #" + errorCount + " out of " + MAX_ERROR_COUNT);
        } finally {
            if (errorCount >= MAX_ERROR_COUNT) {
                ftp = null;
                getLogger().warn("Did try to connect for " + errorCount + " times. Giving up.");
            }
        }
        return ftp != null;
    }

    private void upload(String localFilename, String remoteFilename) throws IOException, FTPException {
        ftp.put(localFilename, remoteFilename, false);
        getLogger().debug("uploading " + remoteFilename + ": " + ftp.getLastReply().getReplyText());
    }

    /**
     * Richtet das FTP Verzeichnis ein.
     * Verschiebt evtl. kaputte oder abgeschlossene PHP Statistiken in das Archivverzeichnis.
     * Dabei benennt es die Datei um.
     */
    private void setupFTPDirStructure() throws Exception {
        mkdir(SUBDIR);
        mkdir(activepath);
        mkdir(archivepath);

    }

    public void cleanupStatsFile() {
        if (!connect()) return;
        try {
            move2archive();
            ftp.quit();
            errorCount = 0;
            getLogger().debug(ftp.getLastReply().getReplyText());
        } catch (Exception e) {
            getLogger().error(e);
            e.printStackTrace();
            errorCount++;
            getLogger().warn("Try #" + errorCount + " out of " + MAX_ERROR_COUNT);
        } finally {
            if (errorCount >= MAX_ERROR_COUNT) {
                ftp = null;
                getLogger().warn("Did try to connect for " + errorCount + " times. Giving up.");
            }
        }
    }

    /**
     * Gibts noch eine aktive Statistik Datei ? Dann ins Archiv damit.
     */
    private void move2archive() throws IOException, FTPException {
        String archivefile = archivepath + "/" + new DateTime().toString("yyyyMMddHHmmss") + "-" + uuid + ".php";
        move(remoteFile, archivefile);
    }

    /**
     * Das FTP Protokoll kennt kein Move. Daher habe ich hier eins nachgebaut.
     *
     * @param source
     * @param target
     * @return
     */
    private void move(String source, String target) throws IOException, FTPException {
        if (ftp.existsFile(source)) {
            File tmpFile = File.createTempFile("ocfflag", ".php");
            tmpFile.deleteOnExit();
            ftp.get(tmpFile.getPath(), source);
            ftp.put(tmpFile.getPath(), target);
            ftp.delete(source);
            getLogger().debug("moving " + source + " to " + target);
        }
    }

    /**
     * erstellt ein Verzeichnis, sofern es das noch nicht gibt.
     *
     * @param dirname
     * @return true bei Erfolg
     */
    public void mkdir(String dirname) throws IOException, FTPException {
        if (!ftp.existsDirectory(dirname)) {
            ftp.mkdir(dirname);
            getLogger().debug("mkdir " + dirname + ": " + ftp.getLastReply().getReplyText());
        }
    }

    public void testFTP(JTextArea outputArea, JButton buttonToDisable) {
        if (!Main.getConfigs().isFTPComplete()) {
            outputArea.append("FTP Verbindungsdaten unvollständig\n");
            return;
        }
        buttonToDisable.setEnabled(false);
        // TODO: das hier wird nicht mehr korrekt entfernt.
        if (myAppender == null) {
            myAppender = new StatusMessageAppender(outputArea);
            getLogger().addAppender(myAppender);
        }
        myAppender.setActive(true);
        tryToInitFTP();
        if (!connect()) {
            outputArea.append("Server reagiert nicht\n");
            buttonToDisable.setEnabled(true);
            return;
        }

        SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>() {
            @Override
            protected Boolean doInBackground() throws IOException {
                boolean resultOk = true;

                try {

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
                    String rfile = "ocfflagtest-" + uuid;
                    upload(file.getPath(), rfile);
                    ftp.delete(rfile);
                    getLogger().debug(ftp.getLastReply().getReplyText());
                    ftp.quit();
                    getLogger().debug(rfile + ": " + ftp.getLastReply().getReplyText());
                } catch (Exception ftpEx) {
                    getLogger().error(ftpEx);
                    outputArea.append(ftpEx.toString() + "\n");
                    resultOk = false;
                } finally {
                    myAppender.setActive(false);
                }

                return resultOk;
            }

            @Override
            protected void done() {
                try {
                    buttonToDisable.setEnabled(true);
                    outputArea.append(get() ? "FTP funktioniert\n" : "FEHLER IN FTP\n");
                } catch (InterruptedException e) {
                    getLogger().debug(e);
                } catch (ExecutionException e) {
                    getLogger().fatal(e);
                }
            }
        };

        worker.execute();

    }


    private class StatusMessageAppender extends AppenderSkeleton {
        private final JTextArea jTextA;
        private PatternLayout defaultPatternLayout = new PatternLayout("%d{ISO8601} %-5p: %m%n");
        private boolean active;

        public StatusMessageAppender(JTextArea jTextA) {
            this.jTextA = jTextA;
        }

        @Override
        protected void append(LoggingEvent event) {
            if (active) jTextA.append(defaultPatternLayout.format(event));
        }


        @Override
        public void close() {
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public boolean requiresLayout() {
            return true;
        }
    }

}