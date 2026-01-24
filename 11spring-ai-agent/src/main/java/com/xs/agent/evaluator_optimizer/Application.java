
/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.xs.agent.evaluator_optimizer;

import org.springframework.ai.chat.model.ChatModel;
import com.xs.agent.config.RestClientConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Qualifier;

// ------------------------------------------------------------
// EVALUATOR-OPTIMIZER
// ------------------------------------------------------------

@SpringBootApplication
@Import(RestClientConfig.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Application.class);
		// This is a CLI workflow; disable web server startup.
		app.setWebApplicationType(WebApplicationType.NONE);
		System.exit(SpringApplication.exit(app.run(args)));
	}



	@Bean
	public CommandLineRunner commandLineRunner(@Qualifier("deepSeekChatModel") ChatModel chatModel) {
		return args -> {
			var chatClient = ChatClient.create(chatModel);
			new SimpleEvaluatorOptimizer(chatClient).loop("""
					<user input>
					 面试被问： 怎么高效的将10000行list<User>数据，转化成map<id，user>，不是用stream.
					</user input>
					""");
		};
	}
}
