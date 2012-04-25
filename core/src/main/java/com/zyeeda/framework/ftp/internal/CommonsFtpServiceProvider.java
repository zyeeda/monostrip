/**
 * 
 */
package com.zyeeda.framework.ftp.internal;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.framework.ftp.FtpService;
import com.zyeeda.framework.service.AbstractService;

/**
 * 
 *
 * @creator Qi Zhao
 * @date 2011-6-24
 *
 * @LastChanged
 * @LastChangedBy $LastChangedBy: $
 * @LastChangedDate $LastChangedDate: $
 * @LastChangedRevision $LastChangedRevision:  $
 */
public class CommonsFtpServiceProvider extends AbstractService implements FtpService {

    private static final Logger logger = LoggerFactory.getLogger(CommonsFtpServiceProvider.class);

    private static final String DEFAULT_FTP_HOST = "10.118.250.131";
    private static final String DEFAULT_FTP_USER_NAME = "gzsc";
    private static final String DEFAILT_FTP_PASSWORD = "gzsc";
    private static final int DEFAULT_FTP_PORT = 21;

    private String ftpHost;
    private String ftpUserName;
    private String ftpPassword;
    private String ftpPortProperty = null;
    
    
    public void setFtpHost(String ftpHost) {
        this.ftpHost = ftpHost;
    }

    public void setFtpUserName(String ftpUserName) {
        this.ftpUserName = ftpUserName;
    }

    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }

    public void setFtpPortProperty(String ftpPortProperty) {
        this.ftpPortProperty = ftpPortProperty;
    }

    private int ftpPort;

    @Override
    public void start() throws Exception {
        if( this.ftpHost == null ) {
            this.ftpHost = DEFAULT_FTP_HOST;
        }
        if( this.ftpUserName == null ) {
            this.ftpUserName = DEFAULT_FTP_USER_NAME;
        }
        if( this.ftpPassword == null ) {
            this.ftpPassword = DEFAILT_FTP_PASSWORD;
        }
        this.ftpPort = this.ftpPortProperty == null ? DEFAULT_FTP_PORT : Integer.parseInt(this.ftpPortProperty);
    }

    public FTPClient connectThenLogin() throws IOException,
           FtpConnectionRefusedException, FtpServerLoginFailedException {

               FTPClient ftp = new FTPClient();
               ftp.connect(this.ftpHost, this.ftpPort);
               logger.debug("connected to server = {} ", this.ftpHost);

               String[] messages = ftp.getReplyStrings();
               for (String msg : messages) {
                   logger.debug(msg);
               }

               int replyCode = ftp.getReplyCode();
               if (!FTPReply.isPositiveCompletion(replyCode)) {
                   ftp.disconnect();
                   throw new FtpConnectionRefusedException(
                           "Refused to connect to server " + this.ftpHost + ".");
               }

               boolean sucessful = ftp.login(this.ftpUserName, this.ftpPassword);
               if (!sucessful) {
                   ftp.disconnect();
                   throw new FtpServerLoginFailedException(
                           "Failed to login to server " + this.ftpHost + ".");
               }

               return ftp;
    }

    public void deleteFiles(FTPClient client, FTPFile[] ftpFiles) throws IOException {
        logger.debug("delete file size = {}", ftpFiles.length);

        for (FTPFile ftpFile : ftpFiles) {
            client.deleteFile(ftpFile.getName()); 
        }
    }
}
