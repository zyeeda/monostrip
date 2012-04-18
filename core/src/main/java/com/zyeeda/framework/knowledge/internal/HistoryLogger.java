package com.zyeeda.framework.knowledge.internal;

import org.drools.KnowledgeBase;
import org.drools.audit.WorkingMemoryLogger;
import org.drools.audit.event.LogEvent;
import org.drools.audit.event.RuleFlowLogEvent;
import org.drools.audit.event.RuleFlowNodeLogEvent;
import org.drools.definition.process.Node;
import org.drools.definition.process.Process;
import org.drools.definition.process.WorkflowProcess;
import org.drools.event.KnowledgeRuntimeEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyeeda.framework.entities.ActionHistory;
import com.zyeeda.framework.entities.ProcessHistory;
import com.zyeeda.framework.managers.ActionHistoryManager;
import com.zyeeda.framework.managers.ProcessHistoryManager;
import com.zyeeda.framework.managers.internal.DefaultActionHistoryManager;
import com.zyeeda.framework.managers.internal.DefaultProcessHistoryManager;
import com.zyeeda.framework.persistence.PersistenceService;

public class HistoryLogger extends WorkingMemoryLogger {
	
	private final static Logger logger = LoggerFactory.getLogger(HistoryLogger.class);
	
	private PersistenceService persistenceSvc;
	private KnowledgeBase kbase;
	
	private ProcessHistoryManager pHisMgr;
	private ActionHistoryManager aHisMgr;
	
	/*
	public HistoryLogger(WorkingMemory workingMemory, PersistenceService persistenceSvc, KnowledgeService knowledgeSvc) {
		super(workingMemory);
		
		this.persistenceSvc = persistenceSvc;
		this.knowledgeSvc = knowledgeSvc;
		this.init();
	}*/
	
	public HistoryLogger(KnowledgeRuntimeEventManager session, PersistenceService persistenceSvc, KnowledgeBase kbase) {
		super(session);
		
		this.persistenceSvc = persistenceSvc;
		this.kbase = kbase;
		this.init();
	}
	
	private void init() {
		this.pHisMgr = new DefaultProcessHistoryManager(this.persistenceSvc);
		this.aHisMgr = new DefaultActionHistoryManager(this.persistenceSvc);
	}
	
