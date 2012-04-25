package com.zyeeda.framework.managers.internal;

import com.zyeeda.framework.managers.TimeManager;
import com.zyeeda.framework.service.AbstractService;

/**
 * FIXME 这类没用?
 * changed by guyong
 *
 */
public class DefaultTimeManager extends AbstractService implements TimeManager {

	//private PersistenceService pSvc;

	@Override
	public void changeTheThirdTableDate() {
		/*MyTimerTask myTimerTask =new MyTimerTask(this.pSvc, this.txSvc);
		Timer timer = new Timer();
		timer.schedule(myTimerTask, new Date(), 24*60*60*1000);*/
	}

}
