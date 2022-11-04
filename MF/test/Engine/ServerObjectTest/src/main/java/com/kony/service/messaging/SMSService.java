package com.kony.service.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service to send message via SMS - WIP
 *
 * @author Aditya Mankal
 */
public class SMSService implements IMessagingService {

	private static final Logger LOG = LogManager.getLogger(SMSService.class);

	@Override
	public boolean sendMessage(String recepientId, String messageSubject, String messageBody, String bodyMimeType, String attachmentContent, String attachmentName,
			String attachmentMimeType) {
		LOG.error("SMS Service is not yet supported");
		throw new UnsupportedOperationException("SMS Service is not yet supported");
	}

	@Override
	public boolean sendMessage(String recepientId, String messageSubject, String messageBody, String bodyMimeType) {
		LOG.error("SMS Service is not yet supported");
		throw new UnsupportedOperationException("SMS Service is not yet supported");
	}

}
