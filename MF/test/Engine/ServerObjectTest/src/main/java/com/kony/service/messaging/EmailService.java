package com.kony.service.messaging;

import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service to send email message
 *
 * @author Aditya Mankal
 */
public class EmailService implements IMessagingService {

	private static final Logger LOG = LogManager.getLogger(EmailService.class);

	static final String SMTP_USERNAME = EmailServiceProperties.SMTP_USERNAME;
	static final String SMTP_PASSWORD = EmailServiceProperties.SMTP_PASSWORD;
	Properties props = EmailServiceProperties.CONFIG_PROPS;
	Session session = null;

	public EmailService() {
		initializeMailClient();
	}

	private void initializeMailClient() {
		LOG.debug("Preparing mail client...");
		session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
			}
		});
		LOG.debug("Mail client initialized.");
	}

	@Override
	public boolean sendMessage(String recepientId, String messageSubject, String messageBody, String bodyMimeType, String attachmentContent, String attachmentName,
			String attachmentMimeType) {

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(EmailServiceProperties.SMTP_SENDERID));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recepientId));
			message.setSubject(messageSubject);

			Multipart multipart = new MimeMultipart();

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(messageBody);
			multipart.addBodyPart(messageBodyPart);

			if (attachmentContent != null && attachmentName != null && attachmentMimeType != null) {
				LOG.debug("Adding attachment to Mail Content");
				messageBodyPart = new MimeBodyPart();
				DataSource source = new ByteArrayDataSource(attachmentContent, attachmentMimeType);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(attachmentName);
				multipart.addBodyPart(messageBodyPart);
				LOG.debug("Attachment added to Mail Content");
			}

			message.setContent(multipart, attachmentMimeType);

			LOG.debug("Sending message...");
			Transport.send(message);
			LOG.debug("Message sent.");
			return true;
		} catch (MessagingException | IOException e) {
			LOG.error("Exception while sending sending email", e);
			return false;
		}
	}

	@Override
	public boolean sendMessage(String recepientId, String messageSubject, String messageBody, String bodyMimeType) {
		return sendMessage(recepientId, messageSubject, messageBody, bodyMimeType, null, null, null);
	}
}
