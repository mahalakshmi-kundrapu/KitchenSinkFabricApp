package com.kony.service.messaging;

/**
 * Interface for Messaging channels
 *
 * @author Aditya Mankal
 */
public interface IMessagingService {

	boolean sendMessage(String recepientId, String messageSubject, String messageBody, String bodyMimeType, String attachmentContent, String attachmentName,
			String attachmentMimeType);

	boolean sendMessage(String recepientId, String messageSubject, String messageBody, String bodyMimeType);
}
