/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.kafka;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
//import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MessagePublisherClient {

	private static final Logger LOG = LoggerFactory.getLogger(MessagePublisherClient.class);

	public static void main(String[] args) throws Exception {

		LOG.info("About to run Kafka-camel integration...");

		String testKafkaMessage = "Test Message from  MessagePublisherClient " + Calendar.getInstance().getTime();

		// TO DO add try with resources
		CamelContext camelContext = new DefaultCamelContext();

		// Register and configure the PropertiesComponent manually
		camelContext.getPropertiesComponent().setLocation("classpath:application.properties");

		// Add route to send messages to Kafka
		camelContext.addRoutes(new RouteBuilder() {
			public void configure() {
				// Topic and offset of the record is returned.
				from("direct:kafkaStart").routeId("DirectToKafka")
						.to("kafka:{{producer.topic}}?brokers={{kafka.bootstrap.servers}}")
						.log("headers: ${headers}");
				// Topic can be set in header as well.
				from("direct:kafkaStartNoTopic").routeId("kafkaStartNoTopic")
						.to("kafka:placeholderTopic?brokers={{kafka.bootstrap.servers}}")
						.log("${headers}"); // Topic and offset of the record is returned.
				from("direct:kafkaStartDynamic")
						.routeId("kafkaDynamicRoute")
						.setHeader("kafkaUri", simple("kafka:${header.kafka.TOPIC}?brokers={{kafka.bootstrap.servers}}"))
						.toD("${header.kafkaUri}")  // toD = dynamic endpoint
						.log("${headers}");
				// Use custom partitioner based on the key.
				from("direct:kafkaStartWithPartitioner").routeId("kafkaStartWithPartitioner")
						.to("kafka:{{producer.topic}}?brokers={{kafka.bootstrap.servers}}&partitioner={{producer.partitioner}}")
						.log("${headers}"); // Use custom partitioner based on the key.
				// Takes input from the command line.
				from("stream:in").setHeader(KafkaConstants.PARTITION_KEY, simple("0"))
						.setHeader(KafkaConstants.KEY, simple("1"))
						.to("direct:kafkaStart");
			}
		});

		ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
		camelContext.start();

		Map<String, Object> headers = new HashMap<>();
		headers.put(KafkaConstants.PARTITION_KEY, 0);
		headers.put(KafkaConstants.KEY, "1");
		producerTemplate.sendBodyAndHeaders("direct:kafkaStart", testKafkaMessage, headers);

		// Send with topicName in header
		testKafkaMessage = "TOPIC " + testKafkaMessage;
		headers.put(KafkaConstants.KEY, "2");
		headers.put(KafkaConstants.TOPIC, "WebLogs");
		producerTemplate.sendBodyAndHeaders("direct:kafkaStartNoTopic", testKafkaMessage, headers);

		// Send with topicName in header
		testKafkaMessage = "TOPIC_2 " + testKafkaMessage;
		headers.put(KafkaConstants.KEY, "3");
		headers.put(KafkaConstants.TOPIC, "WebLogs");
		producerTemplate.sendBodyAndHeaders("direct:kafkaStartDynamic", testKafkaMessage, headers);

		testKafkaMessage = "PART 0 :  " + testKafkaMessage;
		Map<String, Object> newHeader = new HashMap<>();
		newHeader.put(KafkaConstants.KEY, "AB"); // This should go to partition 0
		producerTemplate.sendBodyAndHeaders("direct:kafkaStartWithPartitioner", testKafkaMessage, newHeader);

		testKafkaMessage = "PART 1 :  " + testKafkaMessage;
		newHeader.put(KafkaConstants.KEY, "ABC"); // This should go to partition 1
		producerTemplate.sendBodyAndHeaders("direct:kafkaStartWithPartitioner", testKafkaMessage, newHeader);

		LOG.info("Successfully published event to Kafka.");
		System.out.println("Enter text on the line below : [Press Ctrl-C to exit.] ");

		Thread.sleep(5 * 60 * 1000);

		camelContext.stop();
	}

}
