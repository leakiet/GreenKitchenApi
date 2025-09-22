package com.greenkitchen.portal.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;


@Configuration
public class AIConfig {

	@Bean
	public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
		return ChatClient.builder(openAiChatModel)
			.defaultOptions(OpenAiChatOptions.builder()
				.build())
			.build();
	}
}
