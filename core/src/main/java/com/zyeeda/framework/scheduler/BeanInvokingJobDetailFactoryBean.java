package com.zyeeda.framework.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.StatefulJob;
import org.quartz.impl.JobDetailImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.MethodInvoker;

/**
 * This is a cluster safe Quartz/Spring FactoryBean implementation, which
 * produces a JobDetail implementation that can invoke any no-arg method on any
 * bean deployed within a Spring container.
 * <p>
 * Use this Class instead of the MethodInvokingJobDetailBeanFactory Class
 * provided by Spring when deploying to a web environment like Tomcat.
 * <p>
 * <b>Implementation</b><br>
 * The Spring ApplicationContext cannot be passed to a Job via the JobDataMap,
 * because it is not Serializable (and for very good reason!) So, instead of
 * associating an ApplicationContext with a JobDetail or a Trigger object, I
 * made the [Stateful]BeanInvokingJob, which is not persisted in the database,
 * get the applicationContext from the BeanInvokingJobDetailFactoryBean, which
 * is ApplicationContextAware, when the [Stateful]BeanInvokingJob is created and
 * executed.
 * <p>
 * The name or id of the of the bean to invoke (targetBean) and the method to
 * invoke (targetMethod) must be provided in the bean declaration or a
 * JobExecutionException will be thrown.
 * <p>
 * I wrote BeanInvokingJobDetailFactoryBean, because the
 * MethodInvokingJobDetailFactoryBean does not produce Serializable JobDetail
 * objects, and as a result cannot be deployed into a clustered environment (as
 * is documented within the Class).
 * <p>
 * <b>Example</b> <code>
 * <ul>
 * 	&lt;bean id="<i>exampleBean</i>" class="example.ExampleImpl"&gt;
	&lt;/bean&gt;
	<p>
 * 	&lt;bean id="<i>exampleTrigger</i>" class="org.springframework.scheduling.quartz.CronTriggerBean"&gt;
 * <ul>
	<i>&lt;!-- Execute exampleBean.fooBar() at 2am every day --&gt;</i><br>
	&lt;property name="cronExpression" value="0 0 2 * * ?" /&gt;<br>
	&lt;property name="jobDetail"&gt;
	<ul>
	&lt;bean class="frameworkx.springframework.scheduling.quartz.<b>BeanInvokingJobDetailFactoryBean</b>"&gt;
	<ul>
	&lt;property name="concurrent" value="<i>false</i>"/&gt;<br>
	&lt;property name="targetBean" value="<i>exampleBean</i>" /&gt;<br>
	&lt;property name="targetMethod" value="<i>fooBar</i>" /&gt;<br>
	&lt;property name="arguments"&gt;
	<ul>
		&lt;list&gt;
		<ul>
			&lt;value&gt;arg1Value&lt;/value&gt;<br>
			&lt;value&gt;arg2Value&lt;/value&gt;
		</ul>
		&lt;list&gt;
	</ul>
	&lt;/property&gt;
	</ul>
	&lt;/bean&gt;
	</ul>
	&lt;/property&gt;
	</ul>
	&lt;/bean&gt;
	<p>
	&lt;bean class="org.springframework.scheduling.quartz.SchedulerFactoryBean"&gt;
	<ul>
	&lt;property name="triggers"&gt;
	<ul>
	&lt;list&gt;
	<ul>
	&lt;ref bean="<i>exampleTrigger</i>" /&gt;
	</ul>
	&lt;/list&gt;
	</ul>
	&lt;/property&gt;
	</ul>
	&lt;/bean&gt;
	</ul>
 * </code> In this example we created a BeanInvokingJobDetailFactoryBean, which
 * will produce a JobDetail Object with the jobClass property set to
 * StatefulBeanInvokingJob.class (concurrent=="false"; Set to
 * BeanInvokingJob.class when concurrent=="true"), which will in turn invoke the
 * <code>fooBar</code>(String, String) method of the bean with id "
 * <code>exampleBean</code>". Method <code>arguments</code> are optional. In
 * this case there are two String arguments being provided to the
 * <code>fooBar</code> method. The Scheduler is the heart of the whole
 * operation; without it, nothing will happen.
 * <p>
 * For more information on cronExpression visit <a
 * href="http://www.opensymphony.com/quartz/api/org/quartz/CronTrigger.html"
 * >http://www.opensymphony.com/quartz/api/org/quartz/CronTrigger.html</a>
 * 
 * <p>
 * <b>Troubleshooting</b>
 * <p>
 * <b>Error:</b> java.io.IOException: JobDataMap values must be Strings when the
 * 'useProperties' property is set. Key of offending value: arguments<br>
 * <b>Solution:</b> do not set the <code>arguments</code> property when
 * <code>org.quartz.jobstore.useProperty</code> is set to "true" in
 * <code>quartz.properties</code>.
 * <p>
 * 
 * @author Stephen M. Wick
 * 
 * @see #afterPropertiesSet()
 */
