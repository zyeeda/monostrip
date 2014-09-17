package com.zyeeda.cdeio.bpm.mapper;

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
	// 此处与 comment 表进行关联，只支持一个任务只有一条 comment 的情况，如果多于一个 comment 则会出现任务历史冗余	
    @Select("select t.ID_ as id, t.name_ as name, t.assignee_ as assignee, "
    		+ " t.start_time_ as startTime, t.claim_time_ as claimTime, t.end_time_ as endTime, "
    		+ " a.f_account_name as assigneeName, c.message_ comment "
    		+ " from act_hi_taskinst t"
    		+ " left outer join zda_account a on t.assignee_ = a.f_id  "
    		+ " left outer join act_hi_comment c on t.id_ = c.task_id_ "
    		+ " where t.proc_inst_id_ = #{processInstanceId} "
    		+ " order by t.start_time_ ")
    List<Map<String, Object>> getHistoricTasksByProcessInstanceId(String processInstanceId);
}
