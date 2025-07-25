/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.toolcalling.tushare;

import com.alibaba.cloud.ai.toolcalling.common.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author HunterPorter
 */
@SpringBootTest(classes = { TushareAutoConfiguration.class, CommonToolCallAutoConfiguration.class })
@DisplayName("Tushare stock quotes Test")
public class TushareStockQuotesServiceTest {

	@Autowired
	private TushareStockQuotesService tushareStockQuotesService;

	private static final Logger log = LoggerFactory.getLogger(TushareStockQuotesServiceTest.class);

	@Test
	@EnabledIfEnvironmentVariable(named = TushareConstants.TOKEN_ENV, matches = CommonToolCallConstants.NOT_BLANK_REGEX)
	@DisplayName("Tushare Tool-Calling Test By Stock Code")
	public void testStockQuotesSearchByStockCode() {
		TushareStockQuotesService.Request request = new TushareStockQuotesService.Request("000001.SZ", null, null);

		TushareStockQuotesService.Response response = tushareStockQuotesService.apply(request);
		Assertions.assertNotNull(response, "Response should not be null");
		log.info("stock quotes Search Response: {}", response.message());
		assertThat(response.message()).doesNotContain("Error");
	}

	@Test
	@EnabledIfEnvironmentVariable(named = TushareConstants.TOKEN_ENV, matches = CommonToolCallConstants.NOT_BLANK_REGEX)
	@DisplayName("Tushare Tool-Calling Test By Date Range")
	public void testStockQuotesSearchByDateRange() {
		TushareStockQuotesService.Request request = new TushareStockQuotesService.Request(null, "20250710", "20250710");

		TushareStockQuotesService.Response response = tushareStockQuotesService.apply(request);
		Assertions.assertNotNull(response, "Response should not be null");
		log.info("stock quotes Search Response: {}", response.message());
		assertThat(response.message()).doesNotContain("Error");
	}

}
