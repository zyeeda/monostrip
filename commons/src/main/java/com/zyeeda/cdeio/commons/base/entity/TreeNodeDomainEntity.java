package com.zyeeda.cdeio.commons.base.entity;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * 树类型基类实体
 * 
 * $Author$
 */
@MappedSuperclass
public class TreeNodeDomainEntity extends DomainEntity {

	/**
	 * 序列化
	 */
	private static final long serialVersionUID = 1233326661404185568L;
	
	/**
	 * 节点的 checkBox / radio 的 勾选状态 [setting.check.enable = true & treeNode.nocheck = false 时有效]
	 * 1、如果不使用 checked 属性设置勾选状态，请修改 setting.data.key.checked
	 * 2、建立 treeNode 数据时设置 treeNode.checked = true 可以让节点的输入框默认为勾选状态
	 * 3、修改节点勾选状态，可以使用 treeObj.checkNode / checkAllNodes / updateNode 方法，具体使用哪种请根据自己的需求而定
	 * 4、为了解决部分朋友生成 json 数据出现的兼容问题, 支持 "false","true" 字符串格式的数据
	 * 默认值：false
	 * 
	 * true 表示节点的输入框被勾选
	 * false 表示节点的输入框未勾选
	 */
	private Boolean checked;
	
	/**
	 * 1、设置节点的 checkbox / radio 是否禁用 [setting.check.enable = true 时有效]
	 * 2、为了解决部分朋友生成 json 数据出现的兼容问题, 支持 "false","true" 字符串格式的数据
	 * 3、请勿对已加载的节点修改此属性，禁止 或 取消禁止 请使用 setChkDisabled() 方法
	 * 4、初始化时，如果需要子孙节点继承父节点的 chkDisabled 属性，请设置 setting.check.chkDisabledInherit 属性
	 * 默认值：false
	 * 
	 * true 表示此节点的 checkbox / radio 被禁用
	 * false 表示此节点的 checkbox / radio 可以使用
	 */
	private Boolean chkDisabled;
	
	/**
	 * 强制节点的 checkBox / radio 的 半勾选状态 [setting.check.enable = true & treeNode.nocheck = false 时有效]
	 * 1、强制为半勾选状态后，不再进行自动计算半勾选状态
	 * 2、设置 treeNode.halfCheck = false 或 null 才能恢复自动计算半勾选状态
	 * 3、为了解决部分朋友生成 json 数据出现的兼容问题, 支持 "false","true" 字符串格式的数据
	 * 默认值：false
	 * 
	 * true 表示节点的输入框 强行设置为半勾选
	 * false 表示节点的输入框 根据 zTree 的规则自动计算半勾选状态
	 */
	private Boolean halfCheck;
	
	/**
	 * 节点自定义图标的 URL 路径
	 * 1、父节点如果只设置 icon ，会导致展开、折叠时都使用同一个图标
	 * 2、父节点展开、折叠使用不同的个性化图标需要同时设置 treeNode.iconOpen / treeNode.iconClose 两个属性
	 * 3、如果想利用 className 设置个性化图标，需要设置 treeNode.iconSkin 属性
	 * 
	 * 图标图片的 url 可以是相对路径也可以是绝对路径
	 * 设置相对路径请注意页面与图片之间的关系，确保图片能够正常加载
	 */
	private String icon;
	
	/**
	 * 父节点自定义折叠时图标的 URL 路径
	 * 1、此属性只针对父节点有效
	 * 2、此属性必须与 iconOpen 同时使用
	 * 3、如果想利用 className 设置个性化图标，需要设置 treeNode.iconSkin 属性
	 * 
	 * 图标图片的 url 可以是相对路径也可以是绝对路径
	 * 设置相对路径请注意页面与图片之间的关系，确保图片能够正常加载
	 */
	private String iconClose;
	
	/**
	 * 父节点自定义展开时图标的 URL 路径
	 * 1、此属性只针对父节点有效
	 * 2、此属性必须与 iconClose 同时使用
	 * 3、如果想利用 className 设置个性化图标，需要设置 treeNode.iconSkin 属性
	 * 
	 * 图标图片的 url 可以是相对路径也可以是绝对路径
	 * 设置相对路径请注意页面与图片之间的关系，确保图片能够正常加载
	 */
	private String iconOpen;
	
	/**
	 * 节点自定义图标的 className
	 * 1、需要修改 css，增加相应 className 的设置
	 * 2、css 方式简单、方便，并且同时支持父节点展开、折叠状态切换图片
	 * 3、css 建议采用图片分割渲染的方式以减少反复加载图片，并且避免图片闪动
	 * 4、zTree v3.x 的 iconSkin 同样支持 IE6
	 * 5、如果想直接使用 图片的Url路径 设置节点的个性化图标，需要设置 treeNode.icon / treeNode.iconOpen / treeNode.iconClose 属性
	 */
	private String iconSkin;
	
