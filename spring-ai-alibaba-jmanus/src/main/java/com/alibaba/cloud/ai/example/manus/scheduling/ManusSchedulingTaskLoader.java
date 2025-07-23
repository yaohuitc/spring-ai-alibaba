/*
 * Copyright 2025 the original author or authors.
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
package com.alibaba.cloud.ai.example.manus.scheduling;

import java.util.List;
import java.util.Map;

import com.alibaba.cloud.ai.example.manus.planning.PlanningFactory;
import com.alibaba.cloud.ai.example.manus.planning.coordinator.PlanIdDispatcher;
import com.alibaba.cloud.ai.example.manus.planning.model.po.PlanTemplate;
import com.alibaba.cloud.ai.example.manus.planning.service.PlanTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

/**
 * ManusSchedulingTaskLoader
 * @author yaohui
 * @create 2025/7/21 20:57
 **/
@Component
public class ManusSchedulingTaskLoader implements SchedulingConfigurer {

	private static final Logger logger = LoggerFactory.getLogger(PlanTemplateService.class);

	@Autowired
	private PlanTemplateService planTemplateService;

	@Autowired
	private PlanIdDispatcher planIdDispatcher;

	@Autowired
	@Lazy
	private PlanningFactory planningFactory;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private ScheduledTaskRegistrar taskRegistrar;

	private final Map<String, ScheduledTask> scheduledTasks = Maps.newConcurrentMap();

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		List<PlanTemplate> planTemplateList = planTemplateService.getAllPlanTemplates();
		for (PlanTemplate planTemplate:planTemplateList) {
			try {
				if (StringUtils.isNotBlank(planTemplate.getCron())) {
					CronTask cronTask = new PlanCronTask(planTemplate.getPlanTemplateId(), () -> {
						logger.info(">> jmanus scheduling task execute! template:{} cron: {}", planTemplate.getPlanTemplateId(), planTemplate.getCron());
						planTemplateService.executePlanByTemplateIdInternal(planTemplate.getPlanTemplateId(), planTemplate.getTitle());
					}, planTemplate.getCron());
					scheduledTasks.put(planTemplate.getPlanTemplateId(), taskRegistrar.scheduleCronTask(cronTask));
				}
			} catch (Exception e) {
				logger.warn(">> jmanus scheduling task load failed!", e);
			}
		}
		this.taskRegistrar = taskRegistrar;
	}

	public void schedulePlan(PlanTemplate planTemplate) {
		ScheduledTask scheduledTask = scheduledTasks.get(planTemplate.getPlanTemplateId());
		if (scheduledTask != null) {
			scheduledTask.cancel(false);
			scheduledTasks.remove(planTemplate.getPlanTemplateId());
			logger.info(">> jmanus scheduling task remove success! template:{} cron: {}", planTemplate.getPlanTemplateId(), planTemplate.getCron());
		}
		if (StringUtils.isNotBlank(planTemplate.getCron())) {
			CronTask cronTask = new PlanCronTask(planTemplate.getPlanTemplateId(), () -> {
				planTemplateService.executePlanByTemplateIdInternal(planTemplate.getPlanTemplateId(), planTemplate.getTitle());
			}, planTemplate.getCron());
			scheduledTasks.put(planTemplate.getPlanTemplateId(), taskRegistrar.scheduleCronTask(cronTask));
			logger.info(">> jmanus scheduling task load success! template:{}, cron: {}", planTemplate.getPlanTemplateId(), planTemplate.getCron());
		}
	}

}
