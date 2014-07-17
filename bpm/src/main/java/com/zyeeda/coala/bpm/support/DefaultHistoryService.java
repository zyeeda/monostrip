package com.zyeeda.coala.bpm.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.history.NativeHistoricActivityInstanceQuery;
import org.activiti.engine.history.NativeHistoricDetailQuery;
import org.activiti.engine.history.NativeHistoricProcessInstanceQuery;
import org.activiti.engine.history.NativeHistoricTaskInstanceQuery;
import org.activiti.engine.history.NativeHistoricVariableInstanceQuery;
import org.activiti.engine.history.ProcessInstanceHistoryLogQuery;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;

import com.zyeeda.coala.bpm.HistoryService;
import com.zyeeda.coala.bpm.mapper.HistoricTaskMapper;
import com.zyeeda.coala.bpm.mapping.HistoricTask;

public class DefaultHistoryService implements HistoryService{

    private ProcessEngine processEngine;
    private ManagementService managementService;
    private org.activiti.engine.HistoryService historyService;

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
        this.historyService = this.processEngine.getHistoryService();
        this.managementService = this.processEngine.getManagementService();
    }

	@Override
	public HistoricProcessInstanceQuery createHistoricProcessInstanceQuery() {
		return historyService.createHistoricProcessInstanceQuery();
	}

	@Override
	public HistoricActivityInstanceQuery createHistoricActivityInstanceQuery() {
		return historyService.createHistoricActivityInstanceQuery();
	}

	@Override
	public HistoricTaskInstanceQuery createHistoricTaskInstanceQuery() {
		return historyService.createHistoricTaskInstanceQuery();
	}

	@Override
	public HistoricDetailQuery createHistoricDetailQuery() {
		return historyService.createHistoricDetailQuery();
	}

	@Override
	public NativeHistoricDetailQuery createNativeHistoricDetailQuery() {
		return historyService.createNativeHistoricDetailQuery();
	}

	@Override
	public HistoricVariableInstanceQuery createHistoricVariableInstanceQuery() {
		return historyService.createHistoricVariableInstanceQuery();
	}

	@Override
	public NativeHistoricVariableInstanceQuery createNativeHistoricVariableInstanceQuery() {
		return historyService.createNativeHistoricVariableInstanceQuery();
	}

	@Override
	public void deleteHistoricTaskInstance(String taskId) {
		historyService.deleteHistoricTaskInstance(taskId);
	}

	@Override
	public void deleteHistoricProcessInstance(String processInstanceId) {
		historyService.deleteHistoricProcessInstance(processInstanceId);
	}

	@Override
	public NativeHistoricProcessInstanceQuery createNativeHistoricProcessInstanceQuery() {
		return historyService.createNativeHistoricProcessInstanceQuery();
	}

	@Override
	public NativeHistoricTaskInstanceQuery createNativeHistoricTaskInstanceQuery() {
		return historyService.createNativeHistoricTaskInstanceQuery();
	}

	@Override
	public NativeHistoricActivityInstanceQuery createNativeHistoricActivityInstanceQuery() {
		return historyService.createNativeHistoricActivityInstanceQuery();
	}

	@Override
	public List<HistoricIdentityLink> getHistoricIdentityLinksForTask(
			String taskId) {
		return historyService.getHistoricIdentityLinksForTask(taskId);
	}

	@Override
	public List<HistoricIdentityLink> getHistoricIdentityLinksForProcessInstance(
			String processInstanceId) {
		return historyService.getHistoricIdentityLinksForProcessInstance(processInstanceId);
	}

	@Override
	public ProcessInstanceHistoryLogQuery createProcessInstanceHistoryLogQuery(
			String processInstanceId) {
		return historyService.createProcessInstanceHistoryLogQuery(processInstanceId);
	}

	@Override
	public List<HistoricTask> getHistoricTasksByProcessInstanceId(final String processInstanceId) {
		CustomSqlExecution<HistoricTaskMapper, List<Map<String, Object>>> customSqlExecution =
		          new AbstractCustomSqlExecution<HistoricTaskMapper, List<Map<String, Object>>>(HistoricTaskMapper.class) {
					  public List<Map<String, Object>> execute(HistoricTaskMapper customMapper) {
					      return customMapper.getHistoricTasksByProcessInstanceId(processInstanceId);
					  }

		};
		List<Map<String, Object>> results = managementService.executeCustomSql(customSqlExecution);

		List<HistoricTask> historicTasks = new ArrayList<HistoricTask>(results.size());
		HistoricTask historicTask = null;

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for(Map<String, Object> entry: results){
//			System.out.println("---map :" + entry);
			historicTask = new HistoricTask();
			historicTask.setId(entry.get("id").toString());
			historicTask.setName(entry.get("name").toString());

			if(entry.get("assigneeName")!=null){
				historicTask.setAssigneeName(entry.get("assigneeName").toString());
			}

			if(entry.get("startTime")!=null){
				try {
					Date date = df.parse(entry.get("startTime").toString());
					historicTask.setStartTime(date);
				} catch (ParseException e) {
				}
			}
			if(entry.get("claimTime")!=null){
				try {
					Date date = df.parse(entry.get("claimTime").toString());
					historicTask.setClaimTime(date);
				} catch (ParseException e) {
				}
			}
			if(entry.get("endTime")!=null){
				try {
					Date date = df.parse(entry.get("endTime").toString());
					historicTask.setEndTime(date);
				} catch (ParseException e) {
				}
			}
			if(entry.get("comment")!=null){
				historicTask.setComment(entry.get("comment").toString());
			}

			historicTasks.add(historicTask);
		}
		return historicTasks;
	}
}
