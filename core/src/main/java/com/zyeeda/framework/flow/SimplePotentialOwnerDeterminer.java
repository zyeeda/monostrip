package com.zyeeda.framework.flow;

import java.util.List;

import org.drools.runtime.process.WorkItem;
import org.jbpm.task.OrganizationalEntity;

/**
 * @author guyong
 *
 */
public class SimplePotentialOwnerDeterminer extends AbstractPotentialOwnerDeterminer {

    @Override
    protected List<OrganizationalEntity> determinePotentials(String line, String role, String point, WorkItem workItem) {
        return null;
    }

}
