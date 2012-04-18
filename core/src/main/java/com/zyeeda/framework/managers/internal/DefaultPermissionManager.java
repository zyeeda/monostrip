package com.zyeeda.framework.managers.internal;



import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.zyeeda.framework.entities.Role;
import com.zyeeda.framework.managers.PermissionManager;
import com.zyeeda.framework.viewmodels.AuthVO;
import com.zyeeda.framework.viewmodels.PermissionVo;
public class DefaultPermissionManager implements PermissionManager {

	//private final static String ROAM_PERMISSION_FILE = "roamPermission.xml";
	private final static String ROAM_PERMISSION_FILE = "roamPermission.xml";
	public DefaultPermissionManager() {
	}
	
	public void getAllPermssion(AuthVO auth, String authXml, List<String> authListByRole)
			throws XPathExpressionException, IOException {
		List<AuthVO> list = new ArrayList<AuthVO>();
		list.add(auth);
		List<AuthVO> authList = this.findSubRoamPermissionById(auth.getId(), authXml, authListByRole);
		auth.getChildren().addAll(authList);
		for (AuthVO authVo : authList) {
			this.getAllPermssion(authVo, authXml, authListByRole);
		}
	}

	public List<AuthVO> getPermissionToTree(String id, String authXml, List<String> authListByRole)
			throws XPathExpressionException, IOException {
		 List<AuthVO> listPermission = this.findSubRoamPermissionById(id, authXml, authListByRole);
		for(AuthVO permission : listPermission) {
			this.getAllPermssion(permission, authXml, authListByRole);
		}
		return listPermission;
	}

	public List<AuthVO> findSubRoamPermissionById(String id, String authXml, List<String> authListByRole)
			throws XPathExpressionException, IOException {

		List<AuthVO> authList = new ArrayList<AuthVO>();
		InputStream is = null;
		XPathExpression exp = null;
		try {
			XPathFactory fac = XPathFactory.newInstance();
			XPath xpath = fac.newXPath();
			exp = xpath.compile("//p[@id='" + id + "']");
			is = this.getClass().getClassLoader().getResourceAsStream(
					authXml);
			Document document;
			try {
				document = DefaultMenuManager.generateDocumentByInputStream(is);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			NodeList list = (NodeList) exp
					.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < list.getLength(); i++) {
				Element element = (Element) list.item(i);
				if (element == null) {
					return null;
				}
				NodeList children = element.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node e = children.item(j);
					if (e instanceof Element) {
						Element el = (Element) e;
						AuthVO authVo = new AuthVO(); 
						String value = el.getAttribute("value");
						authVo.setId(el.getAttribute("id"));
						authVo.setLabel( el.getAttribute("name"));
						authVo.setType("ltask");
						authVo.setTag(value);
						if(!(el.getAttribute("value").endsWith("*"))) {
							authVo.setLeaf(true);
						}
						if(authListByRole.contains(value)) {
							authVo.setChecked(true);
						}
						authList.add(authVo);
					}
				}
			}
		} finally {
			is.close();
		}
		return authList;
	}


	public List<PermissionVo> findSubPermissionById(String id, String authXml)
			throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		List<PermissionVo> authList = null;
		InputStream is = null;
		XPathExpression exp = null;
		try {
			XPathFactory fac = XPathFactory.newInstance();
			XPath xpath = fac.newXPath();
			exp = xpath.compile("//p[@id='" + id + "']");
			is = this.getClass().getClassLoader().getResourceAsStream(
					authXml);
			Document document = DefaultMenuManager.generateDocumentByInputStream(is);
			NodeList list = (NodeList) exp
					.evaluate(document, XPathConstants.NODESET);
			authList = new ArrayList<PermissionVo>();
			for (int i = 0; i < list.getLength(); i++) {
				Element element = (Element) list.item(i);
				NodeList children = element.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node e = children.item(j);
					if (e instanceof Element) {
						Element el = (Element) e;
						PermissionVo permission = new PermissionVo();
						permission.setId(el.getAttribute("id"));
						permission.setName(el.getAttribute("name"));
						permission.setValue(el.getAttribute("value"));
						permission.setOrderBy(el.getAttribute("order"));
						if (el.getAttribute("value").endsWith("*")) {
							permission.setIsHaveIO(false);
						} else {
							permission.setIsHaveIO(true);
						}
						authList.add(permission);
					}
				}
			}
		} finally {
			is.close();
		}
		return authList;
	}

	public void getPermissionByPathToRomPermission(ServletContext ctx)
			throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		Map<String, PermissionVo> mapPermission = new HashMap<String, PermissionVo>();