	/**
	 * 判断 treeNode 节点是否被隐藏
	 * 1、初始化 zTree 时，如果节点设置 isHidden = true，会被自动隐藏
	 * 2、请勿对已加载的节点修改此属性，隐藏 / 显示 请使用 hideNode() / hideNodes() / showNode() / showNodes() 方法
	 * 
	 * true 表示被隐藏
	 * false 表示被显示
	 */
	private Boolean isHidden;
	
	/**
	 * 记录 treeNode 节点是否为父节点
	 * 1、初始化节点数据时，根据 treeNode.children 属性判断，有子节点则设置为 true，否则为 false
	 * 2、初始化节点数据时，如果设定 treeNode.isParent = true，即使无子节点数据，也会设置为父节点
	 * 3、为了解决部分朋友生成 json 数据出现的兼容问题, 支持 "false","true" 字符串格式的数据
	 * 
	 * true 表示是父节点
	 * false 表示不是父节点
	 */
	private String isParent;
	
	/**
	 * 1、设置节点是否隐藏 checkbox / radio [setting.check.enable = true 时有效]
	 * 2、为了解决部分朋友生成 json 数据出现的兼容问题, 支持 "false","true" 字符串格式的数据
	 * 默认值：false
	 * 
	 * true 表示此节点不显示 checkbox / radio，不影响勾选的关联关系，不影响父节点的半选状态。
	 * false 表示节点具有正常的勾选功能
	 */
	private Boolean nocheck;
	
	/**
	 * 记录 treeNode 节点的 展开 / 折叠 状态
	 * 1、初始化节点数据时，如果设定 treeNode.open = true，则会直接展开此节点
	 * 2、叶子节点 treeNode.open = false
	 * 3、为了解决部分朋友生成 json 数据出现的兼容问题, 支持 "false","true" 字符串格式的数据
	 * 4、非异步加载模式下，无子节点的父节点设置 open=true 后，可显示为展开状态，但异步加载模式下不会生效。（v3.5.15+）
 	 * 默认值：false
 	 * 
 	 * true 表示节点为 展开 状态
	 * false 表示节点为 折叠 状态
	 */
	private Boolean open;
	
	/**
	 * 设置点击节点后在何处打开 url [treeNode.url 存在时有效]
	 * 
	 * 同超链接 target 属性: "_blank", "_self" 或 其他指定窗口名称
	 * 省略此属性，则默认为 "_blank"
	 */
	private String target;
	
	/**
	 * 节点链接的目标 URL
	 * 1、编辑模式 (setting.edit.enable = true) 下此属性功能失效，如果必须使用类似功能，请利用 onClick 事件回调函数自行控制。
	 * 2、如果需要在 onClick 事件回调函数中进行跳转控制，那么请将 URL 地址保存在其他自定义的属性内，请勿使用 url
	 * 
	 * 同超链接 href 属性
	 */
	private String url;

	@Transient
	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	@Transient
	public Boolean getChkDisabled() {
		return chkDisabled;
	}

	public void setChkDisabled(Boolean chkDisabled) {
		this.chkDisabled = chkDisabled;
	}

	@Transient
	public Boolean getHalfCheck() {
		return halfCheck;
	}

	public void setHalfCheck(Boolean halfCheck) {
		this.halfCheck = halfCheck;
	}
	
	@Transient
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Transient
	public String getIconClose() {
		return iconClose;
	}

	public void setIconClose(String iconClose) {
		this.iconClose = iconClose;
	}

	@Transient
	public String getIconOpen() {
		return iconOpen;
	}

	public void setIconOpen(String iconOpen) {
		this.iconOpen = iconOpen;
	}

	@Transient
	public String getIconSkin() {
		return iconSkin;
	}

	public void setIconSkin(String iconSkin) {
		this.iconSkin = iconSkin;
	}

	@Transient
	public Boolean getIsHidden() {
		return isHidden;
	}

	public void setIsHidden(Boolean isHidden) {
		this.isHidden = isHidden;
	}

	@Transient
	public String getIsParent() {
		return isParent;
	}

	public void setIsParent(String isParent) {
		this.isParent = isParent;
	}

	@Transient
	public Boolean getNocheck() {
		return nocheck;
	}

	public void setNocheck(Boolean nocheck) {
		this.nocheck = nocheck;
	}

	@Transient
	public Boolean getOpen() {
		return open;
	}

	public void setOpen(Boolean open) {
		this.open = open;
	}

	@Transient
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Transient
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
