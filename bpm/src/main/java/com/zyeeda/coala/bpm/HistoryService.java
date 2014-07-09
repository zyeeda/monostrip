package com.zyeeda.coala.bpm;

import java.util.List;

import com.zyeeda.coala.bpm.mapping.HistoricTask;

public interface HistoryService extends org.activiti.engine.HistoryService{
	List<HistoricTask> getHistoricTasksByProcessInstanceId(String processInstanceId);
}