//		RoleManager roleMgr = new DefaultRoleManager();
		Role role = new Role();
		role.setPermissions("form:edite:edit;form:edite:create;form:edite:createTemplate;form:edite:createExec;form:revision:*revi;form:revision:*manager;form:off:offForm;form:off:offMgrForm;form:edite:waitDeal;form:edite:waitMgrDeal;form:edite:myForm;form:edite:searchForm;4d:qnaire:create;4d:qnaire:noRelease;4d:qnaire:survey;4d:qnaire:colse;4d:answer:noReply;4d:analysis:quarter;4d:analysis:word;4d:message:publish;4d:message:show;skillMgr:examManager:manager;skillMgr:examManager:rule;skillMgr:trainManager:examanswer;skillMgr:everydayManager::everydayques;skillMgr:everydayManager:everydayquesmanager;skillMgr:everydayManager:search;from:formCount:temp_DeptCount;from:formCount:temp_FormCount;from:excut:call;form:excut:waitForm;form:excut:myWorkForm;form:excut:excutSearch;from:excutAccpt:callAccpt;form:excutAccpt:waitAccptForm;form:excutAccpt:myAccptForm;form:excutAccpt:adAccptSearch;from:accptWork:callAccptWork;form:accptWork:waitAccptWorkForm;form:accptWork:myAccptWorkForm;form:accptWork:adAccptWorkSearch;form:workticktemanager:one;form:workticktemanager:two;form:workticktemanager:three;form:workticktemanager:waitWt;form:workticktemanager:one;form:workticktemanager:two;form:workticktemanager:three;form:workticktemanager:waitWt;form:workticktemanager:myBianDianWt;form:workticktemanager:chaxunWt;form:workticktemanager:myBianDianWt;form:workticktemanager:chaxunWt;form:xl_workticktemanager:xl_one;form:xl_workticktemanager:xl_two;form:xl_workticktemanager:xl_waitWt;form:xl_workticktemanager:xl_one;form:xl_workticktemanager:xl_two;form:xl_workticktemanager:xl_waitWt;form:xl_workticktemanager:xl_myWt;form:xl_workticktemanager:xl_chaxunWt;form:xl_workticktemanager:xl_myWt;form:xl_workticktemanager:xl_chaxunWt;plan:planManagerAgain:createIntegration;plan:planManagerAgain:createSinoProbe;plan:planManagerAgain:createDisposalSite;plan:planManagerAgain:waitDeal;plan:planManagerAgain:createIntegration;plan:planManagerAgain:createSinoProbe;plan:planManagerAgain:createDisposalSite;plan:planManagerAgain:waitDeal;plan:planManagerAgain:myDeal;plan:planManagerAgain:search;plan:planManagerAgain:myDeal;plan:planManagerAgain:search;plan:ProgramManager:createOperate;plan:ProgramManager:createOverhaul;plan:ProgramManager:createStart;plan:ProgramManager:waitDeal;plan:ProgramManager:createOperate;plan:ProgramManager:createOverhaul;plan:ProgramManager:createStart;plan:ProgramManager:waitDeal;plan:ProgramManager:myDeal;plan:ProgramManager:search;plan:ProgramManager:myDeal;plan:ProgramManager:search;fms:defectManagement:startDefect;fms:defectManagement:defectManageBox;fms:defectManagement:defectMyBox;fms:defectManagement:defectSendBox;tongJiFenXi:qxsltj;tongJiFenXi:qxztfb;tongJiFenXi:qxglxlzbtj;tongJiFenXi:xqltj;tongJiFenXi:jjzdqxjs;tongJiFenXi:xzqxfx;tongJiFenXi:byxcqxfx;tongJiFenXi:wxcqxfx;tongJiFenXi:cxushijiantj;tongJiFenXi:dcqxword;sdqxgl:sdqxlcgl:sdcreate;sdqxgl:sdqxlcgl:haddealsd;sdqxgl:sdqxlcgl:mydealsd;sdqxgl:sdqxlcgl:sdsearch;fms:defectManagement:startDefect;fms:defectManagement:defectManageBox;fms:defectManagement:defectMyBox;fms:defectManagement:defectSendBox;tongJiFenXi:qxsltj;tongJiFenXi:qxztfb;tongJiFenXi:qxglxlzbtj;tongJiFenXi:xqltj;tongJiFenXi:jjzdqxjs;tongJiFenXi:xzqxfx;tongJiFenXi:byxcqxfx;tongJiFenXi:wxcqxfx;tongJiFenXi:cxushijiantj;tongJiFenXi:dcqxword;sdqxgl:sdqxlcgl:sdcreate;sdqxgl:sdqxlcgl:haddealsd;sdqxgl:sdqxlcgl:mydealsd;sdqxgl:sdqxlcgl:sdsearch;xiTongGuanLi:xtgl:jsgl;xiTongGuanLi:xtgl:zzgl;form:xtgl:riskManage;systemMessage:message:publish;systemMessage:message:show;changYongXiTong:changYong:shengChan;changYongXiTong:changYong:chengBaoShang;changYongXiTong:changYong:OAXT;changYongXiTong:changYong:caiWuFMIS;imo:jianCeJiLu:noteMgr:biLeiQi;imo:jianCeJiLu:noteMgr:ceLiang;imo:jianCeJiLu:noteMgr:jckyCL;imo:jianCeJiLu:noteMgr:jddzCL;imo:jianCeJiLu:noteMgr:jyzJC;imo:jianCeJiLu:noteMgr:xlhwCW;imo:jianCeJiLu:noteMgr:xlymCS;imo:shuDianYunXing:runMgr:jdxgc;imo:shuDianYunXing:runMgr:sdqx;imo:shuDianYunXing:runMgr:tsqyfx;imo:shuDianYunXing:runMgr:xlsggz;imo:shuDianYunXing:runMgr:xltd;imo:shuDianYunXing:runMgr:xlxs;imo:shuDianYunXing:runMgr:xlyxfx;imo:bianDian:Manger:biaoCanShu;imo:bianDian:Manger:riDianLiang;imo:bianDian:Manger:biaoCanShu;imo:bianDian:Manger:dianJiLu;imo:shiYan:turn:zhuanHuan;imo:jiChu:function:sheShi;imo:jiChu:function:xinXi;imo:jiChu:function:quexianSelect;imo:jiChu:function:quexianRenYuan;imo:jiChu:function:pingJia;imo:jiChu:function:taiZhang;imo:jiChu:function:zhangAi;imo:liangPiao:LPGL:dengJi;imo:liangPiao:LPGL:dengJi2;imo:liangPiao:LPGL:dengJi3;imo:liangPiao:LPGL:dengJi1;imo:liangPiao:LPGL:jiLu;imo:queXian:defectManger:queXianGuanLi;imo:queXian:defectManger:showInbox;imo:sheBei:assets:pingJi;imo:sheBei:assets:dingYi;imo:sheBei:assets:taiZhang1;imo:sheBei:assets:taiZhang2;imo:shunShi:assetsxunshi:dingQiJiLu;imo:shunShi:assetsxunshi:biLei;imo:shunShi:assetsxunshi:bianYa;imo:shunShi:assetsxunshi:duanLu;imo:shunShi:assetsxunshi:faLeng;imo:shunShi:assetsxunshi:fang;imo:shunShi:assetsxunshi:xunShi;imo:shunShi:assetsxunshi:xiaoFang;imo:shunShi:assetsxunshi:ceLiang;imo:shiGu:fault:qieDuan;imo:shiGu:fault:tiaoZha;imo:yunXing:operation:dingZhi;imo:yunXing:operation:biSuo;imo:yunXing:operation:touTui;imo:yunXing:operation:zhuangChai;imo:yunXing:operation:touQie;imo:yunXing:operation:bianDianBan;imo:yunXing:operation:jieYong;imo:yunXing:operation:tiaoDu;imo:yunXing:operation:tingFu;imo:yunXing:operation:daShi;imo:yunXing:operation:jianShi;imo:yunXing:operation:liShi;imo:yunXing:operation:luRu;imo:yunXing:operation:dingZhi1;imo:yunXing:operation:tingFu1;imo:yunXing:operation:jiaoJie;imo:tongXinYunXing:ejtxwlsskz;imo:tongXinYunXing:ejtxwlyx;imo:tongXinYunXing:gzrwd;imo:tongXinYunXing:txxsjl;imo:tongXinYunXing:txzgzjl;imo:tongXinYunXing:wlxtzysbyx;imo:tongXinYunXing:xxwlyxqk;imo:tongXinYunXing:yjtxwscsskz;imo:tongXinYunXing:yjtxwtx;imo:banZu:huoDong;imo:banZu:anQuanHuoDong;imo:banZu:banHouHui;imo:banZu:huiYi;imo:banZu:peiXun;imo:banZu:wenDa;imo:banZu:yuXiang;imo:banZu:fanShiGu;imo:banZu:fenXi;imo:banZu:gongZuoRiZhi;imo:banZu:huiYiJiLu;imo:banZu:shiftDuty&quanXianGuanLi:yxbmsh;quanXianGuanLi:sjbsh;quanXianGuanLi:sd;quanXianGuanLi:zzsd;quanXianGuanLi:sjzc;quanXianGuanLi:qxys;quanXianGuanLi:qxsjj;quanXianGuanLi:xqtjfx;quanXianGuanLi:qxfxyjl:submit;quanXianGuanLi:qxfxyjl:edite;quanXianGuanLi:jxbmsh:ljshenghe;quanXianGuanLi:jxbmsh:sqzc;quanXianGuanLi:xqtjfx:xiaoquefenxi;quanXianGuanLi:xqtjfx:sqzhbz;quanXianGuanLi:xqtjfx:dealxiaoquefenxi;quanXianGuanLi:xqtjfx:dealsqzhbz;xiuBianGuanLi:bz;xiuBianGuanLi:psz;xiuBianGuanLi:xb;mbbdbzlc:shqr;mbbdbzlc:shfbbyld;mbbdbzlc:shfbagain;xiuBianGuanLi:yps:confirmRevision;xiuBianGuanLi:yps:sendTemplateForm;mbbdbzlc:information:esc;mbbdbzlc:information:excutly;mbbdbzlc:shfb:shfbform;mbbdbzlc:shfb:shfbxiuban;mbbdbzlc:shfb:needInformation;lsbddyzxlc:xcdyEdite;lsbddyzxlc:xcsh;lsbddyzxlc:xcdybd:xcdybdSumbit;lsbddyzxlc:xcdybd:xcdybdEdite;lsbddyzxlc:zx:formrk;lsbddyzxlc:zx:formrkedite;xczxbddyzxlc:zhiXing;xczxbddyzxlc:dysh;xczxbddyzxlc:excuteEnd;xczxbddyzxlc:dydaitijiao:dysumbmit;xczxbddyzxlc:dydaitijiao:dyedite;czfagllc:bzsb;czfagllc:sh3;czfagllc:spxf;qdfagllc:bz3;qdfagllc:sh4;qdfagllc:sp3;qdfagllc:jieShu;jxfagllc:bzsb;jxfagllc:shzz;jxfagllc:sjbzzsh;jxfagllc:sjbzrsd;jxfagllc:sp4;jxfagllc:deptDirectorReview;CompositPlanManage:edite;CompositPlanManage:review;CompositPlanManage:publish;SpecialPlan:spedite;SpecialPlan:spdircetor;SpecialPlan:spreview;SiteDisposalManage:sdmedite;SiteDisposalManage:sdmreview;SiteDisposalManage:sdmdedicated;SiteDisposalManage:sdmdirector;gzpglone:wkksgzone;gzpglone:wkzj1one;gzpglone:dsqone:wkbjone;gzpglone:dsqone:wktjone;gzpglone:dsqone:wkscone;gzpglone:ysqone:wkht1one;gzpglone:ysqone:wkqfone;gzpglone:yqfone:wkjsone;gzpglone:yqfone:wkht2one;gzpglone:yqfone:wkhqone;gzpglone:yhqone:wkjsone;gzpglone:yhqone:wkht3one;gzpglone:yjsone:wkgzxkone;gzpglone:gzxkone:wkjdone;gzpglone:gzxkone:wkfzrbgone;gzpglone:gzxkone:wkyqone;gzpglone:gzxkone:wkzjgzlrone;gzpglone:gzxkone:wkgzpzjone;gzpglone:gzxkone:wkzjone;gzpgltwo:wkksgztwo;gzpgltwo:wkzj1two;gzpgltwo:dsqtwo:wkbjtwo;gzpgltwo:dsqtwo:wktjtwo;gzpgltwo:dsqtwo:wksctwo;gzpgltwo:ysqtwo:wkht1two;gzpgltwo:ysqtwo:wkqftwo;gzpgltwo:yqftwo:wkjstwo;gzpgltwo:yqftwo:wkht2two;gzpgltwo:yqftwo:wkhqtwo;gzpgltwo:yhqtwo:wkjstwo;gzpgltwo:yhqtwo:wkht3two;gzpgltwo:yjstwo:wkgzxktwo;gzpgltwo:gzxktwo:wkjdtwo;gzpgltwo:gzxktwo:wkfzrbgtwo;gzpgltwo:gzxktwo:wkyqtwo;gzpgltwo:gzxktwo:wkzjgzlrtwo;gzpgltwo:gzxktwo:wkzjtwo;gzpglthree:wkksgzthree;gzpglthree:wkzj1three;gzpglthree:dsqthree:wkbjthree;gzpglthree:dsqthree:wktjthree;gzpglthree:dsqthree:wkscthree;gzpglthree:ysqthree:wkht1three;gzpglthree:ysqthree:wkqfthree;gzpglthree:yjsthree:wkgzxkthree;gzpglthree:gzxkthree:wkjdthree;gzpglthree:gzxkthree:wkfzrbgthree;gzpglthree:gzxkthree:wkyqthree;gzpglthree:gzxkthree:wkzjgzlrthree;gzpglthree:gzxkthree:wkzjthree;gzpglshuone:wkksgzshuone;gzpglshuone:wkzj1shuone;gzpglshuone:dsqshuone:wkbjshuone;gzpglshuone:dsqshuone:wktjshuone;gzpglshuone:dsqshuone:wkscshuone;gzpglshuone:ysqshuone:wkht1shuone;gzpglshuone:ysqshuone:wkqfshuone;gzpglshuone:yqfshuone:wkjsshuone;gzpglshuone:yqfshuone:wkht2shuone;gzpglshuone:yqfshuone:wkhqshuone;gzpglshuone:yhqshuone:wkjsshuone;gzpglshuone:yhqshuone:wkht3shuone;gzpglshuone:yjsshuone:wkgzxkshuone;gzpglshuone:gzxkshuone:wkjdshuone;gzpglshuone:gzxkshuone:wkfzrbgshuone;gzpglshuone:gzxkshuone:wkyqshuone;gzpglshuone:gzxkshuone:wkzjgzlrshuone;gzpglshuone:gzxkshuone:wkzjshuone;gzpglshutwo:dsqshutwo:wkbjshutwo;gzpglshutwo:dsqshutwo:wktjshutwo;gzpglshutwo:dsqshutwo:wkscshutwo;gzpglshutwo:ysqshutwo:wkht1shutwo;gzpglshutwo:ysqshutwo:wkqfshutwo;transDefect:bianz;transDefect:bzreview;transDefect:review;transDefect:normolldispel;transDefect:normolacceptance;transDefect:zdzrsh;transDefect:zdqxsh;transDefect:scfgjsh;transDefect:importantdispel;transDefect:importantaccept;transDefect:tranend;mbbdbzlc:shqragain");
		InputStream is = null;
		XPathExpression exp = null;
		List<PermissionVo> listPermission = new ArrayList<PermissionVo>();
			try {
				is = this.getClass().getClassLoader().getResourceAsStream(
						ROAM_PERMISSION_FILE);
				Document document = DefaultMenuManager.generateDocumentByInputStream(is);
				XPathFactory fac = XPathFactory.newInstance();
				XPath xpath = fac.newXPath();
				for(String auth : role.getRoamPermissionList()) {
					PermissionVo permission = new PermissionVo();
					exp = xpath.compile("//p[@value='" + auth + "']");
					Node node = (Node) exp.evaluate(document, XPathConstants.NODE);
					Element elementNode = (Element) node;
					if (elementNode != null) {
						if (elementNode.getAttribute("value") == null) {
							return;
						}
						permission.setId(elementNode.getAttribute("id"));
						permission.setName(elementNode.getAttribute("name"));
						permission.setValue(elementNode.getAttribute("value"));
						permission.setOrderBy(elementNode.getAttribute("order"));
						if (elementNode.getAttribute("value").endsWith("*")) {
							permission.setIsHaveIO(false);
						} else {
							permission.setIsHaveIO(true);
						}
						listPermission.add(permission);
						mapPermission.put(auth, permission);
						
					}
				}
			} finally {
				if(is!=null){is.close();}
			}
			if(ctx.getAttribute("authPermission") == null) {
				ctx.setAttribute("authPermission", mapPermission);
			}
		}

	public PermissionVo getPermissionByPath(Node node)
			throws XPathExpressionException, IOException {
		PermissionVo permission = new PermissionVo();
			try {
				Element elementNode = (Element) node;
				if (elementNode != null) {
					if (elementNode.getAttribute("value") == null) {
						return null;
					}
					permission.setId(elementNode.getAttribute("id"));
					permission.setName(elementNode.getAttribute("name"));
					permission.setValue(elementNode.getAttribute("value"));
					permission.setOrderBy(elementNode.getAttribute("order"));
					if (elementNode.getAttribute("value").endsWith("*")) {
						permission.setIsHaveIO(false);
					} else {
						permission.setIsHaveIO(true);
					}
				} else {
					return null;
				}
			} finally {
//				if(is!=null){is.close();}
			}
		return permission;
	}

	public PermissionVo getParentPermissionByPath(Node node)
			throws XPathExpressionException, IOException {
		PermissionVo permission = new PermissionVo();
					Element elementNode = (Element) node;
					Element elementParent = null;
					if (elementNode != null) {
						elementParent = (Element) elementNode.getParentNode();
						permission.setId(elementParent.getAttribute("id"));
						permission.setName(elementParent.getAttribute("name"));
						permission
								.setValue(elementParent.getAttribute("value"));
						permission.setOrderBy(elementParent
								.getAttribute("order"));
						if (StringUtils.isBlank(elementParent
								.getAttribute("value"))) {
							permission = null;
						}
					}
		return permission;
	}
	
	private void getParentPermissionListAuthByPath(String auth,
			Set<String> allAuth, String authXml) throws XPathExpressionException, IOException {
		List<PermissionVo> permissionList = new ArrayList<PermissionVo>();
		permissionList = findSubPermissionByValue(auth, authXml);
		for (PermissionVo permission : permissionList) {
			allAuth.add(permission.getValue());
			this.getParentPermissionListAuthByPath(permission.getValue(), allAuth, authXml);
		}
	}

	public String getParentPermissionListAuthByList(List<String> authList, String authXml)
			throws XPathExpressionException, IOException {
		Set<String> allAuth = new HashSet<String>();
		for (String auth : authList) {
			allAuth.add(auth);
			this.getParentPermissionListAuthByPath(auth, allAuth, authXml);
		}
		String utils = StringUtils.join(allAuth, ";");
		return utils;
	}

	public List<PermissionVo> findSubPermissionByValue(String value, String authXml)
			throws XPathExpressionException, IOException {
		List<PermissionVo> authList = null;
		InputStream is = null;
		InputSource src = null;
		XPathExpression exp = null;
		try {
			XPathFactory fac = XPathFactory.newInstance();
			XPath xpath = fac.newXPath();
			exp = xpath.compile("//p[@value='" + value + "']");
			is = this.getClass().getClassLoader().getResourceAsStream(
					authXml);
			src = new InputSource(is);
			NodeList list = (NodeList) exp
					.evaluate(src, XPathConstants.NODESET);
			authList = new ArrayList<PermissionVo>();
			for (int i = 0; i < list.getLength(); i++) {
				Element element = (Element) list.item(i);
				if (element != null) {
					NodeList children = element.getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						Node e = children.item(j);
						if (e instanceof Element) {
							Element el = (Element) e;
							PermissionVo permission = new PermissionVo();
							permission.setId(el.getAttribute("id"));
							permission.setName(el.getAttribute("name"));
							permission.setValue(el.getAttribute("value"));
							permission.setOrderBy(el.getAttribute("order"));
							if (el.getAttribute("value").endsWith("*")) {
								permission.setIsHaveIO(false);
							} else {
								permission.setIsHaveIO(true);
							}
							authList.add(permission);
						}
						}
					}
				}
		} finally {
			is.close();
		}
		return authList;
	}

}
