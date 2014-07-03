package com.zyeeda.coala.bpm.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;

/**
 * 任务信息自定义查询映射
 *
 ****************************
 * @author child          *
 * @date   2014年7月2日        *
 ****************************
 */
public interface HistoricTaskMapper {
    @Select("select t.ID_ as id, t.name_ as name, t.assignee_ as assignee, "
    		+ " t.start_time_ as startTime, t.claim_time_ as claimTime, t.end_time_ as endTime, "
    		+ " a.f_account_name as assigneeName "
    		+ " from act_hi_taskinst t"
    		+ " left outer join zda_account a on t.assignee_ = a.f_id "
    		+ " where t.proc_inst_id_ = #{processInstanceId} ")
    List<Map<String, Object>> getHistoricTasksByProcessInstanceId(String processInstanceId);
}
