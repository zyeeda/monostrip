package com.zyeeda.cdeio.bpm;

import java.util.List;

import com.zyeeda.cdeio.bpm.mapping.HistoricTask;

public interface HistoryService extends org.activiti.engine.HistoryService{
	List<HistoricTask> getHistoricTasksByProcessInstanceId(String processInstanceId);
}