	@Override
	public void logEventCreated(LogEvent logEvent) {
		switch (logEvent.getType()) {
			case LogEvent.BEFORE_RULEFLOW_CREATED: {
				RuleFlowLogEvent event = (RuleFlowLogEvent) logEvent;
				
				if (logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder();
					sb.append("BEFORE RULEFLOW CREATED\n");
					sb.append(String.format("\tprocess id = %s\n", event.getProcessId()));
					sb.append(String.format("\tprocess name = %s\n", event.getProcessName()));
					sb.append(String.format("\tprocess instance id = %s", event.getProcessInstanceId()));
					logger.debug(sb.toString());
				}
				
				ProcessHistory history = new ProcessHistory();
				history.setProcessId(event.getProcessId());
				history.setName(event.getProcessName());
				history.setProcessInstanceId(event.getProcessInstanceId());
				this.pHisMgr.persist(history);
				break;
			}
			case LogEvent.AFTER_RULEFLOW_COMPLETED: {
				RuleFlowLogEvent event = (RuleFlowLogEvent) logEvent;
				
				if (logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder();
					sb.append("AFTER RULEFLOW COMPLETED\n");
					sb.append(String.format("\tprocess id = %s\n", event.getProcessId()));
					sb.append(String.format("\tprocess name = %s\n", event.getProcessName()));
					sb.append(String.format("\tprocess instance id = %s", event.getProcessInstanceId()));
					logger.debug(sb.toString());
				}
				
				ProcessHistory history = this.pHisMgr.findByProcessInstanceId(event.getProcessInstanceId());
				if (history == null) {
					logger.warn("Cannot find process history by process instance id {}.", event.getProcessInstanceId());
				} else {
					history.setEnded(true);
					this.pHisMgr.save(history);
				}
				
				break;
			}
			case LogEvent.BEFORE_RULEFLOW_NODE_TRIGGERED: {
				RuleFlowNodeLogEvent event = (RuleFlowNodeLogEvent) logEvent;
				
				String nodeType = null;
				Process process = this.kbase.getProcess(event.getProcessId());
				if (process instanceof WorkflowProcess) {
					WorkflowProcess workflow = (WorkflowProcess) process;
					try {
						Node node = workflow.getNode(Long.parseLong(event.getNodeId()));
						nodeType = node.getClass().getSimpleName();
					} catch (NumberFormatException e) {
						logger.warn("node is is not a long value", e);
					}
				} else {
					logger.warn("process {} is not of WorkflowProcess type.", event.getProcessId());
				}
				
				if (logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder();
					sb.append("BEFORE RULEFLOW NODE TRIGGERED\n");
					sb.append(String.format("\tprocess id = %s\n", event.getProcessId()));
					sb.append(String.format("\tprocess name = %s\n", event.getProcessName()));
					sb.append(String.format("\tprocess instance id = %s\n", event.getProcessInstanceId()));
					sb.append(String.format("\tnode id = %s\n", event.getNodeId()));
					sb.append(String.format("\tnode name = %s\n", event.getNodeName()));
					sb.append(String.format("\tnode istance id = %s\n", event.getNodeInstanceId()));
					sb.append(String.format("\tnode type = %s", nodeType));
					logger.debug(sb.toString());
				}
				
				ProcessHistory proHist = this.pHisMgr.findByProcessInstanceId(event.getProcessInstanceId());
				if (proHist == null) {
					logger.warn("Cannot find process history by process instance id {}.", event.getProcessInstanceId());
				} else {
					proHist.setCurrentState(event.getNodeName());
					this.pHisMgr.save(proHist);
				}
				
				ActionHistory actHist = new ActionHistory();
				actHist.setProcessId(event.getProcessId());
				actHist.setProcessName(event.getProcessName());
				actHist.setProcessInstanceId(event.getProcessInstanceId());
				actHist.setNodeId(event.getNodeId());
				actHist.setNodeInstanceId(event.getNodeInstanceId());
				actHist.setName(event.getNodeName());
				actHist.setNodeType(nodeType);
				actHist.setAlive(true);
				this.aHisMgr.persist(actHist);
				break;
			}
			case LogEvent.BEFORE_RULEFLOW_NODE_EXITED: {
				RuleFlowNodeLogEvent event = (RuleFlowNodeLogEvent) logEvent;
				
				if (logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder();
					sb.append("BEFORE RULEFLOW NODE EXITED\n");
					sb.append(String.format("\tprocess id = %s\n", event.getProcessId()));
					sb.append(String.format("\tprocess name = %s\n", event.getProcessName()));
					sb.append(String.format("\tprocess instance id = %s\n", event.getProcessInstanceId()));
					sb.append(String.format("\tnode id = %s\n", event.getNodeId()));
					sb.append(String.format("\tnode name = %s\n", event.getNodeName()));
					sb.append(String.format("\tnode istance id = %s", event.getNodeInstanceId()));
					logger.debug(sb.toString());
				}
				
				ProcessHistory proHist = this.pHisMgr.findByProcessInstanceId(event.getProcessInstanceId());
				if (proHist == null) {
					logger.warn("Cannot find process history by process instance id {}.", event.getProcessInstanceId());
				} else {
					this.pHisMgr.save(proHist);	
				}
				
				ActionHistory actHist = aHisMgr.findAlive(event.getProcessInstanceId(), event.getNodeInstanceId());
				if (actHist == null) {
					logger.warn("Cannot find action history by process instance id {} and node instance id {}.", 
							event.getProcessInstanceId(), event.getNodeInstanceId());
				} else {
					actHist.setAlive(false);
					this.aHisMgr.save(actHist);
				}
				
				break;
			}
		default:
			// ignore
		}
		
		this.persistenceSvc.getCurrentSession().flush();
	}
	
}
