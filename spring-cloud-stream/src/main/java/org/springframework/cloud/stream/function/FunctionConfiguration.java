/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.function;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.catalog.FunctionInspector;
import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

/**
 * @author Oleg Zhurakousky
 * @author David Turanski
 * @author Ilayaperumal Gopinathan
 * @since 2.1
 */
@Configuration
@ConditionalOnProperty("spring.cloud.stream.function.definition")
@EnableConfigurationProperties(StreamFunctionProperties.class)
public class FunctionConfiguration {

	@Autowired(required = false)
	private Source source;

	@Autowired(required = false)
	private Processor processor;

	@Bean
	public IntegrationFlowFunctionSupport functionSupport(FunctionCatalogWrapper functionCatalog,
		FunctionInspector functionInspector, CompositeMessageConverterFactory messageConverterFactory,
		StreamFunctionProperties functionProperties) {
		return new IntegrationFlowFunctionSupport(functionCatalog, functionInspector, messageConverterFactory,
			functionProperties);
	}

	@Bean
	public FunctionCatalogWrapper functionCatalogWrapper(FunctionCatalog catalog) {
		return new FunctionCatalogWrapper(catalog);
	}

	/**
	 * This configuration creates an instance of {@link IntegrationFlow} appropriate for binding declared using EnableBinding.
	 */
	@ConditionalOnMissingBean
	@Bean
	public IntegrationFlow integrationFlowCreator(IntegrationFlowFunctionSupport functionSupport) {
		if (this.processor != null) {
			return functionSupport.containsFunction(Function.class) ?
					functionSupport.integrationFlowForFunction(this.processor.input(), this.processor.output()).get() : null;
		}
		else if (this.source != null && this.processor == null) {
			return functionSupport.containsFunction(Supplier.class) ?
				functionSupport.integrationFlowFromNamedSupplier().channel(this.source.output()).get() : null;
		}
		return null;
	}
}