@SuppressWarnings("deprecation")
public class BeanInvokingJobDetailFactoryBean implements FactoryBean<JobDetail>,
        BeanNameAware, InitializingBean, ApplicationContextAware {
    /**
     * Set by <code>setApplicationContext</code> when a
     * BeanInvokingJobDetailFactoryBean is defined within a Spring
     * ApplicationContext as a bean.
     * <p>
     * Used by the <code>execute</code> method of the BeanInvokingJob and
     * StatefulBeanInvokingJob classes.
     * 
     * @see #setApplicationContext(ApplicationContext)
     * @see BeanInvokingJob#execute(JobExecutionContext)
     */
    protected static ApplicationContext applicationContext;

    private Log logger = LogFactory.getLog(getClass());

    /**
     * The JobDetail produced by the <code>afterPropertiesSet</code> method of
     * this Class will be assigned to the Group specified by this property.
     * Default: Scheduler.DEFAULT_GROUP
     * 
     * @see #afterPropertiesSet()
     * @see Scheduler#DEFAULT_GROUP
     */
    private String group = Scheduler.DEFAULT_GROUP;

    /**
     * Indicates whether or not the Bean Method should be invoked by more than
     * one Scheduler at the specified time (like when deployed to a cluster,
     * and/or when there are multiple Spring ApplicationContexts in a single
     * JVM<i> - Tomcat 5.5 creates 2 or more instances of the DispatcherServlet
     * (a pool), which in turn creates a separate Spring ApplicationContext for
     * each instance of the servlet</i>)
     * <p>
     * Used by <code>afterPropertiesSet</code> to set the JobDetail.jobClass to
     * BeanInvokingJob.class or StatefulBeanInvokingJob.class when true or
     * false, respectively. Default: true
     * 
     * @see #afterPropertiesSet()
     */
    private boolean concurrent = true;

    /**
     * Used to set the JobDetail.durable property. Default: false
     * <p>
     * Durability - if a job is non-durable, it is automatically deleted from
     * the scheduler once there are no longer any active triggers associated
     * with it.
     * 
     * @see <a
     *      href="http://www.opensymphony.com/quartz/wikidocs/TutorialLesson3.html">http://www.opensymphony.com/quartz/wikidocs/TutorialLesson3.html</a>
     * @see #afterPropertiesSet()
     */
    private boolean durable = false;

    /**
     * Used by <code>afterPropertiesSet</code> to set the
     * JobDetail.requestsRecovery property. Default: false<BR>
     * <p>
     * RequestsRecovery - if a job "requests recovery", and it is executing
     * during the time of a 'hard shutdown' of the scheduler (i.e. the process
     * it is running within crashes, or the machine is shut off), then it is
     * re-executed when the scheduler is started again. In this case, the
     * JobExecutionContext.isRecovering() method will return true.
     * 
     * @see <a
     *      href="http://www.opensymphony.com/quartz/wikidocs/TutorialLesson3.html">http://www.opensymphony.com/quartz/wikidocs/TutorialLesson3.html</a>
     * @see #afterPropertiesSet()
     */
    private boolean shouldRecover = false;

    /**
     * The name assigned to this bean in the Spring ApplicationContext. Used by
     * <code>afterPropertiesSet</code> to set the JobDetail.name property.
     * 
     * @see afterPropertiesSet()
     * @see JobDetail#setName(String)
     **/
    private String beanName;

    /**
     * The JobDetail produced by the <code>afterPropertiesSet</code> method, and
     * returned by the <code>getObject</code> method of the Spring FactoryBean
     * interface.
     * 
     * @see #afterPropertiesSet()
     * @see #getObject()
     * @see FactoryBean
     **/
    private JobDetail jobDetail;

    /**
     * The name or id of the bean to invoke, as it is declared in the Spring
     * ApplicationContext.
     **/
    private String targetBean;

    /**
     * The method to invoke on the bean identified by the targetBean property.
     **/
    private String targetMethod;

    /**
     * The arguments to provide to the method identified by the targetMethod
     * property. These Objects must be Serializable when concurrent=="true".
     */
    private Object[] arguments;

    /**
     * Get the targetBean property.
     * 
     * @see #targetBean
     * @return targetBean
     */
    public String getTargetBean() {
        return targetBean;
    }

    /**
     * Set the targetBean property.
     * 
     * @see #targetBean
     */
    public void setTargetBean(String targetBean) {
        this.targetBean = targetBean;
    }

    /**
     * Get the targetMethod property.
     * 
     * @see #targetMethod
     * @return targetMethod
     */
    public String getTargetMethod() {
        return targetMethod;
    }

    /**
     * Set the targetMethod property.
     * 
     * @see #targetMethod
     */
    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    /**
     * @return jobDetail - The JobDetail that is created by the
     *         afterPropertiesSet method of this FactoryBean
     * @see #jobDetail
     * @see #afterPropertiesSet()
     * @see FactoryBean#getObject()
     */
    public JobDetail getObject() throws Exception {
        return jobDetail;
    }

    /**
     * @return JobDetail.class
     * @see FactoryBean#getObjectType()
     */
    public Class<JobDetail> getObjectType() {
        return JobDetail.class;
    }

    /**
     * @return true
     * @see FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * Set the beanName property.
     * 
     * @see #beanName
     * @see BeanNameAware#setBeanName(String)
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * Invoked by the Spring container after all properties have been set.
     * <p>
     * Sets the <code>jobDetail</code> property to a new instance of JobDetail
     * <ul>
     * <li>jobDetail.name is set to <code>beanName</code><br>
     * <li>jobDetail.group is set to <code>group</code><br>
     * <li>jobDetail.jobClass is set to BeanInvokingJob.class or
     * StatefulBeanInvokingJob.class depending on whether the
     * <code>concurrent</code> property is set to true or false, respectively.
     * <br>
     * <li>jobDetail.durability is set to <code>durable</code>
     * <li>jobDetail.volatility is set to <code>volatility</code>
     * <li>jobDetail.requestsRecovery is set to <code>shouldRecover</code>
     * <li>jobDetail.jobDataMap["targetBean"] is set to <code>targetBean</code>
     * <li>jobDetail.jobDataMap["targetMethod"] is set to
     * <code>targetMethod</code>
     * <li>jobDetail.jobDataMap["arguments"] is set to <code>arguments</code>
     * <li>Each JobListener name in <code>jobListenerNames</code> is added to
     * the <code>jobDetail</code> object.
     * </ul>
     * <p>
     * Logging occurs at the DEBUG and INFO levels; 4 lines at the DEBUG level,
     * and 1 line at the INFO level.
     * <ul>
     * <li>DEBUG: start
     * <li>DEBUG: Creating JobDetail <code>{beanName}</code>
     * <li>DEBUG: Registering JobListener names with JobDetail object
     * <code>{beanName}</code>
     * <li>INFO: Created JobDetail: <code>{jobDetail}</code>; targetBean:
     * <code>{targetBean}</code>; targetMethod: <code>{targetMethod}</code>;
     * arguments: <code>{arguments}</code>;
     * <li>DEBUG: end
     * </ul>
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     * @see JobDetail
     * @see #jobDetail
     * @see #beanName
     * @see #group
     * @see BeanInvokingJob
     * @see StatefulBeanInvokingJob
     * @see #durable
     * @see #volatility
     * @see #shouldRecover
     * @see #targetBean
     * @see #targetMethod
     * @see #arguments
     * @see #jobListenerNames
     */
    public void afterPropertiesSet() throws Exception {
        try {
            logger.debug("start");

            logger.debug("Creating JobDetail " + beanName);
            JobDetailImpl jobDetailImpl = new JobDetailImpl();
            jobDetailImpl.setName(beanName);
            jobDetailImpl.setGroup(group);
            jobDetailImpl.setJobClass(concurrent ? BeanInvokingJob.class : StatefulBeanInvokingJob.class);
            jobDetailImpl.setDurability(durable);
            jobDetailImpl.setRequestsRecovery(shouldRecover);
            jobDetailImpl.getJobDataMap().put("targetBean", targetBean);
            jobDetailImpl.getJobDataMap().put("targetMethod", targetMethod);
            jobDetailImpl.getJobDataMap().put("arguments", arguments);
            
            jobDetail = jobDetailImpl;
            logger.info("Created JobDetail: " + jobDetailImpl + "; targetBean: "
                    + targetBean + "; targetMethod: " + targetMethod
                    + "; arguments: " + arguments + ";");
        } finally {
            logger.debug("end");
        }
    }

    /**
     * Setter for the concurrent property.
     * 
     * @param concurrent
     * @see #concurrent
     */
    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    /**
     * setter for the durable property.
     * 
     * @param durable
     * 
     * @see #durable
     */
    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    /**
     * setter for the group property.
     * 
     * @param group
     * 
     * @see #group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * setter for the {@link #shouldRecover} property.
     * 
     * @param shouldRecover
     * @see #shouldRecover
     */
    public void setShouldRecover(boolean shouldRecover) {
        this.shouldRecover = shouldRecover;
    }

    /**
     * Set the Spring ApplicationContext in which this class has been deployed.
     * <p>
     * Invoked by Spring as a result of the ApplicationContextAware interface
     * implemented by this Class.
     * 
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext context)
            throws BeansException {
        applicationContext = context;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    /**
     * This is a cluster safe Job designed to invoke a method on any bean
     * defined within the same Spring ApplicationContext.
     * <p>
     * The only entries this Job expects in the JobDataMap are "targetBean" and
     * "targetMethod".<br>
     * - It uses the value of the <code>targetBean</code> entry to get the
     * desired bean from the Spring ApplicationContext.<br>
     * - It uses the value of the <code>targetMethod</code> entry to determine
     * which method of the Bean (identified by targetBean) to invoke.
     * <p>
     * It uses the static ApplicationContext in the
     * BeanInvokingJobDetailFactoryBean, which is ApplicationContextAware, to
     * get the Bean with which to invoke the method.
     * <p>
     * All Exceptions thrown from the execute method are caught and wrapped in a
     * JobExecutionException.
     * 
     * @see BeanInvokingJobDetailFactoryBean#applicationContext
     * @see #execute(JobExecutionContext)
     * 
     * @author Stephen M. Wick
     */
    public static class BeanInvokingJob implements Job {
        protected Log logger = LogFactory.getLog(getClass());

        /**
         * When invoked by a Quartz scheduler, <code>execute</code> invokes a
         * method on a bean deployed within the scheduler's Spring
         * ApplicationContext.
         * <p>
         * <b>Implementation</b><br>
         * The bean is identified by the "targetBean" entry in the JobDataMap of
         * the JobExecutionContext provided.<br>
         * The method is identified by the "targetMethod" entry in the
         * JobDataMap of the JobExecutionContext provided.<br>
         * <p>
         * The Quartz scheduler shouldn't start up correctly if the bean
         * identified by "targetBean" cannot be found in the scheduler's Spring
         * ApplicationContext. BeanFactory.getBean() throws an exception if the
         * targetBean doesn't exist, so I'm not going to waste any code testing
         * for the bean's existance in the ApplicationContext.
         * <p>
         * Logging is provided at the DEBUG and INFO levels; 5 lines at the
         * DEBUG level, and 1 line at the INFO level.
         * 
         * @see Job#execute(JobExecutionContext)
         */
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
            try {
                logger.debug("start");

                String targetBean = context.getMergedJobDataMap().getString(
                        "targetBean");
                logger.debug("targetBean is " + targetBean);
                if (targetBean == null)
                    throw new JobExecutionException(
                            "targetBean cannot be null.", false);

                String targetMethod = context.getMergedJobDataMap().getString(
                        "targetMethod");
                logger.debug("targetMethod is " + targetMethod);
                if (targetMethod == null)
                    throw new JobExecutionException(
                            "targetMethod cannot be null.", false);

                // when org.quartz.jobStore.useProperties=="true" the arguments
                // entry (which should be an Object[]) in the JobDataMap gets
                // converted into a String.
                Object argumentsObject = context.getMergedJobDataMap().get(
                        "arguments");
                Object[] arguments = (argumentsObject instanceof String) ? null
                        : (Object[]) argumentsObject;
                logger.debug("arguments array is " + arguments);

                Object bean = applicationContext.getBean(targetBean);
                logger.debug("applicationContext resolved bean name/id '"
                        + targetBean + "' to " + bean);

                MethodInvoker beanMethod = new MethodInvoker();
                beanMethod.setTargetObject(bean);
                beanMethod.setTargetMethod(targetMethod);
                beanMethod.setArguments(arguments);
                beanMethod.prepare();
                logger.info("Invoking Bean: " + targetBean + "; Method: "
                        + targetMethod + "; arguments: " + arguments + ";");
                beanMethod.invoke();
            } catch (JobExecutionException e) {
                throw e;
            } catch (Exception e) {
                throw new JobExecutionException(e);
            } finally {
                logger.debug("end");
            }
        }
    }

    public static class StatefulBeanInvokingJob extends BeanInvokingJob
            implements StatefulJob {
        // No additional functionality; just needs to implement StatefulJob.
    }
}